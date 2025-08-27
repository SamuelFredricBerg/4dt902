grammar Martin;

@header {
    package generated;
}

// ----------------------------
// Parser Rules
// ----------------------------

start
    : (function | main)* EOF
    ;

main
    : 'void' 'main' '(' ')' '{' stmt* '}'
    ;

function
    : returnType ID '(' (functionInput (',' functionInput)*)? ')' '{' stmt* '}'
    ;

functionInput
    : TYPE ID
    ;

stmt
    : 'println' '(' expr ')' ';'
    | 'print' '(' expr ')' ';'
    | assign
    | decl
    | ifAndElseStatements
    | whileStatement
    | callFunction ';'
    | returnStatement
    ;

assign
    : ID ('[' expr ']')? '=' expr ';'
    ;

decl
    : TYPE ID ('=' expr)? ';'
    ;

returnStatement
    : 'return' expr? ';'
    ;

whileStatement
    : 'while' '(' expr ')' '{' stmt* '}'
    ;

ifAndElseStatements
    : 'if' '(' expr ')' ( '{' stmt* '}' | stmt ) ('else' ( '{' stmt* '}' | stmt ))?
    ;

callFunction
    : ID '(' (expr (',' expr)*)? ')'
    ;

// ----------------------------
// Expression Rules with Precedence
// ----------------------------

expr
    : expr ('<' | '>' | '==') expr      # CompareExpr
    | expr ('+' | '-') expr             # AddSubExpr
    | expr ('*' | '/') expr             # MulDivExpr
    | '-' expr                          # NegateExpr
    | expr '.' 'length'                 # ArrayLengthExpr
    | expr '[' expr ']'                 # ArrayAccessExpr
    | '(' expr ')'                      # ParensExpr
    | '{' (expr (',' expr)*)? '}'       # ArrayLiteralExpr
    | newArray                          # NewArrayExpr
    | callFunction                      # FunctionCallExpr
    | BOOLEAN                           # BooleanLiteralExpr
    | INT                               # IntLiteralExpr
    | FLOAT                             # FloatLiteralExpr
    | STRING                            # StringLiteralExpr
    | CHAR                              # CharLiteralExpr
    | ID                                # IdentifierExpr
    ;

// ----------------------------
// Array Creation
// ----------------------------
newArray
    : 'new' TYPE '[' expr ']'
    ;

// ----------------------------
// Lexer Rules
// ----------------------------

TYPE
    : 'int' '[]'?
    | 'bool'
    | 'string'
    | 'float' '[]'?
    | 'char' '[]'?
    ;

returnType
    : TYPE
    | 'void'
    ;

FLOAT
    : [0-9]+ '.' [0-9]+
    ;

INT
    : '0' | [1-9][0-9]*
    ;

ID
    : [a-zA-Z][a-zA-Z0-9]*
    ;

BOOLEAN
    : 'true' | 'false'
    ;

STRING
    : '"' (~["\\\r\n])* '"'
    ;

CHAR
    : '\'' (~['\\\r\n]) '\''
    ;

// ----------------------------
// Comments and Whitespace
// ----------------------------

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
    : '# '~( '\r' | '\n' )* -> skip
    ;
