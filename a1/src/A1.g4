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

funcDecl
    : returnType ID '(' (funcInput (',' funcInput)*)? ')' block
    ;

funcInput
    : typeId
    ;

stmt
    : 'println' condition ';'
    | 'print' condition ';'
    | assign
    | decl
    | ifElseStmt
    | whileStmt
    | callFunc ';'
    | returnStmt
    | expr ';'
    ; //TODO: Priority???

block
    : '{' stmt* '}'
    ;

blockOrStmt
    : block
    | stmt
    ;

assign
    : ID ('[' expr ']')? '=' expr ';'
    ; //TODO: stmt or expr??

decl
    : typeId ('=' expr)? ';'
    ;

typeId
    : TYPE ID
    ; //TODO: Adds complexity?

returnStmt
    : 'return' expr? ';'
    ;

whileStmt
    : 'while' condition block
    ;

ifElseStmt
    : 'if' condition blockOrStmt
    ('else' 'if' condition blockOrStmt)*
    ('else' blockOrStmt)?
    ;

callFunc
    : ID '(' (expr (',' expr)*)? ')'
    ;

condition
    : '(' expr ')'
    ;

returnType
    : (TYPE
    | 'void')
    ;


// Expression Rules with Precedence

expr
    : '{' (expr (',' expr)*)? '}'   # ArrayLiteralExpr
    | expr '[' expr ']'             # ArrayAccessExpr
    | expr '.' 'length'             # ArrayLengthExpr
    | condition                     # ParensExpr
    | '-' expr                      # UnaryExpr
    | expr ('*' | '/' ) expr        # MultiplicativeExpr
    | expr ('+' | '-') expr         # AdditiveExpr
    | expr ('<' | '>') expr         # RelationalExpr
    | expr '==' expr                # EqualityExpr
    | assign                        # AssignmentExpr
    | newArray                      # NewArrayExpr
    | callFunc                      # FunctionCallExpr
    | BOOLEAN                       # BooleanLiteralExpr
    | INT                           # IntLiteralExpr
    | FLOAT                         # FloatLiteralExpr
    | STRING                        # StringLiteralExpr
    | CHAR                          # CharLiteralExpr
    | ID                            # IdentifierExpr
    ;   //TODO: Priority okay??


// Array Creation

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

FLOAT
    : ('0'|[1-9][0-9]*)'.'[0-9]+
    ;

INT
    : '0'|[1-9][0-9]*
    ;

ID
    : [a-zA-Z]+
    ;

BOOLEAN
    : 'true'
    | 'false'
    ;

STRING
    : '"' ([a-zA-Z!.?,=:() ])* '"'
    ;

CHAR
    : '\'' ([a-zA-Z!.?,=:() ]) '\''
    ; //TODO: Check if same spec as string for chars?


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
