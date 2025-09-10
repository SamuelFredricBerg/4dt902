grammar exam;

start : stmt+ ;

stmt : assign ';'
     | print ';'
     | 'while' expr '{' stmt* '}'
     ;

assign : ID '=' expr ;
print  : 'print' expr ;

expr : expr ('*' | '/') expr
     | expr ('+' | '-') expr
     | expr ('<' | '>') expr
     | (INT | ID)
     ;

INT : [0-9]+ ;
ID  : [a-zA-Z]+;
WS  : [ \t\r\n]+ -> skip ;