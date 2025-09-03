grammar A1;

@header {
    package generated;
}


// Parser Rules

application
    : funcDecl*  main funcDecl* EOF
    ;

main
    : VOID MAIN '(' ')' block
    ;

funcDecl
    : returnType ID '(' (funcInput (',' funcInput)*)? ')' block
    ;

callFunc
    : ID '(' (expr (',' expr)*)? ')'
    ;

funcInput
    : typeId
    ;

returnType
    : TYPE
    | VOID
    ;

//TODO: Precedence Relatade (needed for stmt????)

stmt
    : PRINTLN condition ';'
    | PRINT condition ';'
    | assignStmt
    | declStmt
    | ifElseStmt
    | whileStmt
    | returnStmt
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
    | BOOLEAN
    | INT
    | FLOAT
    | CHAR
    | STRING
    | ID
    ; //TODO: Priority???

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
    : typeId ('=' expr)? ';'
    ;

typeId
    : TYPE ID
    ; //TODO: Adds complexity?

returnStmt
    : RETURN expr? ';'
    ;

ifElseStmt
    : IF condition blockOrStmt
    (ELSE blockOrStmt)?
    ;

whileStmt
    : WHILE condition block
    ;

condition
    : '(' expr ')'
    ;

newArray
    : NEW TYPE '[' expr ']'
    ;


// Lexer Rules

IF
    : 'if'
    ;

ELSE
    : 'else'
    ;

WHILE
    : 'while'
    ;

PRINTLN
    : 'println'
    ;

PRINT
    : 'print'
    ;

RETURN
    : 'return'
    ;

NEW
    : 'new'
    ;

VOID
    : 'void'
    ;

MAIN
    : 'main'
    ;

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
    ; //TODO: Check if same spec as string for chars?

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

//TODO: Why am I not able to add all the different types of comments under one LEXER rule???