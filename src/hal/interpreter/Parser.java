package hal.interpreter;

import hal.parser.HalLexer;
import hal.parser.HalParser;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.DOTTreeGenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class Parser {
    private File astfile;
    private boolean dotformat;

    public Parser(File file, boolean dot) {
        astfile = file;
        dotformat = dot;
    }

    public HalTree process(CharStream source) throws IOException {
        HalTree tree = getTree(source);

        if(astfile != null)
            writeASTFile(tree);

        return tree;
    }

    public HalTree getTree(CharStream source) {
        // Creates the lexer
        HalLexer lex = new HalLexer(source);
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
        int nerrors = parser.getNumberOfSyntaxErrors() + lex.getNumberOfSyntaxErrors();
        if (nerrors > 0)
            throw new RuntimeException(nerrors + " syntax error" + (nerrors > 1 ? "s" : ""));

        return (HalTree)result.getTree();
    }

    private void writeASTFile(HalTree t) throws IOException {
        BufferedWriter output = new BufferedWriter(new FileWriter(astfile, true));
        if (dotformat) {
            DOTTreeGenerator gen = new DOTTreeGenerator();
            output.write(gen.toDOT(t).toString());
        } else {
            output.write(t.toStringTree());
        }
        output.close();
    }
}
