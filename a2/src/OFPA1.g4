grammar OFPA1;

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

stmt
    : print         # PrintStmt
    | callFunc ';'  # FuncCallStmt
    | assign        # AssignStmt
    | varDecl       # VarDeclStmt
    | if            # IfStmt
    | while         # WhileStmt
    | return        # ReturnStmt
    ;

expr
    : '{' argList? '}'          # ArrayInitExpr
    | expr '[' expr ']'         # ArrayAccessExpr
    | expr '.length'            # ArrayLengthExpr
    | callFunc                  # FuncCallExpr
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

block
    : '{' stmt* '}'
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

print
    : ('print' | 'println') '(' expr ')' ';'
    ;

assign
    : ID ('[' expr ']')? '=' expr ';'
    ;

varDecl
    : TYPE ID ('=' expr)? ';'
    ;

return
    : 'return' expr? ';'
    ;

if
    : 'if' '(' expr ')' (block | stmt)
    ('else' (block | stmt))?
    ;

while
    : 'while' '(' expr ')' (block | stmt)
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
    : '#' ~[\r\n]* -> skip
    ;

