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
    | 'void'
    ;

//TODO: Precedence Relatade (needed for stmt????)

stmt
    : 'println' condition ';'
    | 'print' condition ';'
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
    | expr MULDIV expr
    | expr ('+' | '-') expr //TODO: Why does it fail if I use ADDSUB LEXER rule here instead of current??
    | expr RELOP expr
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

IF //TODO: will LEXER rules for stmts such as if add complexity for future Assignmnents???
    : 'if'
    ;

MULDIV
    : '*' | '/'
    ;

ADDSUB
    : '+' | '-'
    ;

RELOP
    : '<' | '>'
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