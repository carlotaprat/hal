package hal;

// Imports for ANTLR
import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
import org.antlr.stringtemplate.*;

// Imports from Java
import org.apache.commons.cli.*; // Command Language Interface
import java.io.*;

// Parser and Interpreter
import hal.parser.*;
import hal.interpreter.*;

/**
 * The class <code>Hal</code> implement the main function of the
 * interpreter. It accepts a set of options to generate the AST in
 * dot format and avoid the execution of the program. To know about
 * the accepted options, run the command hal -help.
 */

public class Hal{

    /** The file name of the program. */
    private static String infile = null;
    /** Name of the file representing the AST. */
    private static String astfile = null;
    /** Flag indicating that the AST must be written in dot format. */
    private static boolean dotformat = false;
    /** Name of the file storing the trace of the program. */
    private static String tracefile = null;
    /** Flag to indicate whether the program must be executed after parsing. */
    private static boolean execute = true;
      
    /** Main program that invokes the parser and the interpreter. */
    
    public static void main(String[] args) throws Exception {
        // Parser for command line options
        if (!readOptions (args)) System.exit(1);

        // Parsing of the input file
        
        CharStream input = null;
        try {
            input = new ANTLRFileStream(infile);
        } catch (IOException e) {
            System.err.println ("Error: file " + infile + " could not be opened.");
            System.exit(1);
        }

        // Creates the lexer
        HalLexer lex = new HalLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lex);

        // Creates and runs the parser. As a result, an AST is created
        HalParser parser = new HalParser(tokens);
        HalTreeAdaptor adaptor = new HalTreeAdaptor();
        parser.setTreeAdaptor(adaptor);
        HalParser.prog_return result = null;
        try {
            result = parser.prog();
        } catch (Exception e) {} // Just catch the exception (nothing to do)
        
        // Check for parsing errors
        int nerrors = parser.getNumberOfSyntaxErrors();
        if (nerrors > 0) {
            System.err.println (nerrors + " errors detected. " +
                                "The program has not been executed.");
            System.exit(1);
        }

        // Get the AST
        HalTree t = (HalTree)result.getTree();

        // Generate a file for the AST (option -ast file)
        if (astfile != null) {
            File ast = new File(astfile);
            BufferedWriter output = new BufferedWriter(new FileWriter(ast));
            if (dotformat) {
                DOTTreeGenerator gen = new DOTTreeGenerator();
                output.write(gen.toDOT(t).toString());
            } else {
                output.write(t.toStringTree());
            }
            output.close();
        }

        // Start interpretation (only if execution required)
        if (execute) {
            // Creates and prepares the interpreter
            Interpreter I = null;
            int linenumber = -1;
            try {
                I = new Interpreter(t, tracefile); // prepares the interpreter
                I.Run();                  // Executes the code
            } catch (RuntimeException e) {
                if (I != null) linenumber = I.lineNumber();
                System.err.print (e.getClass().getSimpleName());
                if (linenumber < 0) System.err.print (": ");
                else System.err.print (" (" + infile + ", line " + linenumber + "): ");
                System.err.println (e.getMessage() + ".");
                System.err.format (I.getStackTrace());
            } catch (StackOverflowError e) {
                if (I != null) linenumber = I.lineNumber();
                System.err.print("Stack overflow error");
                if (linenumber < 0) System.err.print (".");
                else System.err.println (" (" + infile + ", line " + linenumber + ").");
                System.err.format (I.getStackTrace(5));
            }
        }
    }

    /**
     * Function to parse the command line. It defines some of
     * the attributes of the class. It returns true if the parsing
     * hass been successful, and false otherwise.
     */

    private static boolean readOptions(String[] args) {
        // Define the options
        Option help = new Option("help", "print this message");
        Option noexec = new Option("noexec", "do not execute the program");
        Option dot = new Option("dot", "dump the AST in dot format");
        Option ast = OptionBuilder
                        .withArgName ("file")
                        .hasArg()
                        .withDescription ("write the AST")
                        .create ("ast");
        Option trace = OptionBuilder
                        .withArgName ("file")
                        .hasArg()
                        .withDescription ("write a trace of function calls during the execution of the program")
                        .create ("trace");
                                       
        Options options = new Options();
        options.addOption(help);
        options.addOption(dot);
        options.addOption(ast);
        options.addOption(trace);
        options.addOption(noexec);
        CommandLineParser clp = new GnuParser();
        CommandLine line = null;

        String cmdline = "hal [options] file";
        
        
        // Parse the options
        try {
            line = clp.parse (options, args);
        }
        catch (ParseException exp) {
            System.err.println ("Incorrect command line: " + exp.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp (cmdline, options);
            return false;
        }

        // Option -help
        if (line.hasOption ("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp (cmdline, options);
            return false;
        }
        
        // Option -dot
        if (line.hasOption ("dot")) dotformat = true;

        // Option -ast dotfile
        if (line.hasOption ("ast")) astfile = line.getOptionValue ("ast");
        
        // Option -trace dotfile
        if (line.hasOption ("trace")) tracefile = line.getOptionValue ("trace");
        
        // Option -noexec
        if (line.hasOption ("noexec")) execute = false;

        // Remaining arguments (the input file)
        String[] files = line.getArgs();
        if (files.length != 1) {
            System.err.println ("Incorrect command line.");
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp (cmdline, options);
            return false;
        }
        
        infile = files[0];
        return true;
    }
}

