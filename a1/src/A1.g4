grammar A1;

@header {
    package generated;
}

// ----------------------------
// Parser Rules
// ----------------------------

start
    : (function*  main function*) EOF
    ;

main
    : 'void' 'main' '(' ')' block
    ;

function
    : returnType ID '(' (functionInput (',' functionInput)*)? ')' block
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

block
    : '{' stmt* '}'
    ;

blockOrStmt
    : block
    | stmt
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
    : 'while' '(' expr ')' block
    ;

ifAndElseStatements
    : 'if' '(' expr ')' ( blockOrStmt )
    ('else' 'if' '(' expr ')' ( blockOrStmt ))*
    ('else' ( blockOrStmt ))?
    ;

callFunction
    : ID '(' (expr (',' expr)*)? ')'
    ;

// ----------------------------
// Expression Rules with Precedence
// ----------------------------

expr
    : '{' (expr (',' expr)*)? '}'                                                                       # ArrayLiteralExpr
    | expr '[' expr ']'                                                                                 # ArrayAccessExpr
    | expr '.' 'length'                                                                                 # ArrayLengthExpr
    | '(' expr ')'                                                                                      # ParensExpr
    | expr ('++' | '--')                                                                                # PostFixExpr
    | ('++' | '--' | '+' | '-') expr                                                                    # UnaryExpr
    | ('~' | '!') expr                                                                                  # UnaryNotExpr
    | expr ('*' | '/' | '%') expr                                                                       # MultiplicativeExpr
    | expr ('+' | '-') expr                                                                             # AdditiveExpr
    | expr ('<<' | '>>' | '>>>') expr                                                                   # ShiftExpr
    | expr ('<' | '>' | '<=' | '>=') expr                                                               # RelationalExpr
    | expr ('==' | '!=') expr                                                                           # EqualityExpr
    | expr '&' expr                                                                                     # AndExpr
    | expr '^' expr                                                                                     # XorExpr
    | expr '|' expr                                                                                     # OrExpr
    | expr '&&' expr                                                                                    # LogicalAndExpr
    | expr '||' expr                                                                                    # LogicalOrExpr
    | expr '?' expr ':' expr                                                                            # TernaryExpr
    | expr ('=' | '+=' | '-=' | '*=' | '/=' | '%=' | '&=' | '^=' | '|=' | '<<=' | '>>=' | '>>>=') expr  # AssignmentExpr
    | newArray                                                                                          # NewArrayExpr
    | callFunction                                                                                      # FunctionCallExpr
    | BOOLEAN                                                                                           # BooleanLiteralExpr
    | INT                                                                                               # IntLiteralExpr
    | FLOAT                                                                                             # FloatLiteralExpr
    | STRING                                                                                            # StringLiteralExpr
    | CHAR                                                                                              # CharLiteralExpr
    | ID                                                                                                # IdentifierExpr
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
    | 'float' '[]'?
    | 'char' '[]'?
    | 'bool'
    | 'string'
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
    : [a-zA-Z]+
    ;

BOOLEAN
    : 'true' | 'false'
    ;

STRING
    : '"' ( [a-zA-Z!.?,=:() ] )* '"'
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
    : '#'~( '\r' | '\n' )* -> skip
    ;
