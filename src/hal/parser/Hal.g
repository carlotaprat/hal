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
    FOR_STMT;
    WHILE_STMT;
    ASSIGN;
    BOOLEAN;
    LIST;
    LAMBDA;
}

@header
{
    package hal.parser;
}

@lexer::header {
    package hal.parser;
}

// INDENTATION
@lexer::members
{
    public static final int MAX_INDENTS = 100;
    private int indentLevel = 0;
    private boolean end = false;
    int[] indentStack = new int[MAX_INDENTS];
    java.util.Queue<Token> tokens =
        new java.util.LinkedList<Token>();

    {
        // Compute first line indentation manually
        int i = 1;
        while(input.LA(i) == ' ') i++;

        int next = input.LA(i);

        // Ignore empty lines
        if(i > 1 && next != '\n' && next != '\r' && next != -1) {
            jump(Indent);
            indentStack[indentLevel] = i-1;
        }
    }

    @Override
    public void emit(Token t) {
        state.token = t;
        tokens.offer(t);
        System.err.println("=> " + t);
    }

    @Override
    public Token nextToken() {
        super.nextToken();

        if(tokens.isEmpty()) {
            // End file with new line
            if(!end) {
                emit(new CommonToken(NEWLINE, "\\n"));
                end = true;
            }

            // Undo all indentation
            if(indentLevel > 0) {
                jump(Dedent);
                return nextToken();
            }

            emit(Token.EOF_TOKEN);
        }
        Token t = tokens.poll();
        System.err.println("<= " + t);
        return t;
    }


    private void jump(int ttype) {
        String name;
        if(ttype == Indent) {
            name = "Indentation";
            indentLevel++;
        }
        else {
            name = "Dedentation";
            indentLevel--;
        }
        emit(new CommonToken(ttype, name));
    }
}
// END INDENTATION

@parser::members {
  public boolean space(TokenStream input) {
    return !directlyFollows(input.LT(-1), input.LT(1));
  }

  private boolean directlyFollows(Token first, Token second) {
    CommonToken firstT = (CommonToken) first;
    CommonToken secondT = (CommonToken) second;

    if (firstT.getStopIndex() + 1 != secondT.getStartIndex())
      return false;

    return true;
  }
}

// GRAMMAR

prog
    :   (stmt)* EOF -> ^(BLOCK stmt*)
    ;

stmt
    :   simple_stmt[true]
    |   compound_stmt
    |   NEWLINE!
    ;

simple_stmt[boolean can_lambda]
    @init{boolean conditional = false;}
    :   s1=small_stmt[can_lambda] ({!$s1.has_lambda}?=> (
                (options {greedy=true;}:SEMICOLON small_stmt[false])* SEMICOLON?
            |   IF {conditional=true;} expr[false] (ELSE s2=small_stmt[false])?
        ) NEWLINE)?
        -> {conditional}? ^(IF_STMT expr ^(BLOCK $s1) ^(BLOCK $s2))
        -> small_stmt+
    ;

small_stmt[boolean can_lambda] returns [boolean has_lambda]
    @init{$has_lambda=false;}
    : assign
    | (e=expr[can_lambda] {$has_lambda=$e.has_lambda;})
    ;

compound_stmt
    :   if_stmt
    |   for_stmt
    |   while_stmt
    |   fundef
    ;

if_stmt
    :   IF if_body -> ^(IF_STMT if_body)
    ;

if_body
    :   expr[false] COLON! block if_extension?
    ;

if_extension
    :   ELIF if_body -> ^(BLOCK ^(IF_STMT if_body))
    |   ELSE! COLON! block
    ;

for_stmt
    :  FOR paramlist IN expr[false] COLON block
        -> ^(FOR_STMT ^(PARAMS paramlist) expr block)
    ;

while_stmt
    :  WHILE expr[false] COLON block -> ^(WHILE_STMT expr block)
    ;

block
    :   simple_stmt[false] -> ^(BLOCK simple_stmt)
    |   multiline_block
    ;

multiline_block
    :   NEWLINE Indent (stmt)+ Dedent -> ^(BLOCK (stmt)+)
    ;

fundef
    :   DEF ID params COLON block -> ^(FUNDEF ID params block)
    ;

params
    :   paramlist? -> ^(PARAMS paramlist?)
    ;

paramlist
    :   ID (','! ID)*
    ;

funcall[boolean can_lambda] returns [boolean has_lambda]
    @init{$has_lambda=false;}
    :   ID args ({can_lambda}?=> lambda {$has_lambda=true;})? -> ^(FUNCALL ID args lambda?)
    ;

args
    :   arglist? -> ^(ARGS arglist?)
    ;

arglist
    :   {space(input) && (!input.LT(1).getText().equals("-") ||
                directlyFollows(input.LT(1), input.LT(2)))}?
        expr[false] (options {greedy=true;}: ','! expr[false])*
    ;

lambda
    :
        (AS paramlist)? COLON block -> ^(LAMBDA ^(PARAMS paramlist?) block)
    ;


// Assignment
assign
    :	ID eq=EQUAL expr[true] -> ^(ASSIGN[$eq,":="] ID expr)
    ;

expr[boolean can_lambda] returns [boolean has_lambda]
    :   e1=boolterm[can_lambda] {$has_lambda=$e1.has_lambda;}
        ({!$has_lambda}?=> (options {greedy=true;}: OR^ boolterm[false])*)?
    ;

boolterm[boolean can_lambda] returns [boolean has_lambda]
    :   b1=boolfact[can_lambda] {$has_lambda=$b1.has_lambda;}
        ({!$has_lambda}?=> (options {greedy=true;}: AND^ boolfact[false])*)?
    ;

boolfact[boolean can_lambda] returns [boolean has_lambda]
    :   f1=num_expr[can_lambda] {$has_lambda=$f1.has_lambda;}
        (options {greedy=true;}: {!$has_lambda}?=>
            (DOUBLE_EQUAL^ | NOT_EQUAL^ | LT^ | LE^ | GT^ | GE^) num_expr[false])?
    ;

num_expr[boolean can_lambda] returns [boolean has_lambda]
    :   n1=term[can_lambda] {$has_lambda=$n1.has_lambda;}
        ({!$has_lambda}?=> (options {greedy=true;}: (PLUS^ | MINUS^) term[false])*)?
    ;

term[boolean can_lambda] returns [boolean has_lambda]
    :   t1=factor[can_lambda] {$has_lambda=$t1.has_lambda;}
        ({!$has_lambda}?=> (options {greedy=true;}: (MUL^ | DIV^ | MOD^) factor[false])*)?
    ;

factor[boolean can_lambda] returns [boolean has_lambda]
    :   NOT^ a=atom[can_lambda] {$has_lambda=$a.has_lambda;}
    |   MINUS a=atom[can_lambda] {$has_lambda=$a.has_lambda;} -> ^(MINUS["NEGATE"] atom)
    |   a=atom[can_lambda] {$has_lambda=$a.has_lambda;}
    ;

atom[boolean can_lambda] returns [boolean has_lambda]
    @init{$has_lambda=false;}
    :   INT
    |   (b=TRUE | b=FALSE)  -> ^(BOOLEAN[$b,$b.text])
    |   list
    |   (f=funcall[can_lambda] {$has_lambda=$f.has_lambda;})
    |   LPAREN! expr[false] RPAREN!
    ;

list
    :  LBRACK (expr[false] (',' expr[false])*)? RBRACK -> ^(LIST expr*)
    ;

// LEXICAL RULES

// OPERATORS
EQUAL	: '=' ;
DOUBLE_EQUAL : '==';
NOT_EQUAL: '!=' ;
LT      : '<' ;
LE      : '<=';
GT      : '>';
GE      : '>=';
PLUS    : '+' ;
MINUS   : '-' ;
MUL     : '*';
DIV     : '/';
MOD     : '%' ;
// KEYWORDS
NOT     : 'not';
AND     : 'and' ;
OR      : 'or' ;
TRUE    : 'true';
FALSE   : 'false';
IF      : 'if';
ELIF    : 'elif';
ELSE    : 'else';
FOR     : 'for';
WHILE   : 'while';
IN      : 'in';
DEF     : 'def';
AS      : 'as';
// SPECIAL SYMBOLS
COLON   : ':' ;
SEMICOLON : ';';
LPAREN  : '(';
RPAREN  : ')';
LBRACK  : '[';
RBRACK  : ']';

// Useful fragments
fragment DIGIT : ('0'..'9');
fragment LOWER : ('a'..'z');
fragment UPPER : ('A'..'Z');
fragment LETTER: (LOWER|UPPER);
fragment NL     : (('\r')? '\n')+;
fragment SP     : (' ' | '\t')+;

// Identifiers
ID  : (LETTER|'_') (LETTER|'_'|DIGIT)* ('!'|'?')?;

// Integers
INT : (DIGIT)+;

WS
    : SP {skip();}
    ;

COMMENT: '#' ~('\n'|'\r')* {skip();};

NEWLINE
    @init
    {
        int n = 0;
    }
    : NL (' ' {n++;} | '\t' {n += 8; n -= (n \% 8); })*
    {
        emit(new CommonToken(NEWLINE, "\\n"));

        int next = input.LA(1);
        int currentIndent = indentStack[indentLevel];

        // Skip if same indentation or empty line
        if(n == currentIndent || next == '\n' || next == '\r' || next == -1)
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

fragment Indent : ;
fragment Dedent : ;
