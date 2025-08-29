grammar A1;

@header {
    package generated;
}

// ----------------------------
// Parser Rules
// ----------------------------

start
    : (funcDecl*  main funcDecl*) EOF
    ;

main
    : 'void' 'main' '(' ')' block
    ;

funcDecl
    : RETURNTYPE ID '(' (funcInput (',' funcInput)*)? ')' block
    ;

funcInput
    : TYPE ID
    ;

stmt
    : 'println' '(' expr ')' ';'
    | 'print' '(' expr ')' ';'
    | assign
    | decl
    | ifElseStmt
    | whileStmt
    | callFunc ';'
    | returnStmt
    ;

block
    : '{' stmt* '}'
    ;

blockOrStmt
    : block
    | stmt
    ;

assign
    : ID ('[' expr ']')? assignOp expr ';'   //TODO: Think this is worng logically with the current grammer of expr
    ;

assignOp
    : ('=' | '+=' | '-=' | '*=' | '/=' | '%=' | '&=' | '^=' | '|=' | '<<=' | '>>=' | '>>>=') //? Is this functional implementation of assinOps and expr priority?
    ;

decl
    : TYPE ID ('=' expr)? ';'
    ;

returnStmt
    : 'return' expr? ';'
    ;

whileStmt
    : 'while' '(' expr ')' block
    ;

ifElseStmt
    : 'if' '(' expr ')' blockOrStmt
    ('else' 'if' '(' expr ')' blockOrStmt)*
    ('else' blockOrStmt)?
    ;

callFunc
    : ID '(' (expr (',' expr)*)? ')'
    ;

// ----------------------------
// Expression Rules with Precedence
// ----------------------------

expr
    : '{' (expr (',' expr)*)? '}'                   # ArrayLiteralExpr
    | expr '[' expr ']'                             # ArrayAccessExpr
    | expr '.' 'length'                             # ArrayLengthExpr
    | '(' expr ')'                                  # ParensExpr
    | expr ('++' | '--')                            # PostFixExpr
    | ('++' | '--' | '+' | '-' | '~' | '!') expr    # UnaryExpr
    | expr ('*' | '/' | '%') expr                   # MultiplicativeExpr
    | expr ('+' | '-') expr                         # AdditiveExpr
    | expr ('<<' | '>>' | '>>>') expr               # ShiftExpr
    | expr ('<' | '>' | '<=' | '>=') expr           # RelationalExpr
    | expr ('==' | '!=') expr                       # EqualityExpr
    | expr '&' expr                                 # AndExpr
    | expr '^' expr                                 # XorExpr
    | expr '|' expr                                 # OrExpr
    | expr '&&' expr                                # LogicalAndExpr
    | expr '||' expr                                # LogicalOrExpr
    | expr '?' expr ':' expr                        # TernaryExpr
    | expr assignOp expr                            # AssignmentExpr
    | newArray                                      # NewArrayExpr
    | callFunc                                      # FunctionCallExpr
    | BOOLEAN                                       # BooleanLiteralExpr
    | INT                                           # IntLiteralExpr
    | FLOAT                                         # FloatLiteralExpr
    | STRING                                        # StringLiteralExpr
    | CHAR                                          # CharLiteralExpr
    | ID                                            # IdentifierExpr
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

RETURNTYPE
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
    : 'true'
    | 'false'
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
