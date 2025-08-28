/**

Define a grammar called Expressions*/
grammar Martin;

@header {
    // Define name of package for generated Java files.
    package generated;
}

// Syntax Specification ==> Context-free Grammar
start :  function* main+ function;

main
    : 'void' 'main' '(' ')' '{' stmt '}'
    ;

function
    : TYPE  ID '(' ((functionInput ',')* functionInput)? ')' '{' stmt* '}'
    ;

callFunction
    : ID '(' (expr ',')* (expr)? ')' ';'?
    ;
functionInput
    : TYPE ID
    ;

stmt
    : 'println'  '(' expr ')' ';'
    |'print' '(' expr ')' ';'
    | assign
    | decl
    | ifAndElseStatments
    | whileStatment
    | callFunction
    | return
    ;

expr
    : '(' expr ')'
    | expr ('' | '/') expr
    | expr ('+' | '-') expr
    | expr ('<' | '>' | '==') expr
    | '-' expr
    | expr ('.length')
    | expr ('[' expr ']')
    | '{' ((expr ',') expr)? '}'
    | newArray
    | callFunction
    | (BOOLEAN
    | INT
    | ID
    | STRING
    | FLOAT
    | CHAR)
    ;

whileStatment
    : 'while' ('(' expr ')' '{' stmt* '}')
    ;

ifAndElseStatments
    : ('if') '(' expr ')' ('{' stmt* '}'
    | stmt) ('else'  ('{' stmt* '}' | stmt) )?
    ;

newArray
    : ( 'new' TYPE '[' (expr) ']')
    ;

assign
    : ID ('[' expr ']')?  '=' expr ';'
    ;

decl
    : TYPE  ID ('=' expr)?  ';'
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
    : ('0' | (('1'..'9') ('0'..'9'))) '.' ('0'..'9')+
    ;
INT
    : '0' | ('1' ..'9') ('0' ..'9')
    ;
ID
    : (('a'..'z') | ('A'..'Z'))+
    ;
BOOLEAN
    : ('true' | 'false')
    ;
STRING
    : '"' [a-zA-Z!.,?=:() ]* '"'
    ;
CHAR
    : ''' [a-zA-Z!.,?=:()] '''
    ;

WS
    : [ \t\r\n]+ -> skip
    ;
COMMENT
    : '# '~( '\r' | '\n' )* -> skip
    ;
COMMENTFull
    : '/' .? '/' -> skip
    ;
LINE_COMMENT
    : '//' ~[\r\n] -> skip
    ;