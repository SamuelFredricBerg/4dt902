grammar A1;

@header {
    package generated;
}


// Parser Rules

start
    : funcDecl*  main funcDecl* EOF
    ;

main
    : 'void' 'main' '(' ')' block
    ;

funcDecl
    : (TYPE | 'void') ID '(' paramList? ')' block
    ;

callFunc
    : ID '(' argList? ')'
    ;

paramList
    : TYPE ID (',' TYPE ID)*
    ;

argList
    : expr (',' expr)*
    ;

stmt
    : printStmt
    | callFunc ';'
    | assignStmt
    | declStmt
    | ifElseStmt
    | whileStmt
    | returnStmt
    ;

expr
    : '{' argList? '}'
    | expr '[' expr ']'
    | expr '.length'
    | callFunc
    | condition
    | '-' expr
    | expr ('*' | '/') expr
    | expr ('+' | '-') expr
    | expr ('<' | '>') expr
    | expr '==' expr
    | 'new' TYPE '[' expr ']'
    | INT
    | FLOAT
    | BOOLEAN
    | CHAR
    | STRING
    | ID
    ;

block
    : '{' stmt* '}'
    ;

printStmt
    : ('print' | 'println') condition ';'
    ;

assignStmt
    : ID ('[' expr ']')? '=' expr ';'
    ;

declStmt
    : TYPE ID ('=' expr)? ';'
    ;

returnStmt
    : 'return' expr? ';'
    ;

ifElseStmt
    : 'if' condition (block | stmt)
    ('else' (block | stmt))?
    ;

whileStmt
    : 'while' condition (block | stmt)
    ;

condition
    : '(' expr ')'
    ;

// Lexer Rules

TYPE
    : 'int' '[]'?
    | 'float' '[]'?
    | 'char' '[]'?
    | 'bool'
    | 'string'
    ;

BOOLEAN
    : 'true'
    | 'false'
    ;

INT
    : '0'|[1-9][0-9]*
    ;

FLOAT
    : INT'.'[0-9]+
    ;

CHAR
    : '\'' ([a-zA-Z!.?,=:() ]) '\''
    ;

STRING
    : '"' ([a-zA-Z!.?,=:() ])* '"'
    ;

ID
    : [a-zA-Z]+
    ;


// Comments and Whitespace

WS
    : [ \t\r\n]+ -> skip
    ;

LINE_COMMENT
    : ('//' | '#') ~[\r\n]* -> skip
    ;

BLOCK_COMMENT
    : '/*' .*? '*/' -> skip
    ;


