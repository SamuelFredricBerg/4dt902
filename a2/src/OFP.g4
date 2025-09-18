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

funcCall
    : ID '(' (expr (',' expr)*)? ')'
    ;


block
    : '{' stmt* '}'
    ;

stmt
    : ('print' | 'println') '(' expr ')' ';'    # PrintStmt
    | funcCall ';'                              # FuncCallStmt
    | ID ('[' expr ']')? '=' expr ';'           # AssignStmt
    | TYPE ID ('=' expr)? ';'                   # VarDeclStmt
    | 'if' '(' expr ')' (block | stmt)
    ('else' (block | stmt))?                    # IfStmt
    | 'while' '(' expr ')' (block | stmt)       # WhileStmt
    | 'return' expr? ';'                        # ReturnStmt
    ;

expr
    : ('{' (expr (',' expr)*)? '}'
    | 'new' TYPE '[' expr ']')      # ArrayInitExpr
    | ID '[' expr ']'               # ArrayAccessExpr
    | expr '.length'                # ArrayLengthExpr
    | funcCall                      # FuncCallExpr
    | '(' expr ')'                  # ParenExpr
    | '-' expr                      # UnaryExpr
    | expr ('*' | '/') expr         # MultExpr
    | expr ('+' | '-') expr         # AddiExpr
    | expr ('<' | '>') expr         # RelExpr
    | expr '==' expr                # EqExpr
    | INT                           # IntExpr
    | FLOAT                         # FloatExpr
    | BOOLEAN                       # BoolExpr
    | CHAR                          # CharExpr
    | STRING                        # StringExpr
    | ID                            # IDExpr
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
    : '\'' [a-zA-Z!.?,=:() ]? '\''
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
    :  '#' ~[\r\n]* -> skip
    ;