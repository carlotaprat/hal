import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
import org.antlr.stringtemplate.*;
import hal.parser.*;

public class Hal
{
    public static void main(String[] args) throws Exception
    {
        HalLexer lexer = new HalLexer(new ANTLRInputStream(System.in));
        HalParser parser = new HalParser(new CommonTokenStream(lexer));

        try
        {
            CommonTree tree = (CommonTree)parser.prog().getTree();
            DOTTreeGenerator gen = new DOTTreeGenerator();
            StringTemplate st = gen.toDOT(tree);
            System.out.println(st);
        }
        catch(RuntimeException e)
        {
            System.err.println("parse error: " + e.getMessage());
        }
  }
}
