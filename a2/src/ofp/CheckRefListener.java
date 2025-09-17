package ofp;

import org.antlr.v4.runtime.tree.ParseTreeProperty;

import generated.OFPBaseListener;
import generated.OFPParser;

public class CheckRefListener extends OFPBaseListener {
    private ParseTreeProperty<Scope> scopes;
    private Scope currentScope;
    private Scope globalScope;
    private int checkRefErrorCount = 0;

    public CheckRefListener(ParseTreeProperty<Scope> scopes, Scope globalScope) {
        this.scopes = scopes;
        this.globalScope = globalScope;
    }

    @Override
    public void enterBlock(OFPParser.BlockContext ctx) {
        currentScope = scopes.get(ctx);
    }

    @Override
    public void exitBlock(OFPParser.BlockContext ctx) {
        currentScope = currentScope.getEnclosingScope();
    }

    @Override
    public void enterIDExpr(OFPParser.IDExprContext ctx) {
        String varName = ctx.ID().getText();
        Symbol varSymbol = currentScope.resolve(varName);

        if (varSymbol == null) {
            System.err.println("Error: Variable '" + varName + "' is not declared in this scope at line "
                    + ctx.getStart().getLine() + ", column " + ctx.getStart().getCharPositionInLine() + ".");
            checkRefErrorCount++;
        }
    }

    @Override
    public void enterFuncCall(OFPParser.FuncCallContext ctx) {
        String functionName = ctx.ID().getText();
        Symbol functionSymbol = currentScope.resolve(functionName);

        if (functionSymbol == null || !(functionSymbol instanceof FunctionSymbol)) {
            System.err.println("Error: Function '" + functionName + "' is not declared.");
            checkRefErrorCount++;
        } else {
            FunctionSymbol funcSym = (FunctionSymbol) functionSymbol;
            if (ctx.expr().size() != funcSym.getParameters().size()) {
                System.err.println("Error: Function '" + functionName + "' expects " + funcSym.getParameters().size()
                        + " arguments but " + ctx.expr().size() + " were provided.");
                checkRefErrorCount++;
            }
        }
    }

    @Override
    public void enterAssignStmt(OFPParser.AssignStmtContext ctx) {
        String varName = ctx.ID().getText();
        Symbol varSymbol = currentScope.resolve(varName);

        if (varSymbol == null) {
            System.err.println("Error: Variable '" + varName + "' is not declared in this scope at line "
                    + ctx.getStart().getLine() + ", column " + ctx.getStart().getCharPositionInLine() + ".");
            checkRefErrorCount++;
        } else if (currentScope == globalScope) {
            System.err.println("Error: Assignment to variable '" + varName + "' is not allowed in the global scope.");
            checkRefErrorCount++;

        }
    }

    @Override
    public void enterReturnStmt(OFPParser.ReturnStmtContext ctx) {
        if (currentScope == globalScope) {
            System.err.println("Error: 'return' statement is used outside of a function at line "
                    + ctx.getStart().getLine() + ", column " + ctx.getStart().getCharPositionInLine() + ".");
            checkRefErrorCount++;
        }
    }

    public void reportErrors() {
        System.out.println("Semantic analysis completed with " + checkRefErrorCount + " errors.\n");
    }
}
