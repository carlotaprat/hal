package hal.interpreter;

import hal.interpreter.core.MethodDefinition;
import hal.interpreter.datatypes.*;
import hal.interpreter.exceptions.AttributeException;
import hal.interpreter.exceptions.SyntaxException;
import hal.interpreter.exceptions.TypeException;
import hal.parser.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.io.*;

/** Class that implements the interpreter of the language. */

public class Interpreter {

    /** Memory of the virtual machine. */
    private Stack Stack;

    /**
     * Map between function names (keys) and ASTs (values).
     * Each entry of the map stores the root of the AST
     * correponding to the function.
     */
    private HashMap<String,HalTree> FuncName2Tree;

    /** Standard input of the interpreter (System.in). */
    private Scanner stdin;

    /**
     * Stores the line number of the current statement.
     * The line number is used to report runtime errors.
     */
    private int linenumber = -1;

    /** File to write the trace of function calls. */
    private PrintWriter trace = null;

    /** Nested levels of function calls. */
    private int function_nesting = 0;
    
    /**
     * Constructor of the interpreter. It prepares the main
     * data structures for the execution of the main program.
     */
    public Interpreter(PrintWriter tracefile) {
        Stack = new Stack(); // Creates the memory of the virtual machine
        Stack.pushActivationRecord("Base", 0);
        // Initializes the standard input of the program
        stdin = new Scanner (new BufferedReader(new InputStreamReader(System.in)));
        trace = tracefile;
        function_nesting = 0;
    }

    /** Runs the program by calling the main function without parameters. */
    public DataType Run(HalTree t) {
        PreProcessAST(t); // Some internal pre-processing on the AST
        return executeListInstructions(t);
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
     * Gets the current line number. In case of a runtime error,
     * it returns the line number of the statement causing the
     * error.
     */
    public int lineNumber() { return linenumber; }

    /** Defines the current line number associated to an AST node. */
    private void setLineNumber(HalTree t) { linenumber = t.getLine();}

    /** Defines the current line number with a specific value */
    private void setLineNumber(int l) { linenumber = l;}
    
    /**
     * Executes a function.
     * @param funcname The name of the function.
     * @param args The AST node representing the list of arguments of the caller.
     * @return The data returned by the function.
     */
    private DataType executeCall(String funcname, HalTree args) {
        // Get the AST of the function
        DataType f = Stack.getVariable(funcname);
        return f.call(this, args);
    }

    public DataType executeMethod(MethodDefinition def, HalTree args)
    {
        HalTree tree = def.tree;

        // Gather the list of arguments of the caller. This function
        // performs all the checks required for the compatibility of
        // parameters.
        ArrayList<Reference> Arg_values = listArguments(tree, args);

        // Dumps trace information (function call and arguments)
        if (trace != null) traceFunctionCall(tree, Arg_values);

        // List of parameters of the callee
        HalTree p = def.params;
        int nparam = p.getChildCount(); // Number of parameters

        // Create the activation record in memory
        Stack.pushActivationRecord(def.name, lineNumber());

        // Track line number
        setLineNumber(tree);

        // Copy the parameters to the current activation record
        for (int i = 0; i < nparam; ++i) {
            String param_name = p.getChild(i).getText();
            Stack.defineReference(param_name, Arg_values.get(i));
        }

        // Execute the instructions
        DataType result = executeListInstructions(def.block);

        // If the result is null, then the function returns void
        if (result == null) result = new HalNone();

        // Dumps trace information
        if (trace != null) traceReturn(tree, result, Arg_values);

        // Destroy the activation record
        Stack.popActivationRecord();

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
    private DataType executeListInstructions (HalTree t) {
        assert t != null;
        Reference result = Stack.getReference("return");
        DataType last = null;

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
    private DataType executeInstruction (HalTree t) {
        assert t != null;
        
        setLineNumber(t);
        DataType value; // The returned value

        // A big switch for all type of instructions
        switch (t.getType()) {

            // Assignment
            case HalLexer.ASSIGN:
                return evaluateAssign(t);

            case HalLexer.EXPR:
                return evaluateExpression(t.getChild(0));

            // If-then-else
            case HalLexer.IF:
                value = evaluateExpression(t.getChild(0));
                if (value.toBoolean()) return executeListInstructions(t.getChild(1));
                // Is there else statement ?
                if (t.getChildCount() == 3) return executeListInstructions(t.getChild(2));
                return null;

            // While
            case HalLexer.WHILE_STMT:
                DataType last = new HalNone();
                while (true) {
                    value = evaluateExpression(t.getChild(0));
                    if(!value.toBoolean())
                        return last;
                    last = executeListInstructions(t.getChild(1));
                }

            // Return
            case HalLexer.RETURN:
                if(function_nesting == 0)
                    throw new SyntaxException("return outside method");

                DataType result;

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

    private DataType evaluateAssign(HalTree t) {
        HalTree left = t.getChild(0);
        DataType value = evaluateExpression(t.getChild(1).getChild(0));

        switch(left.getType()) {
            case HalLexer.FUNCALL:
                if(left.getChild(1).getChildCount() > 0)
                    throw new TypeException("Funcall with arguments in left hand assignation.");

                String id = left.getChild(0).getText();
                Stack.defineVariable(id, value);
                break;
            case HalLexer.GET_ITEM:
                DataType d = evaluateExpression(left.getChild(0));
                d.__setitem__(evaluateExpression(left.getChild(1)), value);
                break;
            default:
                throw new TypeException("Impossible to assign to left expression.");
        }

        return value;
    }

    /**
     * Evaluates the expression represented in the AST t.
     * @param t The AST of the expression
     * @return The value of the expression.
     */
    private DataType evaluateExpression(HalTree t) {
        assert t != null;

        int previous_line = lineNumber();
        setLineNumber(t);
        int type = t.getType();

        DataType value = null;
        // Atoms
        switch (type) {
            // A variable
            case HalLexer.ID:
                value = Stack.getVariable(t.getText());
                break;
            // An integer literal
            case HalLexer.INT:
                value = new HalInteger(t.getIntValue());
                break;
            // A Boolean literal
            case HalLexer.BOOLEAN:
                value = new HalBoolean(t.getBooleanValue());
                break;
            case HalLexer.STRING:
                value = new HalString(t.getStringValue());
                break;
            case HalLexer.ARRAY:
                value = evaluateArray(t);
                break;
            // A function call. Checks that the function returns a result.
            case HalLexer.FUNCALL:
                value = executeCall(t.getChild(0).getText(), t.getChild(1));
                assert value != null;
                if (value.getValue() == null) {
                    throw new RuntimeException ("function expected to return a value");
                }
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
                    value = value.__pos__();
                    break;
                case HalLexer.MINUS:
                    value = value.__neg__();
                    break;
                case HalLexer.NOT:
                    value = value.__not__();
                    break;
                default: assert false; // Should never happen
            }
            setLineNumber(previous_line);
            return value;
        }

        DataType value2 = null;

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
                value2 = evaluateMethodCall(value, t.getChild(1));
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
                value = value.__eq__(value2); break;
            case HalLexer.NOT_EQUAL:
                value = value.__neq__(value2); break;
            case HalLexer.LT:
                value = value.__lt__(value2); break;
            case HalLexer.LE:
                value = value.__le__(value2); break;
            case HalLexer.GT:
                value = value.__gt__(value2); break;
            case HalLexer.GE:
                value = value.__ge__(value2); break;

            // Arithmetic operators
            case HalLexer.PLUS:
                value = value.__add__(value2); break;
            case HalLexer.MINUS:
                value = value.__sub__(value2); break;
            case HalLexer.MUL:
                value = value.__mul__(value2); break;
            case HalLexer.DIV:
                value = value.__div__(value2); break;
            case HalLexer.MOD:
                value = value.__mod__(value2); break;

            // Additional operators
            case HalLexer.GET_ITEM:
                value = value.__getitem__(value2); break;

            default: assert false; // Should never happen
        }
        
        setLineNumber(previous_line);
        return value;
    }

    private DataType evaluateArray(HalTree t) {
        HalArray value = new HalArray();
        int n = t.getChildCount();

        for(int i = 0; i < n; ++i)
            value.__append__(evaluateExpression(t.getChild(i)));

        return value;
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
    private DataType evaluateBoolean (int type, DataType v, HalTree t) {
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

    private DataType evaluateMethodCall(DataType obj, HalTree funcall) {
        String id = funcall.getChild(0).getText();
        HalTree arglist = funcall.getChild(1);
        int n = arglist.getChildCount();

        Class<?>[] param_types = new Class<?>[n];
        Arrays.fill(param_types, DataType.class);

        Object[] args = new Object[n];
        for(int i = 0; i < n; ++i) {
            args[i] = evaluateExpression(arglist.getChild(i));
        }

        try {
            return (DataType) obj.getClass().getMethod("__" + id + "__", param_types).invoke(
                    obj, args);
        } catch (IllegalAccessException e) {
            throw new TypeException("Illegal access to method.");
        } catch (InvocationTargetException e) {
            throw new TypeException("Number of arguments did not match.");
        } catch (NoSuchMethodException e) {
            throw new AttributeException("Undefined method: " + id);
        }
    }

    /**
     * Gathers the list of arguments of a function call. It also checks
     * that the arguments are compatible with the parameters. In particular,
     * it checks that the number of parameters is the same and that no
     * expressions are passed as parametres by reference.
     * @param args The AST of the list of arguments passed by the caller.
     * @return The list of evaluated arguments.
     */
     
    private ArrayList<Reference> listArguments (HalTree AstF, HalTree args) {
        setLineNumber(args);
        HalTree pars = AstF.getChild(1);   // Parameters of the function

        // Create the list of parameters
        ArrayList<Reference> Params = new ArrayList<Reference> ();
        int n = pars.getChildCount();

        // Checks the compatibility of the parameters passed by
        // reference and calculates the values and references of
        // the parameters.
        for (int i = 0; i < n; ++i) {
            HalTree p = pars.getChild(i); // Parameters of the callee
            HalTree a = args.getChild(i); // Arguments passed by the caller
            setLineNumber(a);
            // Pass by value: evaluate the expression
            Params.set(i, new Reference(evaluateExpression(a)));
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
    private void traceFunctionCall(HalTree f, ArrayList<Reference> arg_values) {
        function_nesting++;
        HalTree params = f.getChild(1);
        int nargs = params.getChildCount();
        
        for (int i=0; i < function_nesting; ++i) trace.print("|   ");

        // Print function name and parameters
        trace.print(f.getChild(0) + "(");
        for (int i = 0; i < nargs; ++i) {
            if (i > 0) trace.print(", ");
            HalTree p = params.getChild(i);
            trace.print(p.getText() + "=" + arg_values.get(i));
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
    private void traceReturn(HalTree f, DataType result, ArrayList<Reference> arg_values) {
        for (int i=0; i < function_nesting; ++i) trace.print("|   ");
        function_nesting--;
        trace.print("return");
        if (result.getValue() != null) trace.print(" " + result);
        
        // Print the value of arguments passed by reference
        HalTree params = f.getChild(1);
        int nargs = params.getChildCount();
        for (int i = 0; i < nargs; ++i) {
            HalTree p = params.getChild(i);
            trace.print(", " + p.getText() + "=" + arg_values.get(i));
        }
        
        trace.println(" <line " + lineNumber() + ">");
        if (function_nesting < 0) trace.close();
    }
}
