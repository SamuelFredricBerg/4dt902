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

stmt
    : print         # printStmt
    | callFunc ';'  # funcCallStmt
    | assign        # assignStmt
    | decl          # declStmt
    | ifElse        # ifElseStmt
    | while         # whileStmt
    | return        # returnStmt
    ;

expr
    : '{' argList? '}'          # arrayInit
    | expr '[' expr ']'         # arrayAccess
    | expr '.length'            # arrayLength
    | callFunc                  # functionCall
    | '(' expr ')'              # parentheses
    | '-' expr                  # unaryOp
    | expr ('*' | '/') expr     # mulDivOp
    | expr ('+' | '-') expr     # addSubOp
    | expr ('<' | '>') expr     # relOp
    | expr '==' expr            # equalityOp
    | 'new' TYPE '[' expr ']'   # arrayCreation
    | (INT
    | FLOAT
    | BOOLEAN
    | CHAR
    | STRING
    | ID)                       # atomic
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

decl
    : TYPE ID ('=' expr)? ';'
    ;

return
    : 'return' expr? ';'
    ;

ifElse
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


