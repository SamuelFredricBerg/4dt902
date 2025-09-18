# TODO

Kemal:

```ANTLR
assign : ID '=' expr ';' ;

arrayAssign : arrayVar '=' expr ';' ;
```

My:

```ANTLR
stmt
    : ID ('[' expr ']')? '=' expr ';'  # AssignStmt
;
```
