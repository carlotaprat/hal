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
  public boolean before(int before, int type) {
    int i = 1;
    Token t;

    do {
        t = input.LT(i);
        i++;

        if(t.getType() == type)
            return true;
    } while(t.getType() != EOF && t.getType() != before);

    return false;
  }

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
    :   simple_stmt
    |   compound_stmt
    |   NEWLINE!
    ;

simple_stmt
    @init{boolean conditional = false;}
    :   s1=small_stmt (
                (options {greedy=true;}:SEMICOLON small_stmt)* SEMICOLON?
            |   IF {conditional=true;} expr (ELSE s2=small_stmt)?
        )
        NEWLINE
        -> {conditional}? ^(IF_STMT expr ^(BLOCK $s1) ^(BLOCK $s2))
        -> small_stmt+
    ;

small_stmt
    @init{boolean assign=false;}
    :   expr (EQUAL^ expr)*
    ;

compound_stmt
    :   if_stmt
    |   for_stmt
    |   while_stmt
    |   fundef
    |   do_lambda
    |   assign_lambda
    ;

if_stmt
    :   IF if_body -> ^(IF_STMT if_body)
    ;

if_body
    :   expr COLON! block if_extension?
    ;

if_extension
    :   ELIF if_body -> ^(BLOCK ^(IF_STMT if_body))
    |   ELSE! COLON! block
    ;

for_stmt
    :  FOR paramlist IN expr COLON block
        -> ^(FOR_STMT ^(PARAMS paramlist) expr block)
    ;

while_stmt
    :  WHILE expr COLON block -> ^(WHILE_STMT expr block)
    ;

block
    :   simple_stmt -> ^(BLOCK simple_stmt)
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

funcall
    :   ID args -> ^(FUNCALL ID args)
    ;

args
    :   arglist? -> ^(ARGS arglist?)
    ;

arglist
    :   {space(input) && (!input.LT(1).getText().equals("-") ||
                directlyFollows(input.LT(1), input.LT(2)))}?
        expr (options {greedy=true;}: ','! expr)*
    ;

do_lambda
    :   {input.LT(1).getType()==ID && before(NEWLINE, COLON) && !before(COLON, EQUAL)}?
        ID args lambda -> ^(FUNCALL ID args lambda)
    ;

assign_lambda
    :   {before(NEWLINE, COLON) && before(COLON, EQUAL)}?
        expr eq=EQUAL do_lambda -> ^(ASSIGN[$eq, ":="] expr do_lambda)
    ;

lambda
    :
        (LKW paramlist)? COLON block -> ^(LAMBDA ^(PARAMS paramlist?) block)
    ;

expr
    :   boolterm (options {greedy=true;}: OR^ boolterm)*
    ;

boolterm
    :   boolfact (options {greedy=true;}: AND^ boolfact)*
    ;

boolfact
    :   num_expr (options {greedy=true;}:
            (DOUBLE_EQUAL^ | NOT_EQUAL^ | LT^ | LE^ | GT^ | GE^) num_expr)?
    ;

num_expr
    :   term (options {greedy=true;}: (PLUS^ | MINUS^) term)*
    ;

term
    :   factor (options {greedy=true;}: (MUL^ | DIV^ | MOD^) factor)*
    ;

factor
    :   NOT^ atom
    |   MINUS atom -> ^(MINUS["NEGATE"] atom)
    |   atom
    ;

atom
    :   INT
    |   (b=TRUE | b=FALSE)  -> ^(BOOLEAN[$b,$b.text])
    |   list
    |   funcall // An ID can be considered a "funcall" with 0 args
    |   LPAREN! expr RPAREN!
    ;

list
    :   LBRACK (expr (',' expr)*)? RBRACK -> ^(LIST expr*)
    ;

access
    :   LBRACK! expr RBRACK!
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
LKW     : 'as';
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
