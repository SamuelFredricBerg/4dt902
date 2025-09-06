grammar modified_v;

@header {
    package generated;
}

start
    : 'program' ID block EOF    // EOF preferred  due to explicit enof of file.
    ;

block
    : '{' stmt* '}'             // '*' preferred due to 0 or more statements a program does not require any statements to be valid.
    ;

stmt
    : declStmt
    | assignStmt
    | printStmt
    | whileStmt
    | ifStmt
    ; // Looks good, Note: ordering of different statements does not matter as far as I can tell.

declStmt
    : TYPE ID ('=' expr)? ';'
    ; // Would want this to be a variable type de to more then one type of variable declaration in exercise input file.

assignStmt
    : ID '=' expr ';'
    ; // Looks good.

printStmt
    : 'print' '(' expr ')' ';'
    ; // Looks good. Note: Unclear if having the print statements in A1 as parser rules increes complexity or not.

whileStmt
    : 'while' '(' expr ')' block
    ; // Looks good.

ifStmt
    : 'if' '(' expr ')' (block | stmt) ('else' (block | stmt))?
    ; // We should allow non-bracketed single statements after if and else according to java syntax.

expr
    : '(' expr ')'              //# parenExpr
    | expr ('*'|'/') expr       //# mulDivExpr
    | expr ('+'|'-') expr       //# addSubExpr
    | expr ('<'|'>') expr       //# relExpr // Removed some operators due to them being unused for the exercise aswell as the A1.
    | expr '==' expr            //# equalsExpr // moved down from the above relational operators due to it according to java having lower priority then '<' and '>'.
    | BOOLEAN                   //# booleanExpr
    | INT                       //# intExpr
    | ID                        //# idExpr
    ; // Looks good, but missing the boolean type and ID should always be at the bottom due to it always having lowest priority.


TYPE
    : 'int'
    | 'boolean'
    ; // This to allow for boolean and ints to be declared by the same declStmt rule. Note: adding more types will become easier due to this implementation for later on.

INT
    : '0' | [1-9][0-9]*
    ; // We do not want to allow incorect integers like 00, 01, 0001 etc.

BOOLEAN
    : 'true' | 'false'
    ;

ID
    : [a-zA-Z][a-zA-Z0-9]*
    ; // Looks good, but do not think '_' should be included as valid ID character.

WS
    : [ \t\r\n]+ -> skip
    ;
LINE_COMMENT
    : '//' ~[\r\n]* -> skip
    ;