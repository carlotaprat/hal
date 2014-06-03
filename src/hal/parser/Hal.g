grammar Hal;

options
{
    output=AST;
}

tokens
{
    PARAMS;
    PARAM_GROUP;
    BLOCK;
    CLASSDEF;
    PARENT;
    FUNDEF;
    FUNCALL;
    LAMBDACALL;
    ARGS;
    FLATTEN_ARG;
    KEYWORD;
    IF_STMT;
    FOR_STMT;
    WHILE_STMT;
    IMPORT_STMT;
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
    REFERENCE_VAR;
    LIST_EXPR;
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

  public boolean keywordIsNext() {
    return input.LT(1).getType() == ID
        && input.LT(2).getType() == LARROW;
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
    :   assign_or_expr
    |   r=RETURN expr -> ^(RETURN[$r, "RETURN"] expr)
    ;

assign_or_expr
    :   expr (a=ASSIGN assign_or_expr)? // Right-associative
        -> {a==null}? ^(EXPR expr)
        -> ^(ASSIGN expr assign_or_expr)
    ;

compound_stmt
    :   if_stmt
    |   for_stmt
    |   while_stmt
    |   import_stmt
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
        -> ^(FOR_STMT expr ^(LAMBDA ^(PARAMS paramlist) block))
    ;

while_stmt
    :  WHILE expr COLON block -> ^(WHILE_STMT expr block)
    ;

import_stmt
    :  IMPORT module -> ^(IMPORT_STMT module)
    |  FROM module IMPORT ID (COMMA ID)* -> ^(IMPORT_STMT module ID+)
    ;

module
    :  ID ('.'! ID^)*
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
    :   (ID | param_group) (COMMA! (ID | param_group))* (COMMA! keyword)*
    |   keyword (COMMA! keyword)*
    ;

param_group
    :   '*' ID -> ^(PARAM_GROUP ID)
    ;

keyword
    :   {keywordIsNext()}?
        ID LARROW expr -> ^(KEYWORD ID expr)
    ;

funcall
    :   (options {greedy=true;}: ID args) -> ^(FUNCALL ID args)
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
    @init{boolean keywords=false;}
    :  (flatten_arg | expr | keyword {keywords=true;}) (options {greedy=true;}: COMMA! (
            {keywords==false}?=> (flatten_arg | expr | keyword {keywords=true;})
        |   keyword {keywords=true;}
        ))*
    ;

flatten_arg
    : {input.LT(1).getText().equals("*") && directlyFollows(input.LT(1), input.LT(2))}?
      '*' expr -> ^(FLATTEN_ARG expr)
    ;

do_lambda
    :   {!nextIs(FOR, WHILE, IF, ELSE, ELIF, DEF, CLASS)
        && before(NEWLINE, COLON) && !before(COLON, ASSIGN)}?
        methcalls lambda -> ^(LAMBDACALL methcalls lambda)
    ;

methcalls
    :   (atom -> atom) ('.' f=funcall -> ^(METHCALL $methcalls $f))*
    ;

assign_lambda
    :   {before(NEWLINE, COLON) && before(COLON, ASSIGN)}?
        expr ASSIGN^ do_lambda
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
    :   shift_expr (options {greedy=true;}:
            (EQUAL^ | NOT_EQUAL^ | LT^ | LE^ | GT^ | GE^) shift_expr)?
    ;

shift_expr
    :   num_expr (options {greedy=true;}: (LSHIFT^ | RSHIFT^) num_expr)*
    ;

num_expr
    :   term (options {greedy=true;}: (PLUS^ | MINUS^ | DOUBLE_PLUS^) term)*
    ;

term
    :   power (options {greedy=true;}: (MUL^ | DIV^ | DDIV^ | MOD^) power)*
    ;

power
    :   factor (options {greedy=true;}: POW^ factor)*
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
    |   BACKTICKS
    |   (b=TRUE | b=FALSE)  -> ^(BOOLEAN[$b,$b.text])
    |   NONE
    |   global_var
    |   instance_var
    |   klass_var
    |   reference_var
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

reference_var
    :   AMPERSAND ID -> ^(REFERENCE_VAR ID)
    ;

list
    @init{boolean f = false;}
    :   LBRACK (e1=expr ((COMMA expr)* | FOR paramlist IN e2=expr {f=true;}))? RBRACK
        -> {f}? ^(LIST_EXPR $e2 ^(LAMBDA ^(PARAMS paramlist) ^(BLOCK ^(EXPR $e1))))
        -> ^(ARRAY expr*)
    ;

dict
    :   LBRACE (entry (COMMA entry)*)? RBRACE -> ^(DICT entry*)
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
ASSIGN	: '=' ;
EQUAL   : '==';
NOT_EQUAL: '!=' ;
LT      : '<' ;
LE      : '<=';
GT      : '>';
GE      : '>=';
PLUS    : '+' ;
DOUBLE_PLUS : '++';
MINUS   : '-' ;
MUL     : '*';
POW     : '**';
DIV     : '/';
DDIV    : '//';
MOD     : '%' ;
LSHIFT  : '<<';
RSHIFT  : '>>';
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
IMPORT  : 'import';
FROM    : 'from';
// SPECIAL SYMBOLS
COLON     : ':' ;
SEMICOLON : ';';
LPAREN  : '(';
RPAREN  : ')';
LBRACK  : '[' NL?;
RBRACK  : NL? SP? ']';
LBRACE  : '{' NL?;
RBRACE  : NL? SP? '}';
LARROW  : '=>';
AT      : '@';
DOUBLE_AT : '@@';
DOLLAR    : '$';
AMPERSAND : '&';
COMMA     : ',' NL?;


// Useful fragments
fragment DIGIT : ('0'..'9');
fragment LOWER : ('a'..'z');
fragment UPPER : ('A'..'Z');
fragment LETTER: (LOWER|UPPER);
fragment NL     : (('\r')? '\n')+;
fragment SP     : (' ' | '\t')+;

// Identifiers
ID  : (LETTER|'_') (LETTER|'_'|DIGIT)* (('!'|'?')('_')*)?;

// Numbers
NUMBER : {$type=INT;}(DIGIT+ (('.' DIGIT)=> '.' DIGIT+ {$type=FLOAT;})?);
fragment INT   :;
fragment FLOAT :;

// Strings
STRING
    @init { final StringBuilder buf = new StringBuilder(); }
    : '"' ( ESC_SEQ[buf]
          | i = ~('\\'|'"') { buf.appendCodePoint(i); }
          )* '"'
      { setText(buf.toString()); }
    | '\'' ( ESC_SEQ[buf]
          | i = ~('\\'|'\'') { buf.appendCodePoint(i); }
          )* '\''
      { setText(buf.toString()); }
    ;

BACKTICKS
    @init { final StringBuilder buf = new StringBuilder(); }
    :   '`' (
            '\\`' { buf.append('`'); }
            | i = ~('`') { buf.appendCodePoint(i); }
        )* '`'
        { setText(buf.toString()); }
    ;

fragment ESC_SEQ[StringBuilder buf]
        :   '\\'
            ('b'  { buf.append('\b'); }
            |'t'  { buf.append('\t'); }
            |'n'  { buf.append('\n'); }
            |'f'  { buf.append('\f'); }
            |'r'  { buf.append('\r'); }
            |'"' { buf.append('"'); }
            |'\'' { buf.append('\''); }
            |'\\' { buf.append('\\'); } )
        ;

WS        : SP {skip();};
LINEBREAK : '\\' NL {skip();};
COMMENT   : '#' ~('\n'|'\r')* {skip();};

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

        // Skip if same indentation or empty line or comment
        if(n == currentIndent || next == '\n'
           || next == '\r' || next == -1 || next=='#')
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
