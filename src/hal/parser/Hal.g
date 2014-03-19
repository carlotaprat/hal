grammar Hal;

options
{
    output=AST;
}

tokens
{
    PARAMS;
    BLOCK;
    FUNDEF;
    FUNCALL;
    ARGS;
    IF_STMT;
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
    private boolean firstLine = true;
    int[] indentStack = new int[MAX_INDENTS];
    java.util.Queue<Token> tokens = new java.util.LinkedList<Token>();

    {
        // Compute first line indentation manually
        int i = 1;

        while(input.LA(i) == ' ')
            i++;

        int next = input.LA(i);

        // Ignore empty lines
        if(i > 1 && next != '\n' && next != '\r' && next != -1)
        {
            jump(Indent);
            indentStack[indentLevel] = i-1;
        }
    }

    @Override
    public void emit(Token t)
    {
        state.token = t;
        tokens.offer(t);

        //System.err.println("=> " + t);
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
                return nextToken();
            }
            
            // End file with new line
            emit(new CommonToken(NEWLINE, ""));
            emit(Token.EOF_TOKEN);
        }

        Token t = tokens.poll();

        System.err.println("<= " + t);
        return t;
    }

    private void jump(int ttype)
    {
        String name;
        if(ttype == Indent)
        {
            name = "Indentation";
            indentLevel++;
        }
        else
        {
            name = "Dedentation";
            indentLevel--;
        }

        emit(new CommonToken(ttype, name + " (level=" + indentLevel + ")"));
    }
}

parse
    :   (NEWLINE | stmt)* EOF -> ^(BLOCK stmt*)
    ;

stmt
    :   simple_stmt
    |   compound_stmt   
    ;

simple_stmt
    :   small_stmt (options {greedy=true;}:SEMICOLON! small_stmt)* (SEMICOLON!)? NEWLINE!
    ;

small_stmt
    :   funcall
    ;

compound_stmt
    :   if_statement
    |   fundef
    ;

if_statement
    :   IF if_body -> ^(IF_STMT if_body)
    ;

if_body
    :   test COLON! multiline_block if_extension?
    ;

if_extension
    :   ELIF if_body -> ^(BLOCK ^(IF_STMT if_body))
    |   ELSE! COLON! multiline_block
    ;

block
    :   simple_stmt -> ^(BLOCK simple_stmt)
    |   multiline_block
    ;

multiline_block
    :   NEWLINE Indent (stmt)+ Dedent -> ^(BLOCK (stmt)+)
    ;

fundef
    :   DEF Id params COLON block -> ^(FUNDEF Id params block)
    ;

// The list of parameters grouped in a subtree (it can be empty)
params
    : ('(' paramlist? ')' | paramlist?) -> ^(PARAMS paramlist?)
    ;

// Parameters are separated by commas
paramlist
    : Id (','! Id)*
    ;

funcall
    : Id params -> ^(FUNCALL Id params)
    ;

// args
//     : ( '(' paramlist? ')' | paramlist?) -> ^(ARGS paramlist?)
//     ;

// expr_list
//     : test (','! test)*
//     ;

test
    :   Id
    ;

NEWLINE
    @init
    {
        int n = 0;
    }
    : NL (' ' {n++;} | '\t' {n += 8; n -= (n \% 8); })*
    {
        emit(new CommonToken(NEWLINE, ""));

        int next = input.LA(1);
        int currentIndent = indentStack[indentLevel];

        // Skip if same indentation or empty line
        if(n == currentIndent)
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
            while(indentLevel > 0 && indentStack[indentLevel] > n)
            {
                jump(Dedent);
            }

            if(indentStack[indentLevel] != n)
                throw new RuntimeException("Unexpected indentation.");
        }
    }
    ;

STMTEND : SEMICOLON NEWLINE | NEWLINE+;

IF      : 'if';
ELIF    : 'elif';
ELSE    : 'else';
DEF     : 'def';
COLON   : ':' ;
SEMICOLON : ';';

Id
    : ('a'..'z' | 'A'..'Z' | '0'..'9')+
    ;

SpaceChars
    : SP {skip();}
    ;


fragment NL     : (('\r')? '\n')+;
fragment SP     : (' ' | '\t')+;
fragment Indent : ;
fragment Dedent : ;
