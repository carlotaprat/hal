grammar Hal;

options
{
    output=AST;
}

tokens
{
    PARAMS;
    BLOCK;
    CLASSDEF;
    PARENT;
    FUNDEF;
    FUNCALL;
    LAMBDACALL;
    ARGS;
    IF_STMT;
    FOR_STMT;
    WHILE_STMT;
    BOOLEAN;
    ARRAY;
    LAMBDA;
    ACCESS;
    GET_ITEM;
    METHCALL; // Science, bitch!
    EXPR;
    DICT;
    PAIR;
    GLOBAL_VAR;
    INSTANCE_VAR;
    KLASS_VAR;
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
        //System.err.println("=> " + t);
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
        //System.err.println("<= " + t);
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

  public boolean directlyNext(int type) {
    if(input.LT(1).getType() != type)
      return false;

    return directlyFollows(input.LT(-1), input.LT(1));
  }

  public boolean nextIs(int... types) {
    int type = input.LT(1).getType();

    for(int i = 0; i < types.length; ++i) {
        if(types[i] == type)
            return true;
    }

    return false;
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
        -> {conditional}? ^(IF_STMT expr ^(BLOCK $s1) ^(BLOCK $s2)?)
        -> small_stmt+
    ;

small_stmt
    :   assign_expr
    |   r=RETURN expr -> ^(RETURN[$r, "RETURN"] expr)
    ;

assign_expr
    :   expr (a=ASSIGN assign_expr)? // Right-associative
        -> {a==null}? ^(EXPR expr)
        -> ^(ASSIGN expr assign_expr)
    ;

compound_stmt
    :   if_stmt
    |   for_stmt
    |   while_stmt
    |   classdef
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

classdef
    :   CLASS ID ('<<' expr)? COLON block -> ^(CLASSDEF ID ^(PARENT expr?) block)
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
    :   {directlyNext(LPAREN)}?=> LPAREN arglist? RPAREN -> ^(ARGS arglist?)
        | space_arglist? -> ^(ARGS space_arglist?)
    ;

space_arglist
    :   {space(input) && (!input.LT(1).getText().equals("-") ||
            directlyFollows(input.LT(1), input.LT(2)))}?
        arglist
    ;

arglist
    :  expr (options {greedy=true;}: ','! expr)*
    ;

do_lambda
    :   {!nextIs(FOR, WHILE, IF, ELSE, ELIF, DEF, CLASS)
        && before(NEWLINE, COLON) && !before(COLON, EQUAL)}?
        methcalls lambda -> ^(LAMBDACALL methcalls lambda)
    ;

methcalls
    :   (atom -> atom) ('.' f=funcall -> ^(METHCALL $methcalls $f))*
    ;

assign_lambda
    :   {before(NEWLINE, COLON) && before(COLON, EQUAL)}?
        expr EQUAL^ do_lambda
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
            (EQUAL^ | NOT_EQUAL^ | LT^ | LE^ | GT^ | GE^) num_expr)?
    ;

num_expr
    :   term (options {greedy=true;}: (PLUS^ | MINUS^) term)*
    ;

term
    :   factor (options {greedy=true;}: (MUL^ | DIV^ | DDIV^ | MOD^) factor)*
    ;

factor
    :   NOT^ item
    |   MINUS item -> ^(MINUS["NEGATE"] item)
    |   item
    ;

item
    :   (atom -> atom) (options {greedy=true;}:
            a=access -> ^(GET_ITEM $item $a)
        |   ('.' f=funcall -> ^(METHCALL $item $f))
        )*
    ;

atom
    :   INT
    |   FLOAT
    |   STRING
    |   (b=TRUE | b=FALSE)  -> ^(BOOLEAN[$b,$b.text])
    |   NONE
    |   global_var
    |   instance_var
    |   klass_var
    |   list
    |   dict
    |   funcall // An ID can be considered a "funcall" with 0 args
    |   LPAREN! expr RPAREN!
    ;

global_var
    :   DOLLAR ID -> ^(GLOBAL_VAR ID)
    ;

instance_var
    :   AT ID -> ^(INSTANCE_VAR ID)
    ;

klass_var
    :   DOUBLE_AT ID -> ^(KLASS_VAR ID)
    ;

list
    :   LBRACK (expr (',' expr)*)? RBRACK -> ^(ARRAY expr*)
    ;

dict
    :   LBRACE (entry (',' entry)*)? RBRACE -> ^(DICT entry*)
    ;

entry
    :   expr LARROW expr -> ^(PAIR expr expr)
    ;

access
    :   {directlyNext(LBRACK)}?
        LBRACK! expr RBRACK!
    ;

// LEXICAL RULES

// OPERATORS
EQUAL   : '==';
ASSIGN	: '=' ;
NOT_EQUAL: '!=' ;
LT      : '<' ;
LE      : '<=';
GT      : '>';
GE      : '>=';
PLUS    : '+' ;
MINUS   : '-' ;
MUL     : '*';
DIV     : '/';
DDIV    : '//';
MOD     : '%' ;
// KEYWORDS
NOT     : 'not';
AND     : 'and' ;
OR      : 'or' ;
TRUE    : 'true';
FALSE   : 'false';
NONE    : 'none';
IF      : 'if';
ELIF    : 'elif';
ELSE    : 'else';
FOR     : 'for';
WHILE   : 'while';
IN      : 'in';
DEF     : 'def';
LKW     : 'with';
RETURN  : 'return';
CLASS   : 'class';
// SPECIAL SYMBOLS
COLON     : ':' ;
SEMICOLON : ';';
LPAREN  : '(';
RPAREN  : ')';
LBRACK  : '[';
RBRACK  : ']';
LBRACE  : '{';
RBRACE  : '}';
LARROW  : '=>';
AT      : '@';
DOUBLE_AT : '@@';
DOLLAR    : '$';

// Useful fragments
fragment DIGIT : ('0'..'9');
fragment LOWER : ('a'..'z');
fragment UPPER : ('A'..'Z');
fragment LETTER: (LOWER|UPPER);
fragment NL     : (('\r')? '\n')+;
fragment SP     : (' ' | '\t')+;

// Identifiers
ID  : (LETTER|'_') (LETTER|'_'|DIGIT)* (('!'|'?')('_')*)?;

// Integers
INT : (DIGIT+ (('.' DIGIT)=> '.' DIGIT+ {$type=FLOAT;})?);
fragment FLOAT  :;

// Strings
STRING  :  '"' ( ESC_SEQ | ~('\\'|'"') )* '"'
        ;

fragment ESC_SEQ
        :   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
        ;

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
