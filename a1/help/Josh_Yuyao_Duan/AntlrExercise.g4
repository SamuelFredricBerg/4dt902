grammar AntlrExercise;

@header {
    package generated;
}

// ====== Parser rules ======

start
    : 'program' ID block EOF
    ;

block
    : '{' stmt* '}'
    ;

stmt
    : declStmt
    | assignStmt
    | printStmt
    | whileStmt
    | ifStmt
    ;

declStmt
    : 'int' ID ('=' expr)? ';'
    ;

assignStmt
    : ID '=' expr ';'
    ;

printStmt
    : 'print' '(' expr ')' ';'
    ;

whileStmt
    : 'while' '(' expr ')' block
    ;

ifStmt
    : 'if' '(' expr ')' block ('else' block)?
    ;

// expressions
expr
    : expr op=('*'|'/') expr      # mulDivExpr
    | expr op=('+'|'-') expr      # addSubExpr
    | expr op=('<'|'>'|'=='|'!='|'<='|'>=') expr # relExpr
    | ID                          # idExpr
    | INT                         # intExpr
    | '(' expr ')'                # parenExpr
    ;

// ====== Lexer rules ======

INT : [0-9]+ ;
ID  : [a-zA-Z_][a-zA-Z_0-9]* ;

WS  : [ \t\r\n]+ -> skip ;
LINE_COMMENT : '//' ~[\r\n]* -> skip ;