grammar A1;

@header {
    package generated;
}


// Parser Rules

start
    : (funcDecl*  main funcDecl*) EOF
    ;

main
    : 'void' 'main' '(' ')' block
    ;

block
    : '{' stmt* '}'
    ;

blockOrStmt
    : block
    | stmt
    ;

returnType
    : (TYPE
    | 'void')
    ;

funcDecl
    : returnType ID '(' (funcInput (',' funcInput)*)? ')' block
    ;

funcInput
    : typeId
    ;

callFunc
    : ID '(' (expr (',' expr)*)? ')'
    ;

returnStmt
    : 'return' expr? ';'
    ;

typeId
    : TYPE ID
    ; //TODO: Adds complexity?

declStmt
    : typeId ('=' expr)? ';'
    ;

assignStmt
    : ID ('[' expr ']')? ('=' expr) ';'
    ;

condition
    : '(' expr ')'
    ;

whileStmt
    : 'while' condition block
    ;

ifElseStmt
    : 'if' condition blockOrStmt
    ('else' blockOrStmt)?
    ;

newArray
    : 'new' TYPE '[' expr ']'
    ;


// Precedence Relatade (needed for stmt????)

stmt
    : 'println' condition ';'
    | 'print' condition ';'
    | assignStmt
    | declStmt
    | ifElseStmt
    | whileStmt
    | callFunc ';'
    | returnStmt
    // | expr ';'
    ; //TODO: Priority???

expr
    : '{' (expr (',' expr)*)? '}'
    | expr '[' expr ']'
    | expr '.length'
    | condition
    | '-' expr
    | expr ('*' | '/' ) expr
    | expr ('+' | '-') expr
    | expr ('<' | '>') expr
    | expr '==' expr
    | newArray
    | callFunc
    | (BOOLEAN | INT | FLOAT | STRING | CHAR | ID)
    ; //TODO: Priority???


// Lexer Rules

ID
    : [a-zA-Z]+
    ;

TYPE
    : ('int' '[]'?
    | 'float' '[]'?
    | 'char' '[]'?
    | 'bool'
    | 'string')
    ;

INT
    : '0'|[1-9][0-9]*
    ;

FLOAT
    : ('0'|[1-9][0-9]*)'.'[0-9]+
    ;

BOOLEAN
    : 'true'
    | 'false'
    ;

CHAR
    : '\'' ([a-zA-Z!.?,=:() ]) '\''
    ; //TODO: Check if same spec as string for chars?

STRING
    : '"' ([a-zA-Z!.?,=:() ])* '"'
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

COMMENT
    : '#'~( '\r' | '\n' )* -> skip
    ;
