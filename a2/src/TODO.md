# TODO

Kemal:

```ANTLR
arrayInit : 'new' TYPE '[' expr ']' | '{' ( expr ( ',' expr )* )? '}' ;

assign : ID '=' expr ';' ;

arrayAssign : arrayVar '=' expr ';' ;
```

My:

```ANTLR
stmt
    : ID ('[' expr ']')? '=' expr ';'  # AssignStmt
;
expr
    : 'new' TYPE '[' expr ']'       # ArrayCreationExpr
    | '{' (expr (',' expr)*)? '}'   # ArrayInitExpr
    ;
```
