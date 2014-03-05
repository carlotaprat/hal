import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
import org.antlr.stringtemplate.*;

public class Main {
  public static void main(String[] args) throws Exception {
    PyEsqueLexer lexer = new PyEsqueLexer(new ANTLRFileStream("in.txt"));
    PyEsqueParser parser = new PyEsqueParser(new CommonTokenStream(lexer));
    CommonTree tree = (CommonTree)parser.parse().getTree();
    DOTTreeGenerator gen = new DOTTreeGenerator();
    StringTemplate st = gen.toDOT(tree);
    System.out.println(st);
  }
}

