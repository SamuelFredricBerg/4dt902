grammar OFP;

@header {
    package generated;
}


// Parser Rules

program
    : funcDecl*  main funcDecl* EOF
    ;

main
    : 'void' 'main' '(' ')' block
    ;

funcDecl
    : (TYPE | 'void') ID '(' (TYPE ID (',' TYPE ID)*)? ')' block
    ;

block
    : '{' stmt* '}'
    ;

stmt
    : ('print' | 'println') '(' expr ')' ';'    # PrintStmt
    | ID '(' (expr (',' expr)*)? ')' ';'        # FuncCallStmt
    | ID ('[' expr ']')? '=' expr ';'           # AssignStmt
    | TYPE ID ('=' expr)? ';'                   # VarDeclStmt
    | 'if' '(' expr ')' (block | stmt)
    ('else' (block | stmt))?                    # IfStmt
    | 'while' '(' expr ')' (block | stmt)       # WhileStmt
    | 'return' expr? ';'                        # ReturnStmt
    ;

expr
    : '{' (expr (',' expr)*)? '}'       # ArrayInitExpr
    | expr '[' expr ']'                 # ArrayAccessExpr
    | expr '.length'                    # ArrayLengthExpr
    | ID '(' (expr (',' expr)*)? ')'    # FuncCallExpr
    | '(' expr ')'                      # ParenExpr
    | '-' expr                          # UnaryExpr
    | expr ('*' | '/') expr             # MulExpr
    | expr ('+' | '-') expr             # AddExpr
    | expr ('<' | '>') expr             # RelExpr
    | expr '==' expr                    # EqExpr
    | 'new' TYPE '[' expr ']'           # ArrayCreationExpr
    | (INT
    | FLOAT
    | BOOLEAN
    | CHAR
    | STRING
    | ID)                               # AtomicExpr
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
    : '\'' [a-zA-Z!.?,=:() ] '\'' // TODO: Check if '' is allowed
    ;

STRING
    : '"' [a-zA-Z!.?,=:() ]* '"'
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