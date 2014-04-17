package hal.interpreter;

import hal.interpreter.core.*;
import hal.interpreter.exceptions.*;
import hal.interpreter.types.enumerable.*;
import hal.interpreter.types.numeric.*;
import hal.interpreter.types.*;
import hal.parser.*;

import java.io.*;

/** Class that implements the interpreter of the language. */

public class Interpreter
{
    /** Memory of the virtual machine. */
    private Stack Stack;
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
    public Interpreter(PrintWriter tracefile) {
        // Initialize and solve dependency cycles
        HalKernel.init();

        Stack = new Stack(new HalModule("main")); // Creates the memory of the virtual machine
        globals = new ReferenceRecord("globals", null);

        trace = tracefile;
        function_nesting = 0;
    }

    /** Runs the program by calling the main function without parameters. */
    public HalObject Run(HalTree t) {
        try {
            Stack.popUntilFirstLevel();
            PreProcessAST(t); // Some internal pre-processing on the AST
            return executeListInstructions(t);
        } catch(ClassCastException e) {
            throw TypeException.fromCastException(e);
        }
    }

    /** Returns the contents of the stack trace */
    public String getStackTrace() {
        return Stack.getStackTrace(lineNumber());
    }

    /** Returns a summarized contents of the stack trace */
    public String getStackTrace(int nitems) {
        return Stack.getStackTrace(lineNumber(), nitems);
    }

    /**
     * Performs some pre-processing on the AST. Basically, it
     * calculates the value of the literals and stores a simpler
     * representation. See HalTree.java for details.
     */
    private void PreProcessAST(HalTree T) {
        if (T == null) return;
        switch(T.getType()) {
            case HalLexer.INT: T.setIntValue(); break;
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
        HalObject self = Stack.getVariable("self");

        try {
            f = Stack.getVariable(funcname);
            instance = self;
        } catch(NameException e) {
            try {
                f = self.getRecord().getVariable(funcname);
                instance = self;
            } catch (NameException e2) {
                HalClass klass = self.getKlass();
                f = klass.getRecord().getVariable(funcname);
                instance = klass;
            }
        }

        return f.call(instance, lambda, listArguments(args));
    }

    public HalObject executeMethod(MethodDefinition def, HalObject instance, HalObject lambda,
                                   HalObject... args)
    {
        HalTree tree = def.tree;

        // Dumps trace information (function call and arguments)
        if (trace != null) traceFunctionCall(tree, args);

        // List of parameters of the callee
        HalTree p = def.params;
        int nparam = p.getChildCount(); // Number of parameters

        if(nparam != args.length)
            throw new InvalidArgumentsException();

        // Create the activation record in memory
        Stack.pushContext(def.name, instance, def.getLocals(), lineNumber());
        calls++;

        HalObject superkw;
        try {
            superkw = instance.getKlass().getInstanceRecord().parent.getVariable(def.name);
        } catch(NameException e) {
            superkw = HalNone.NONE;
        }

        Stack.defineVariable("super", superkw);

        // Track line number
        setLineNumber(tree);

        // Copy the parameters to the record activation record
        for (int i = 0; i < nparam; ++i) {
            String param_name = p.getChild(i).getText();
            Stack.defineVariable(param_name, args[i]);
        }

        if(lambda != null)
            Stack.defineVariable("yield", lambda);

        // Execute the instructions
        HalObject result = executeListInstructions(def.block);

        // If the result is null, then the function returns void
        if (result == null) result = new HalNone();

        // Dumps trace information
        if (trace != null) traceReturn(tree, result, args);

        // Destroy the activation record
        Stack.popContext();
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
        Reference result = Stack.getReference("return");
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
                HalLambda lambda = new HalLambda(new Lambda(t.getChild(1), Stack.getCurrentRecord()));
                HalObject obj = evaluateExpression(t.getChild(0));
                return obj.methodcall("__each__", lambda);
            }

            case HalLexer.CLASSDEF:
                return evaluateClassDefinition(t);

            // Function definition
            case HalLexer.FUNDEF:
                return evaluateMethodDefinition(t);

            case HalLexer.LAMBDACALL:
                HalTree left = t.getChild(0);
                HalLambda lambda = new HalLambda(new Lambda(t.getChild(1), Stack.getCurrentRecord()));

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

                Stack.defineVariable("return", result);
                return result;

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

        if(right.getType() == HalLexer.EXPR)
            value = evaluateExpression(right.getChild(0));
        else
            value = evaluateAssign(right);

        switch(left.getType()) {
            case HalLexer.FUNCALL:
                if(left.getChild(1).getChildCount() > 0)
                    throw new TypeException("Method call in left hand assignation");

                String id = left.getChild(0).getText();
                Stack.defineVariable(id, value);
                break;
            case HalLexer.GET_ITEM:
                HalObject d = evaluateExpression(left.getChild(0));
                d.methodcall("__setitem__", evaluateExpression(left.getChild(1)), value);
                break;
            case HalLexer.INSTANCE_VAR:
                Stack.getVariable("self").getRecord().defineVariable(left.getChild(0).getText(), value);
                break;
            case HalLexer.KLASS_VAR:
                Stack.getVariable("self").getKlass().getRecord().defineVariable(
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
                value = new HalInteger(t.getIntValue());
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
                value = Stack.getVariable("self").getRecord().getVariable(t.getChild(0).getText());
                break;
            case HalLexer.KLASS_VAR:
                value = Stack.getVariable("self").getKlass().getRecord().getVariable(t.getChild(0).getText());
                break;
            case HalLexer.GLOBAL_VAR:
                value = globals.getVariable(t.getText());
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

            // Arithmetic operators
            case HalLexer.PLUS:
                value = value.methodcall("__add__", value2); break;
            case HalLexer.MINUS:
                value = value.methodcall("__sub__", value2); break;
            case HalLexer.MUL:
                value = value.methodcall("__mul__", value2); break;
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
        return obj.methodcall(name, lambda, listArguments(funcall.getChild(1)));
    }

    private HalObject evaluateClassDefinition(HalTree classdef) {
        String name = classdef.getChild(0).getText();
        HalTree inherit = classdef.getChild(1);
        HalTree block = classdef.getChild(2);
        HalObject self = Stack.getVariable("self");
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

        Stack.pushContext(name, klass, classdef.getLine());

        HalObject result = executeListInstructions(block);

        Stack.popContext();

        return result;
    }

    private HalObject evaluateMethodDefinition(HalTree fundef) {
        HalObject klass = Stack.getVariable("self");
        MethodDefinition def = new MethodDefinition(fundef);
        HalMethod method = new HalMethod(def);
        klass.getInstanceRecord().defineVariable(def.name, method);
        return method;
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
        HalObject[] Params = new HalObject[n];

        // Checks the compatibility of the parameters passed by
        // reference and calculates the values and references of
        // the parameters.
        for (int i = 0; i < n; ++i) {
            HalTree a = args.getChild(i); // Arguments passed by the caller
            setLineNumber(a);
            // Pass by value: evaluate the expression
            Params[i] = evaluateExpression(a);
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
