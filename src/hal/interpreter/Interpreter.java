package hal.interpreter;

import hal.Hal;
import hal.interpreter.core.*;
import hal.interpreter.exceptions.NameException;
import hal.interpreter.exceptions.SyntaxException;
import hal.interpreter.exceptions.TypeException;
import hal.interpreter.types.*;
import hal.interpreter.types.enumerable.HalArray;
import hal.interpreter.types.enumerable.HalDictionary;
import hal.interpreter.types.enumerable.HalString;
import hal.interpreter.types.numeric.HalFloat;
import hal.interpreter.types.numeric.HalInteger;
import hal.interpreter.types.numeric.HalLong;
import hal.parser.HalLexer;
import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CharStream;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;

/** Class that implements the interpreter of the language. */

public class Interpreter
{
    private Parser parser;
    /** Memory of the virtual machine. */
    private Stack stack;
    private ReferenceRecord globals;

    /**
     * Stores the line number of the record statement.
     * The line number is used to report runtime errors.
     */
    private int linenumber = -1;

    /** File to write the trace of function calls. */
    private PrintWriter trace = null;

    /** Nested levels of function calls. */
    private int function_nesting = 0;
    private int calls = 0;
    
    /**
     * Constructor of the interpreter. It prepares the main
     * data structures for the execution of the main program.
     */
    public Interpreter(Parser parsr, HalModule mainModule, PrintWriter tracefile) {
        // Initialize and solve dependency cycles
        HalKernel.init();

        parser = parsr;
        globals = new ReferenceRecord(null);
        trace = tracefile;
        function_nesting = 0;

        stack = new Stack(); // Creates the memory of the virtual machine
        stack.pushContext(mainModule.value, mainModule, mainModule, null, 0);
    }

    public HalObject run(CharStream input) {
        stack.popUntilFirstLevel();
        HalTree t = parser.getTree(input);
        return evaluate(t);
    }

    public HalObject evaluate(HalTree t) {
        try {
            PreProcessAST(t); // Some internal pre-processing on the AST
            return executeListInstructions(t);
        } catch(ClassCastException e) {
            throw TypeException.fromCastException(e);
        }
    }

    public String getCurrentFilePath() {
        HalModule current = stack.getCurrentModule();

        if(current == null)
            return "none";

        return current.getPath();
    }

    /** Returns the contents of the stack trace */
    public String getStackTrace() {
        return stack.getStackTrace(lineNumber());
    }

    /** Returns a summarized contents of the stack trace */
    public String getStackTrace(int nitems) {
        return stack.getStackTrace(lineNumber(), nitems);
    }

    /**
     * Performs some pre-processing on the AST. Basically, it
     * calculates the value of the literals and stores a simpler
     * representation. See HalTree.java for details.
     */
    private void PreProcessAST(HalTree T) {
        if (T == null) return;
        switch(T.getType()) {
            case HalLexer.BOOLEAN: T.setBooleanValue(); break;
            default: break;
        }
        int n = T.getChildCount();
        for (int i = 0; i < n; ++i) PreProcessAST(T.getChild(i));
    }

    /**
     * Gets the record line number. In case of a runtime error,
     * it returns the line number of the statement causing the
     * error.
     */
    public int lineNumber() { return linenumber; }

    /** Defines the record line number associated to an AST node. */
    private void setLineNumber(HalTree t) { linenumber = t.getLine();}

    /** Defines the record line number with a specific value */
    private void setLineNumber(int l) { linenumber = l;}
    
    /**
     * Executes a function.
     * @param funcname The name of the function.
     * @param args The AST node representing the list of arguments of the caller.
     * @return The data returned by the function.
     */
    private HalObject executeCall(String funcname, HalMethod lambda, Arguments args) {
        HalObject f;
        HalObject instance;
        HalObject self = stack.getVariable("self");

        f = stack.getUnsafeVariable(funcname);
        instance = self;

        if(f == null) {
            f = self.getRecord().getUnsafeVariable(funcname);
            instance = self;

            if(f == null) {
                HalClass klass = self.getKlass();
                f = klass.getRecord().getUnsafeVariable(funcname);
                instance = klass;

                if(f == null) {
                    HalModule currentModule = stack.getCurrentModule();
                    f = currentModule.getRecord().getUnsafeVariable(funcname);
                    instance = currentModule;

                    if(f == null) {
                        args.prepend(new HalString(funcname));
                        return executeCall("__method_missing__", lambda, args);
                    }
                }
            }
        }

        return f.call(instance, lambda, args);
    }

    private HalObject getReference(String funcname) {
        HalObject f;
        HalObject self = stack.getVariable("self");

        try {
            f = stack.getVariable(funcname);
        } catch(NameException e) {
            try {
                f = self.getRecord().getVariable(funcname);
            } catch (NameException e2) {
                try {
                    f = self.getKlass().getRecord().getVariable(funcname);
                } catch(NameException e3) {
                    HalModule currentModule = stack.getCurrentModule();
                    f = currentModule.getRecord().getVariable(funcname);
                }
            }
        }

        return f;
    }

    public HalObject executeMethod(MethodDefinition def, HalTree block, HalObject instance, HalMethod lambda,
                                   Arguments args)
    {
        // Dumps trace information (function call and arguments)
        //if (trace != null) traceFunctionCall(tree, args);

        // Create the activation record in memory
        stack.pushContext(def.name, instance, def.module, def.getLocals(), lineNumber());
        calls++;

        if(def.klass != null) {
            ReferenceRecord parent = def.klass.getInstanceRecord().parent;

            if(parent != null) {
                try {
                    stack.defineVariable("super", parent.getVariable(def.name));
                } catch(NameException e) {
                    // No super
                }
            }
        }

        // Track line number
        setLineNumber(block);

        for(String arg : args.keys())
            stack.defineVariable(arg, args.get(arg));

        if(lambda != null)
            stack.defineVariable("yield", lambda);

        stack.defineVariable("block_given?", new HalBoolean(lambda != null));

        // Execute the instructions
        HalObject result = executeListInstructions(block);

        // If the result is null, then the function returns void
        if (result == null) result = new HalNone();

        // Dumps trace information
        //if (trace != null) traceReturn(tree, result, args);

        // Destroy the activation record
        stack.popContext();
        calls--;

        return result;
    }

    /**
     * Executes a block of instructions. The block is terminated
     * as soon as an instruction returns a non-null result.
     * Non-null results are only returned by "return" statements.
     * @param t The AST of the block of instructions.
     * @return The data returned by the instructions (null if no return
     * statement has been executed).
     */
    private HalObject executeListInstructions (HalTree t) {
        Reference result = stack.getReference("return");
        HalObject last = HalNone.NONE;

        int ninstr = t.getChildCount();
        for (int i = 0; i < ninstr; ++i) {
            last = executeInstruction (t.getChild(i));
            if (result.data != null) return result.data;
        }
        return last;
    }

    /**
     * Executes an instruction.
     * Non-null results are only returned by "return" statements.
     * @param t The AST of the instruction.
     * @return The data returned by the instruction. The data will be
     * non-null only if a return statement is executed or a block
     * of instructions executing a return.
     */
    private HalObject executeInstruction (HalTree t) {
        setLineNumber(t);
        HalObject value; // The returned value

        // A big switch for all type of instructions
        switch (t.getType()) {

            // Assignment
            case HalLexer.ASSIGN:
                return evaluateAssign(t);

            case HalLexer.EXPR:
                return evaluateExpression(t.getChild(0));

            // If-then-else
            case HalLexer.IF_STMT:
                value = evaluateExpression(t.getChild(0));
                if (value.toBoolean()) return executeListInstructions(t.getChild(1));
                // Is there else statement ?
                if (t.getChildCount() == 3) return executeListInstructions(t.getChild(2));
                return HalNone.NONE;

            // While
            case HalLexer.WHILE_STMT: {
                HalObject last = HalNone.NONE;
                while (true) {
                    value = evaluateExpression(t.getChild(0));
                    if(!value.toBoolean())
                        return last;
                    last = executeListInstructions(t.getChild(1));
                }
            }
                
            case HalLexer.FOR_STMT: {
                HalObject obj = evaluateExpression(t.getChild(0));
                return obj.methodcall_lambda("__each__", extractLambda(t.getChild(1)));
            }

            case HalLexer.CLASSDEF:
                return evaluateClassDefinition(t);

            // Function definition
            case HalLexer.FUNDEF:
                return evaluateMethodDefinition(t);

            case HalLexer.LAMBDACALL:
                HalTree left = t.getChild(0);
                HalLambda lambda = extractLambda(t.getChild(1));

                switch(left.getType()) {
                    case HalLexer.FUNCALL:
                        return executeCall(left.getChild(0).getText(), lambda, listArguments(left.getChild(1)));
                    case HalLexer.METHCALL:
                        return evaluateMethodCall(evaluateExpression(left.getChild(0)), left.getChild(1), lambda);
                    default:
                        throw new SyntaxException("Lambda call to literal");
                }
                
            // Return
            case HalLexer.RETURN:
                if(calls == 0)
                    throw new SyntaxException("return outside method");

                HalObject result;

                if (t.getChildCount() != 0)
                     result = evaluateExpression(t.getChild(0));
                else
                    result = new HalNone(); // No expression: returns void data

                stack.defineReturn(result);
                return result;

            case HalLexer.IMPORT_STMT:
                return evaluateImport(t);

            default: assert false; // Should never happen
        }

        // All possible instructions should have been treated.
        assert false;
        return null;
    }

    private HalObject evaluateAssign(HalTree t) {
        HalTree left = t.getChild(0);
        HalTree right = t.getChild(1);
        HalObject value;

        value = executeInstruction(right);

        switch(left.getType()) {
            case HalLexer.FUNCALL:
                if(left.getChild(1).getChildCount() > 0)
                    throw new TypeException("Method call in left hand assignation");

                String id = left.getChild(0).getText();
                stack.defineVariable(id, value);
                break;
            case HalLexer.GET_ITEM:
                HalObject d = evaluateExpression(left.getChild(0));
                d.methodcall("__setitem__", evaluateExpression(left.getChild(1)), value);
                break;
            case HalLexer.INSTANCE_VAR:
                stack.getVariable("self").getRecord().defineVariable(left.getChild(0).getText(), value);
                break;
            case HalLexer.KLASS_VAR:
                stack.getVariable("self").getKlass().getRecord().defineVariable(
                        left.getChild(0).getText(), value);
                break;
            case HalLexer.GLOBAL_VAR:
                globals.defineVariable(left.getChild(0).getText(), value);
                break;
            default:
                throw new TypeException("Impossible to assign to left expression");
        }

        return value;
    }

    /**
     * Evaluates the expression represented in the AST t.
     * @param t The AST of the expression
     * @return The value of the expression.
     */
    public HalObject evaluateExpression(HalTree t) {
        int previous_line = lineNumber();
        setLineNumber(t);
        int type = t.getType();

        HalObject value = null;
        // Atoms
        switch (type) {
            // An integer literal
            case HalLexer.INT:
                try {
                    value = new HalInteger(Integer.parseInt(t.getText()));
                } catch (NumberFormatException e) {
                    value = new HalLong(t.getText());
                }
                break;
            case HalLexer.FLOAT:
                value = new HalFloat(Double.parseDouble(t.getText()));
                break;
            // A Boolean literal
            case HalLexer.BOOLEAN:
                value = new HalBoolean(t.getBooleanValue());
                break;
            case HalLexer.NONE:
                value = HalNone.NONE;
                break;
            case HalLexer.STRING:
                value = new HalString(t.getText());
                break;
            case HalLexer.REGEXP:
                value = new HalRegExp(t.getText());
                break;
            case HalLexer.BACKTICKS:
                HalObject proc = HalProcess.klass.methodcall("__exec__", new HalString(t.getText()));
                value = proc.methodcall("output");
                break;
            case HalLexer.ARRAY:
                value = evaluateArray(t);
                break;
            case HalLexer.DICT:
                value = evaluateDict(t);
                break;
            case HalLexer.FUNCALL:
                value = executeCall(t.getChild(0).getText(), null, listArguments(t.getChild(1)));
                break;
            case HalLexer.INSTANCE_VAR:
                value = stack.getVariable("self").getRecord().getVariable(t.getChild(0).getText());
                break;
            case HalLexer.KLASS_VAR:
                value = stack.getVariable("self").getKlass().getRecord().getVariable(t.getChild(0).getText());
                break;
            case HalLexer.GLOBAL_VAR:
                value = globals.getVariable(t.getText());
                break;
            case HalLexer.REFERENCE_VAR:
                value = getReference(t.getChild(0).getText());
                break;
            case HalLexer.LIST_EXPR:
                HalObject obj = evaluateExpression(t.getChild(0));
                value = obj.methodcall_lambda("__map__", extractLambda(t.getChild(1)));
                break;
                
            default: break;
        }

        // Retrieve the original line and return
        if (value != null) {
            setLineNumber(previous_line);
            return value;
        }

        // Unary operators
        value = evaluateExpression(t.getChild(0));
        if (t.getChildCount() == 1) {
            switch (type) {
                case HalLexer.PLUS:
                    value = value.methodcall("__pos__");
                    break;
                case HalLexer.MINUS:
                    value = value.methodcall("__neg__");
                    break;
                case HalLexer.NOT:
                    value = value.methodcall("__not__");
                    break;
                default: assert false; // Should never happen
            }
            setLineNumber(previous_line);
            return value;
        }

        HalObject value2 = null;

        // Two operands
        switch(type) {
            // Boolean operators
            case HalLexer.AND:
            case HalLexer.OR:
                // The first operand is evaluated, but the second
                // is deferred (lazy, short-circuit evaluation).
                value2 = evaluateBoolean(type, value, t.getChild(1));
                break;

            case HalLexer.METHCALL:
                value2 = evaluateMethodCall(value, t.getChild(1), null);
                break;
        }

        if (value2 != null) {
            setLineNumber(previous_line);
            return value2;
        }

        value2 = evaluateExpression(t.getChild(1));
        switch (type) {
            // Relational operators
            case HalLexer.EQUAL:
                value = value.methodcall("__eq__", value2); break;
            case HalLexer.NOT_EQUAL:
                value = value.methodcall("__neq__", value2); break;
            case HalLexer.LT:
                value = value.methodcall("__lt__", value2); break;
            case HalLexer.LE:
                value = value.methodcall("__le__", value2); break;
            case HalLexer.GT:
                value = value.methodcall("__gt__", value2); break;
            case HalLexer.GE:
                value = value.methodcall("__ge__", value2); break;

            // Shift operators
            case HalLexer.LSHIFT:
                value = value.methodcall("__lshift__", value2); break;
            case HalLexer.RSHIFT:
                value = value.methodcall("__rshift__", value2); break;

            // Arithmetic operators
            case HalLexer.PLUS:
                value = value.methodcall("__add__", value2); break;
            case HalLexer.DOUBLE_PLUS:
                value = value.methodcall("__concat__", value2); break;
            case HalLexer.MINUS:
                value = value.methodcall("__sub__", value2); break;
            case HalLexer.MUL:
                value = value.methodcall("__mul__", value2); break;
            case HalLexer.POW:
                value = value.methodcall("__pow__", value2); break;
            case HalLexer.DIV:
                value = value.methodcall("__div__", value2); break;
            case HalLexer.DDIV:
                value = value.methodcall("__ddiv__", value2); break;
            case HalLexer.MOD:
                value = value.methodcall("__mod__", value2); break;

            // Additional operators
            case HalLexer.GET_ITEM:
                value = value.methodcall("__getitem__", value2); break;

            default: assert false; // Should never happen
        }

        setLineNumber(previous_line);
        return value;
    }

    private HalObject evaluateArray(HalTree t) {
        HalArray array = new HalArray();
        int n = t.getChildCount();

        for(int i = 0; i < n; ++i)
            array.methodcall("__append!__", evaluateExpression(t.getChild(i)));

        return array;
    }

    private HalObject evaluateDict(HalTree t) {
        HalDictionary dict = new HalDictionary();
        int n = t.getChildCount();

        for(int i = 0; i < n; ++i) {
            HalTree entry = t.getChild(i);
            dict.methodcall("__setitem__", evaluateExpression(entry.getChild(0)),
                    evaluateExpression(entry.getChild(1)));
        }

        return dict;
    }

    /**
     * Evaluation of Boolean expressions. This function implements
     * a short-circuit evaluation. The second operand is still a tree
     * and is only evaluated if the value of the expression cannot be
     * determined by the first operand.
     * @param type Type of operator (token).
     * @param v First operand.
     * @param t AST node of the second operand.
     * @return An Boolean data with the value of the expression.
     */
    private HalObject evaluateBoolean (int type, HalObject v, HalTree t) {
        // Boolean evaluation with short-circuit

        switch (type) {
            case HalLexer.AND:
                // Short circuit if v is false
                if (!v.toBoolean()) return v;
                break;

            case HalLexer.OR:
                // Short circuit if v is true
                if (v.toBoolean()) return v;
                break;

            default: assert false;
        }

        // Return the value of the second expression
        v = evaluateExpression(t);
        return v;
    }

    private HalObject evaluateMethodCall(HalObject obj, HalTree funcall, HalLambda lambda) {
        String name = funcall.getChild(0).getText();
        return obj.methodcall_lambda(name, lambda, listArguments(funcall.getChild(1)));
    }

    private HalObject evaluateClassDefinition(HalTree classdef) {
        String name = classdef.getChild(0).getText();
        HalTree inherit = classdef.getChild(1);
        HalTree block = classdef.getChild(2);
        HalObject self = stack.getVariable("self");
        HalObject klass;

        try {
            klass = self.getRecord().getVariable(name);

            if(inherit.getChildCount() != 0)
                throw new TypeException("Parent class can not be updated");
        } catch(NameException e) {
            HalClass parent;

            if(inherit.getChildCount() == 0)
                parent = HalObject.klass;
            else
                parent = (HalClass) evaluateExpression(inherit.getChild(0));

            klass = new HalClass(name, parent) {
                public HalObject newInstance(HalClass instklass) {
                    return parent.newInstance(instklass);
                }
            };

            self.getRecord().defineVariable(name, klass);
        }

        stack.pushContext(name, klass, classdef.getLine());

        HalObject result = executeListInstructions(block);

        stack.popContext();

        return result;
    }

    private HalObject evaluateMethodDefinition(HalTree fundef) {
        HalObject klass = stack.getVariable("self");
        String name = fundef.getChild(0).getText();
        Params.Param[] params = extractParams(fundef.getChild(1));
        MethodDefinition def = new MethodDefinition(stack.getCurrentModule(), klass, name, params);
        HalMethod method = new HalDefinedMethod(def, fundef.getChild(2));
        klass.getInstanceRecord().defineVariable(def.name, method);
        return method;
    }

    private Params.Param[] extractParams(HalTree tparams) {
        int nparams = tparams.getChildCount();
        Params.Param[] params = new Params.Param[nparams];

        for(int i = 0; i < nparams; ++i) {
            HalTree param = tparams.getChild(i);

            switch(param.getType()) {
                case HalLexer.PARAM_GROUP:
                    params[i] = new Params.ParamGroup(param.getChild(0).getText());
                    break;
                case HalLexer.KEYWORD:
                    params[i] = new Params.Keyword(param.getChild(0).getText(),
                            evaluateExpression(param.getChild(1)));
                    break;
                default:
                    params[i] = new Params.Param(param.getText());
                    break;
            }
        }

        return params;
    }

    private HalLambda extractLambda(HalTree tlambda) {
        return new HalLambda(new LambdaDefinition(stack.getCurrentModule(), stack.getCurrentRecord(),
                extractParams(tlambda.getChild(0))), tlambda.getChild(1));
    }

    private HalModule evaluateImport(HalTree imp) {
        HalTree mod = imp.getChild(0);

        HalPackage pkg = null;
        if(mod.getChildCount() > 0)
            pkg = evaluatePackage(mod.getChild(0));

        HalModule module = new HalModule(mod.getText(), pkg);

        try {
            CharStream moduleStream;

            try {
                moduleStream = new ANTLRFileStream(module.getPath());
            } catch (IOException e) {
                String moduleFullPath = new File(
                        new File(
                                new File(Hal.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent(),
                                "lib"),
                        module.getPath()
                ).toString();

                moduleStream = new ANTLRFileStream(moduleFullPath);
                module.setFullPath(moduleFullPath);
            }

            HalTree tree = parser.getTree(moduleStream);

            stack.pushContext(module.value, module, module, null, imp.getLine());
            evaluate(tree);
            stack.popContext();
        } catch(IOException e) {
            // Load native modules
            ClassLoader classLoader = Hal.class.getClassLoader();

            try {
                Class moduleClass = classLoader.loadClass("hal.interpreter.modules." + module.getAddress());
                module = (HalModule) moduleClass.getConstructor(HalPackage.class).newInstance(pkg);
                module.setFullPath(moduleClass.getProtectionDomain().getCodeSource().getLocation().getPath());
            } catch(ClassNotFoundException e2) {
                throw new RuntimeException("Import error: Module '" + module.getAddress() + "' not found");
            } catch (InvocationTargetException e1) {
                e1.printStackTrace();
            } catch (NoSuchMethodException e1) {
                e1.printStackTrace();
            } catch (InstantiationException e1) {
                e1.printStackTrace();
            } catch (IllegalAccessException e1) {
                e1.printStackTrace();
            }
        }

        ReferenceRecord importRecord;
        try {
            importRecord = stack.getVariable("self").getInstanceRecord();
        } catch(TypeException e) {
            importRecord = stack.getCurrentRecord();
        }

        int n = imp.getChildCount();
        if(n > 1) {
            for(int i = 1; i < n; ++i) {
                String name = imp.getChild(i).getText();
                importRecord.defineVariable(name, module.getRecord().getVariable(name));
            }
        }
        else
            importRecord.defineVariable(module.root.value, module.root);

        return module;
    }

    private HalPackage evaluatePackage(HalTree pkg) {
        HalPackage parent = null;

        if(pkg.getChildCount() > 0)
            parent = evaluatePackage(pkg.getChild(0));

        return new HalPackage(pkg.getText(), parent);
    }

    /**
     * Gathers the list of arguments of a function call. It also checks
     * that the arguments are compatible with the parameters. In particular,
     * it checks that the number of parameters is the same and that no
     * expressions are passed as parametres by reference.
     * @param targs The AST of the list of arguments passed by the caller.
     * @return The list of evaluated arguments.
     */

    private Arguments listArguments(HalTree targs) {
        setLineNumber(targs);

        // Create the list of parameters
        final Arguments args = new Arguments();
        int n = targs.getChildCount();

        for(int i = 0; i < n; ++i) {
            HalTree arg = targs.getChild(i);
            setLineNumber(arg);

            switch (arg.getType()) {
                case HalLexer.FLATTEN_ARG:
                    HalObject toFlat = evaluateExpression(arg.getChild(0));
                    toFlat.methodcall_lambda("__each__", new InternalLambda(new Params.Param("value")) {
                        @Override
                        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments fargs) {
                            HalObject value = fargs.get("value");
                            args.append(value);
                            return value;
                        }
                    });
                    break;
                case HalLexer.KEYWORD:
                    args.put(arg.getChild(0).getText(), evaluateExpression(arg.getChild(1)));
                    break;
                default:
                    args.append(evaluateExpression(arg));
                    break;
            }
        }

        return args;
    }

    /**
     * Writes trace information of a function call in the trace file.
     * The information is the name of the function, the value of the
     * parameters and the line number where the function call is produced.
     * @param f AST of the function
     * @param args Record with the arguments
     */
    private void traceFunctionCall(HalTree f, ReferenceRecord args) {
        function_nesting++;
        HalTree params = f.getChild(1);
        int nargs = params.getChildCount();
        
        for (int i=0; i < function_nesting; ++i) trace.print("|   ");

        // Print function name and parameters
        trace.print(f.getChild(0) + "(");
        for (int i = 0; i < nargs; ++i) {
            if (i > 0) trace.print(", ");
            HalTree p = params.getChild(i);
            trace.print(p.getText() + "=" + args.getVariable(p.getText()));
        }
        trace.print(") ");
        
        if (function_nesting == 0) trace.println("<entry point>");
        else trace.println("<line " + lineNumber() + ">");
    }

    /**
     * Writes the trace information about the return of a function.
     * The information is the value of the returned value and of the
     * variables passed by reference. It also reports the line number
     * of the return.
     * @param f AST of the function
     * @param result The value of the result
     * @param args Record with the arguments
     */
    private void traceReturn(HalTree f, HalObject result, ReferenceRecord args) {
        for (int i=0; i < function_nesting; ++i) trace.print("|   ");
        function_nesting--;
        trace.print("return");
        if (result.getValue() != null) trace.print(" " + result);
        
        // Print the value of arguments passed by reference
        HalTree params = f.getChild(1);
        int nargs = params.getChildCount();
        for (int i = 0; i < nargs; ++i) {
            HalTree p = params.getChild(i);
            trace.print(", " + p.getText() + "=" + args.getVariable(p.getText()));
        }
        
        trace.println(" <line " + lineNumber() + ">");
        if (function_nesting < 0) trace.close();
    }
}
