grammar Hal;

options
{
    output=AST;
}

tokens
{
    BLOCK;
}

@header
{
    package hal.parser;
}

@lexer::header {
    package hal.parser;
}

@lexer::members
{
    public static final int MAX_INDENTS = 100;
    private int indentLevel = 0;
    int[] indentStack = new int[MAX_INDENTS];
    java.util.Queue<Token> tokens = new java.util.LinkedList<Token>();

    {
        // Initial block indentation
        jump(Indent);
    }

    @Override
    public void emit(Token t)
    {
        state.token = t;
        tokens.offer(t);
    }

    @Override
    public Token nextToken()
    {
        super.nextToken();

        if(tokens.isEmpty())
        {
            // Undo all indentation
            if(indentLevel > 0)
            {
                jump(Dedent);
                return tokens.poll();
            }

            return Token.EOF_TOKEN;
        }
        
        return tokens.poll();
    }

    private void jump(int ttype)
    {
        indentLevel += (ttype == Dedent ? -1 : 1);
        emit(new CommonToken(ttype, "level=" + indentLevel));
    }
}

parse
    : block EOF -> block
    ;

block
    : Indent block_atoms Dedent -> ^(BLOCK block_atoms)
    ;

block_atoms
    :  (Id | block)+
    ;

NewLine
    @init
    {
        int n = 0;
    }
    : NL (' ' {n++;} | '\t' {n += 8; n -= (n \% 8); })*
    {
        int next = input.LA(1);
        int currentIndent = indentStack[indentLevel];

        // Skip if same indentation or empty line
        if(n == currentIndent || next == '\r' || next == '\n' || next == -1)
        {
            skip();
        }
        else if(n > currentIndent)
        {
            jump(Indent);
            indentStack[indentLevel] = n;
        }
        else
        {
            while(indentLevel >= 0 && indentStack[indentLevel] > n)
                jump(Dedent);

            if(indentStack[indentLevel] != n)
                throw new RuntimeException("Unexpected indentation.");

            indentStack[indentLevel] = n;
        }
    }
    ;

Id
    : ('a'..'z' | 'A'..'Z')+
    ;

SpaceChars
    : SP {skip();}
    ;

fragment NL     : '\r'? '\n' | '\r';
fragment SP     : (' ' | '\t')+;
fragment Indent : ;
fragment Dedent : ;
