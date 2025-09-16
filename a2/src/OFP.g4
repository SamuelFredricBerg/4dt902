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
    : (TYPE | 'void') ID '(' paramList? ')' block
    ;

block
    : '{' stmt* '}'
    ;

paramList
    : TYPE ID (',' TYPE ID)*
    ;

argList
    : expr (',' expr)*
    ;

stmt
    : ('print' | 'println') '(' expr ')' ';'    # PrintStmt
    | ID '(' argList? ')' ';'                   # FuncCallStmt
    | ID ('[' expr ']')? '=' expr ';'           # AssignStmt
    | TYPE ID ('=' expr)? ';'                   # VarDeclStmt
    | 'if' '(' expr ')' (block | stmt)
    ('else' (block | stmt))?                    # IfStmt
    | 'while' '(' expr ')' (block | stmt)       # WhileStmt
    | 'return' expr? ';'                        # ReturnStmt
    ;

expr
    : '{' argList? '}'          # ArrayInitExpr
    | expr '[' expr ']'         # ArrayAccessExpr
    | expr '.length'            # ArrayLengthExpr
    | ID '(' argList? ')'       # FuncCallExpr
    | '(' expr ')'              # ParenExpr
    | '-' expr                  # UnaryExpr
    | expr ('*' | '/') expr     # MulExpr
    | expr ('+' | '-') expr     # AddExpr
    | expr ('<' | '>') expr     # RelExpr
    | expr '==' expr            # EqExpr
    | 'new' TYPE '[' expr ']'   # ArrayCreationExpr
    | (INT
    | FLOAT
    | BOOLEAN
    | CHAR
    | STRING
    | ID)                       # AtomicExpr
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
    : '\'' [a-zA-Z!.?,=:() ] '\'' // TODO: Check if 'epsilon' is allowed epsilon meaning empty
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


