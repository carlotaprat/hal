package hal;

// Imports for ANTLR
import hal.interpreter.Interpreter;
import hal.interpreter.Parser;
import hal.interpreter.types.HalObject;
import jline.console.ConsoleReader;
import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

// Imports from Java
// Parser and Interpreter
// Interactive mode

/**
 * The class <code>Hal</code> implement the main function of the
 * interpreter. It accepts a set of options to generate the AST in
 * dot format and avoid the execution of the program. To know about
 * the accepted options, run the command hal -help.
 */

public class Hal
{
    public static final String VERSION = "0.0.2@$GIT";
    public static final String DATE = "$DATE";

    public static Interpreter INTERPRETER = null;
    /** The file name of the program. */
    private static String infile = null;
    private static File astfile = null;
    /** Flag indicating that the AST must be written in dot format. */
    private static boolean dotformat = false;
    /** Name of the file storing the trace of the program. */
    private static PrintWriter tracefile = null;
    /** Flag to indicate whether the program must be executed after parsing. */
    private static boolean execute = true;
    /** Flag to indicate whether the interpreter works in interactive mode. */
    private static boolean interactive = false;
      
    /** Main program that invokes the parser and the interpreter. */
    
    public static void main(String[] args) throws Exception {
        try {
            // Parser for command line options
            if (!readOptions (args))
                System.exit(1);

            Parser parser = new Parser(astfile, dotformat);
            INTERPRETER = new Interpreter(parser, tracefile); // prepares the interpreter

            if(interactive)
                interactiveMode();
            else
                fileMode();
        } catch (IOException e) {
            System.err.println ("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void interactiveMode() throws IOException {
        System.out.println("Hal " + VERSION + " (" + DATE + ") [java "
                + System.getProperty("java.version") + "]");

        ConsoleReader console = new ConsoleReader();
        console.setExpandEvents(false);
        String input;
        while(true) {
            console.setPrompt(">>> ");
            input = console.readLine();

            if(input == null) {
                System.out.println();
                break;
            }

            if(input.endsWith(":") || input.endsWith(",") || input.endsWith("[") ||
                input.endsWith("{")) {
                String block;
                do {
                    console.setPrompt("... ");
                    block = console.readLine();

                    if(block == null) {
                        System.out.println();
                        break;
                    }

                    input += "\n" + block;
                } while(!block.equals(""));
            }

            if(input.equals("quit"))
                break;

            try {
                HalObject d = evaluate(new ANTLRStringStream(input));

                if (d != null)
                    System.out.println("=> " + d.methodcall("__repr__"));
            } catch(Throwable e) {
                handleException(e);
            }
        }
    }

    private static void fileMode() throws IOException {
        CharStream input;

        try {
            input = new ANTLRFileStream(infile);
            evaluate(input);
        } catch(RuntimeException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private static HalObject evaluate(CharStream input) {
        if(!execute)
            return null;

        try {
            return INTERPRETER.run(input);
        } catch(Throwable e) {
            handleException(e);
        }

        return null;
    }

    private static void handleException(Throwable ex) {
        System.err.println(ex.getClass().getSimpleName() + ": " + ex.getMessage());

        if(ex instanceof StackOverflowError)
            System.err.format(INTERPRETER.getStackTrace(5));
        else
            System.err.format(INTERPRETER.getStackTrace());

        if(ex instanceof NullPointerException || ex instanceof AssertionError)
            ex.printStackTrace();
    }

    /**
     * Function to parse the command line. It defines some of
     * the attributes of the class. It returns true if the parsing
     * hass been successful, and false otherwise.
     */

    private static boolean readOptions(String[] args) throws IOException {
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
        if (line.hasOption ("ast")) {
            astfile = new File(line.getOptionValue ("ast"));
            astfile.delete();
        }
        
        // Option -trace dotfile
        if (line.hasOption ("trace")) {
            File tracef = new File(line.getOptionValue("trace"));
            tracef.delete();
            tracefile = new PrintWriter(new FileWriter(tracef, true));
        }
        
        // Option -noexec
        if (line.hasOption ("noexec")) execute = false;

        // Remaining arguments (the input file)
        String[] files = line.getArgs();

        switch(files.length) {
            case 0:
                interactive = true;
                infile = "stdin";
                break;
            case 1:
                infile = files[0];
                break;
            default:
                System.err.println ("Incorrect command line.");
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp (cmdline, options);
                return false;
        }

        return true;
    }
}

