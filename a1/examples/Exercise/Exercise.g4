/**
 * Define a grammar called Exercise
 */
grammar Exercise;


@header {    // Define name of package for generated Java files.
    package generated;
}

// Syntax Specification ==> Context-free Grammar
start
    : program EOF
    ;

program
    : 'program' ID block
    ;

block
    : '{' stmt* '}'
    ;

stmt
    : expr ';'
    | assign
    | decl
    | ifElse
    | while
    | 'print' '(' expr ')' ';'
    ;

decl
    : TYPE ID ('=' expr)? ';'
    ;

expr
    :  '(' expr ')'
    | expr ('*' | '/') expr
    | expr ('+' | '-') expr
    | expr ('<' | '>') expr
    | expr '==' expr
    | expr '&&' expr
    | INT
    | BOOLEAN
    | ID
    ;

assign
    : ID '=' expr ';'
    ;

ifElse
    : 'if' '(' expr ')' (block | stmt)
    ('else' (block | stmt))?
    ;

while
    : 'while' '(' expr ')' block
    ;

// Lexer Specification ==> Regular Expressions
// Only non-trivial expressions. Trivial token definitions are hard coded in grammar.

TYPE
    : 'int'
    | 'boolean'
    ;

BOOLEAN
    : 'true'
    | 'false'
    ;

INT
    : '0' | [1-9][0-9]*
    ;

ID
    : [a-zA-Z][a-zA-Z0-9]*
    ;

WS
    : [ \t\r\n]+ -> skip
    ;

COMMENT
    : '/*' .*? '*/' -> skip
    ;

LINE_COMMENT
    : '//' ~[\r\n]* -> skip
    ;