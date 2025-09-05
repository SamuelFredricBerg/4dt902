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
    : (TYPE | 'void') ID '(' argList ')' block
    ;

callFunc
    : ID '(' (expr (',' expr)*)? ')'
    ;

argList
    : (TYPE ID (',' TYPE ID)*)?
    ;

stmt
    : 'println' condition ';'
    | 'print' condition ';'
    | callFunc ';'
    | assignStmt
    | declStmt
    | ifElseStmt
    | whileStmt
    | returnStmt
    ;

expr
    : '{' (expr (',' expr)*)? '}'
    | expr '[' expr ']'
    | expr '.length'
    | condition
    | '-' expr
    | expr ('*' | '/') expr
    | expr ('+' | '-') expr
    | expr ('<' | '>') expr
    | expr '==' expr
    | newArray
    | callFunc
    | BOOLEAN
    | INT
    | FLOAT
    | CHAR
    | STRING
    | ID
    ;

block
    : '{' stmt* '}'
    ;

blockOrStmt
    : block
    | stmt
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
    : 'if' condition blockOrStmt
    ('else' blockOrStmt)?
    ;

whileStmt
    : 'while' condition block
    ;

condition
    : '(' expr ')'
    ;

newArray
    : 'new' TYPE '[' expr ']'
    ;


// Lexer Rules

TYPE
    : ('int' '[]'?
    | 'float' '[]'?
    | 'char' '[]'?
    | 'bool'
    | 'string')
    ;

BOOLEAN
    : 'true'
    | 'false'
    ;

INT
    : '0'|[1-9][0-9]*
    ;

FLOAT
    : ('0'|[1-9][0-9]*)'.'[0-9]+
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
    : '//' ~[\r\n]* -> skip
    ;

BLOCK_COMMENT
    : '/*' .*? '*/' -> skip
    ;

EOL_COMMENT
    : '#'~( '\r' | '\n' )* -> skip
    ;
