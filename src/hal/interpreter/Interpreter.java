package hal.interpreter;

import hal.interpreter.core.InternalLambda;
import hal.interpreter.core.LambdaDefinition;
import hal.interpreter.core.MethodDefinition;
import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.exceptions.InvalidArgumentsException;
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

import java.io.IOException;
import java.io.PrintWriter;

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
    public Interpreter(Parser parsr, PrintWriter tracefile) {
        // Initialize and solve dependency cycles
        HalKernel.init();

        parser = parsr;
        HalModule mainModule = new HalModule("main", null);
        globals = new ReferenceRecord("globals", null);
        trace = tracefile;
        function_nesting = 0;

        stack = new Stack(); // Creates the memory of the virtual machine
        stack.pushContext(mainModule.value, mainModule, mainModule, null, 0);
    }

    /** Runs the program by calling the main function without parameters. */
    public HalObject run(CharStream input) throws IOException {
        stack.popUntilFirstLevel();
        HalTree t = parser.process(input);
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

        return current.getFullPath();
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
            case HalLexer.STRING: T.setStringValue(); break;
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
    private HalObject executeCall(String funcname, HalObject lambda, HalTree args) {
        HalObject f;
        HalObject instance;
        HalObject self = stack.getVariable("self");

        try {
            f = stack.getVariable(funcname);
            instance = self;
        } catch(NameException e) {
            try {
                f = self.getRecord().getVariable(funcname);
                instance = self;
            } catch (NameException e2) {
                try {
                    HalClass klass = self.getKlass();
                    f = klass.getRecord().getVariable(funcname);
                    instance = klass;
                } catch(NameException e3) {
                    HalModule currentModule = stack.getCurrentModule();
                    f = currentModule.getRecord().getVariable(funcname);
                    instance = currentModule;
                }
            }
        }

        return f.call(instance, lambda, listArguments(args));
    }

    public HalObject executeMethod(MethodDefinition def, HalObject instance, HalObject lambda, HalObject... args) {
        HalTree tree = def.tree;

        // Dumps trace information (function call and arguments)
        if (trace != null) traceFunctionCall(tree, args);

        // List of parameters of the callee
        MethodDefinition.Params params = def.params;
        int num_pos_params = params.positional_params.size();

        if(num_pos_params > args.length || (num_pos_params < args.length && params.group_params == null))
            throw new InvalidArgumentsException();

        // Create the activation record in memory
        stack.pushContext(def.name, instance, def.module, def.getLocals(), lineNumber());
        calls++;

        HalObject superkw;
        try {
            superkw = instance.getKlass().getInstanceRecord().parent.getVariable(def.name);
        } catch(NameException e) {
            superkw = HalNone.NONE;
        }

        stack.defineVariable("super", superkw);

        // Track line number
        setLineNumber(tree);

        // Copy the parameters to the record activation record
        for(int i = 0; i < params.before_group; ++i)
            stack.defineVariable(params.positional_params.get(i), args[i]);

        for(int i = 0; i < params.after_group; ++i)
            stack.defineVariable(params.positional_params.get(num_pos_params-i-1), args[args.length-i-1]);

        if(params.group_params != null) {
            HalArray group = new HalArray();

            int last_group_param = args.length - num_pos_params + params.before_group;
            for(int i = params.before_group; i < last_group_param; ++i)
                group.methodcall("__append!__", args[i]);

            stack.defineVariable(params.group_params, group);
        }

        if(lambda != null)
            stack.defineVariable("yield", lambda);

        stack.defineVariable("block_given?", new HalBoolean(lambda != null));

        // Execute the instructions
        HalObject result = executeListInstructions(def.block);

        // If the result is null, then the function returns void
        if (result == null) result = new HalNone();

        // Dumps trace information
        if (trace != null) traceReturn(tree, result, args);

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
        assert t != null;
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
        assert t != null;

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
                HalLambda lambda = new HalLambda(new LambdaDefinition(stack.getCurrentModule(), t.getChild(1),
                        stack.getCurrentRecord()));
                HalObject obj = evaluateExpression(t.getChild(0));
                return obj.methodcall_lambda("__each__", lambda);
            }

            case HalLexer.CLASSDEF:
                return evaluateClassDefinition(t);

            // Function definition
            case HalLexer.FUNDEF:
                return evaluateMethodDefinition(t);

            case HalLexer.LAMBDACALL:
                HalTree left = t.getChild(0);
                HalLambda lambda = new HalLambda(new LambdaDefinition(stack.getCurrentModule(), t.getChild(1),
                        stack.getCurrentRecord()));

                switch(left.getType()) {
                    case HalLexer.FUNCALL:
                        return executeCall(left.getChild(0).getText(), lambda, left.getChild(1));
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

                stack.defineVariable("return", result);
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
        assert t != null;

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
                value = new HalFloat(Float.parseFloat(t.getText()));
                break;
            // A Boolean literal
            case HalLexer.BOOLEAN:
                value = new HalBoolean(t.getBooleanValue());
                break;
            case HalLexer.NONE:
                value = HalNone.NONE;
                break;
            case HalLexer.STRING:
                value = new HalString(t.getStringValue());
                break;
            case HalLexer.ARRAY:
                value = evaluateArray(t);
                break;
            case HalLexer.DICT:
                value = evaluateDict(t);
                break;
            case HalLexer.FUNCALL:
                value = executeCall(t.getChild(0).getText(), null, t.getChild(1));
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
            case HalLexer.LIST_EXPR:
                HalLambda lambda = new HalLambda(new LambdaDefinition(stack.getCurrentModule(), t.getChild(1),
                        stack.getCurrentRecord()));
                HalObject obj = evaluateExpression(t.getChild(0));
                value = obj.methodcall_lambda("__map__", lambda);
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

            klass = new HalClass(name, parent);
            self.getRecord().defineVariable(name, klass);
        }

        stack.pushContext(name, klass, classdef.getLine());

        HalObject result = executeListInstructions(block);

        stack.popContext();

        return result;
    }

    private HalObject evaluateMethodDefinition(HalTree fundef) {
        HalObject klass = stack.getVariable("self");
        MethodDefinition def = new MethodDefinition(stack.getCurrentModule(), fundef);
        HalMethod method = new HalMethod(def);
        klass.getInstanceRecord().defineVariable(def.name, method);
        return method;
    }

    private HalModule evaluateImport(HalTree imp) {
        HalTree mod = imp.getChild(0);

        HalPackage pkg = null;
        if(mod.getChildCount() > 0)
            pkg = evaluatePackage(mod.getChild(0));

        HalModule module = new HalModule(mod.getText(), pkg);
        CharStream modfile;

        try {
            modfile = new ANTLRFileStream(module.getFullPath());
        } catch(IOException e) {
            throw new RuntimeException("Import error: " + e.getMessage());
        }

        HalTree tree = parser.getTree(modfile);

        stack.pushContext(module.value, module, module, null, imp.getLine());
        evaluate(tree);
        stack.popContext();

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
     * @param args The AST of the list of arguments passed by the caller.
     * @return The list of evaluated arguments.
     */

    private HalObject[] listArguments(HalTree args) {
        setLineNumber(args);

        // Create the list of parameters
        int n = args.getChildCount();
        HalObject[] arg_expr = new HalObject[n];
        int total_args = 0;

        for(int i = 0; i < n; ++i) {
            HalTree arg = args.getChild(i);

            if(arg.getType() == HalLexer.FLATTEN_ARG) {
                HalObject items = evaluateExpression(arg.getChild(0));
                arg_expr[i] = items;
                total_args += ((HalInteger) items.methodcall("__size__")).value;
            } else {
                arg_expr[i] = evaluateExpression(arg);
                total_args++;
            }
        }

        final HalObject[] Params = new HalObject[total_args];
        final int[] current = {0};

        for (int i = 0; i < n; ++i) {
            HalTree a = args.getChild(i); // Arguments passed by the caller
            setLineNumber(a);

            if(a.getType() == HalLexer.FLATTEN_ARG) {
                arg_expr[i].methodcall_lambda("__each__", new InternalLambda() {
                    @Override
                    public HalObject call(HalObject instance, HalObject lambda, HalObject... args) {
                        if (args.length < 1)
                            throw new InvalidArgumentsException();

                        HalObject value = args[args.length - 1];
                        Params[current[0]++] = value;
                        return value;
                    }
                });
            } else {
                Params[current[0]++] = arg_expr[i];
            }
        }

        return Params;
    }

    /**
     * Writes trace information of a function call in the trace file.
     * The information is the name of the function, the value of the
     * parameters and the line number where the function call is produced.
     * @param f AST of the function
     * @param arg_values Values of the parameters passed to the function
     */
    private void traceFunctionCall(HalTree f, HalObject... arg_values) {
        function_nesting++;
        HalTree params = f.getChild(1);
        int nargs = params.getChildCount();
        
        for (int i=0; i < function_nesting; ++i) trace.print("|   ");

        // Print function name and parameters
        trace.print(f.getChild(0) + "(");
        for (int i = 0; i < nargs; ++i) {
            if (i > 0) trace.print(", ");
            HalTree p = params.getChild(i);
            trace.print(p.getText() + "=" + arg_values[i]);
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
     * @param arg_values The value of the parameters passed to the function
     */
    private void traceReturn(HalTree f, HalObject result, HalObject... arg_values) {
        for (int i=0; i < function_nesting; ++i) trace.print("|   ");
        function_nesting--;
        trace.print("return");
        if (result.getValue() != null) trace.print(" " + result);
        
        // Print the value of arguments passed by reference
        HalTree params = f.getChild(1);
        int nargs = params.getChildCount();
        for (int i = 0; i < nargs; ++i) {
            HalTree p = params.getChild(i);
            trace.print(", " + p.getText() + "=" + arg_values[i]);
        }
        
        trace.println(" <line " + lineNumber() + ">");
        if (function_nesting < 0) trace.close();
    }
}
