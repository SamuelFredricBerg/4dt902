grammar Martin;

@header {
    // Define name of package for generated Java files.
    package generated;
}

// Syntax Specification ==> Context-free Grammar
start :  function* main function*;

main
    : 'void' 'main' '(' ')' block
    ;

function
    : TYPE  ID '(' ((functionInput ',')* functionInput)? ')' block
    ;

callFunction
    : ID '(' (expr ',')* (expr)? ')' ';'?
    ;
functionInput
    : TYPE ID
    ;

stmt
    : 'println'  condition ';'
    | 'print' condition ';'
    | assign
    | decl
    | ifAndElseStatments
    | whileStatment
    | callFunction
    | return
    ;

expr
    : condition
    | expr ('*' | '/') expr
    | expr ('+' | '-') expr
    | expr ('<' | '>' | '==') expr
    | '-' expr
    | expr ('.length')
    | expr ('[' expr ']')
    | '{' ((expr ',')* expr)? '}'
    | newArray
    | callFunction
    | BOOLEAN
    | INT
    | ID
    | STRING
    | FLOAT
    | CHAR
    ;

condition
    : '(' expr ')'
    ;

block
    : '{' stmt* '}'
    ;

whileStatment
    : 'while' condition block
    ;

ifAndElseStatments
    : ('if') condition (block | stmt)
    ('else'  (block | stmt) )?
    ;

newArray
    : ( 'new' TYPE '[' (expr) ']')
    ;

assign
    : ID ('[' expr ']')? '=' expr ';'
    ;

decl
    : TYPE ID ('=' expr)? ';'
    ;

return
    : 'return' (expr)? ';'
    ;

// Lexer Specification ==> Regular Expressions
TYPE
    : ('int'
    | 'int[]'
    | 'bool'
    | 'string'
    | 'float'
    | 'float[]'
    | 'char'
    | 'char[]')
    ;
FLOAT
    : ('0'|[1-9][0-9]*)'.'[0-9]+
    ;
INT
    : '0'|[1-9][0-9]*
    ;
ID
    : [a-zA-Z]+
    ;
BOOLEAN
    : 'true'
    | 'false'
    ;
STRING
    : '"' [a-zA-Z!.,?=:() ]* '"'
    ;
CHAR
    : '\'' [a-zA-Z!.,?=:() ] '\''
    ;

WS
    : [ \t\r\n]+ -> skip
    ;
COMMENT
    : '#'~( '\r' | '\n' )* -> skip
    ;
COMMENTFull
    : '/' .? '/' -> skip
    ;
LINE_COMMENT
    : '//' ~[\r\n] -> skip
    ;