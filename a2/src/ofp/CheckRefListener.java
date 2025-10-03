package ofp;

import org.antlr.v4.runtime.tree.ParseTreeProperty;

import generated.OFPBaseListener;
import generated.OFPParser;

/**
 * Listener for semantic analysis to check references to variables and
 * functions.
 * Reports errors for undeclared variables, incorrect function calls, and
 * invalid assignments.
 */
public class CheckRefListener extends OFPBaseListener {
    private ParseTreeProperty<Scope> scopes;
    private Scope currentScope;
    private Scope globalScope;
    private int checkRefErrorCount = 0;

    /**
     * Constructs a CheckRefListener with the given scopes and global scope.
     *
     * @param scopes      the mapping of parse tree nodes to scopes
     * @param globalScope the global scope
     */
    public CheckRefListener(ParseTreeProperty<Scope> scopes, Scope globalScope) {
        this.scopes = scopes;
        this.globalScope = globalScope;
    }

    /**
     * Checks if a function call refers to a declared function and argument count
     * matches.
     *
     * @param ctx the function call context
     */
    @Override
    public void enterFuncCall(OFPParser.FuncCallContext ctx) {
        String functionName = ctx.ID().getText();
        Symbol functionSymbol;

        if (globalScope.getSymbols().get(functionName) != null)
            functionSymbol = globalScope.resolve(functionName);
        else
            functionSymbol = currentScope.resolve(functionName);

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

    /**
     * Sets the current scope when entering a function block.
     *
     * @param ctx the function block context
     */
    @Override
    public void enterFuncBlock(OFPParser.FuncBlockContext ctx) {
        currentScope = scopes.get(ctx);
    }

    /**
     * Restores the enclosing scope when exiting a function block.
     *
     * @param ctx the function block context
     */
    @Override
    public void exitFuncBlock(OFPParser.FuncBlockContext ctx) {
        currentScope = currentScope.getEnclosingScope();
    }

    /**
     * Sets the current scope when entering a block.
     *
     * @param ctx the block context
     */
    @Override
    public void enterBlock(OFPParser.BlockContext ctx) {
        currentScope = scopes.get(ctx);
    }

    /**
     * Restores the enclosing scope when exiting a block.
     *
     * @param ctx the block context
     */
    @Override
    public void exitBlock(OFPParser.BlockContext ctx) {
        currentScope = currentScope.getEnclosingScope();
    }

    /**
     * Checks if a variable assigned to is declared and not in the global scope.
     *
     * @param ctx the assignment statement context
     */
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

    /**
     * Checks if a return statement is used outside of a function.
     *
     * @param ctx the return statement context
     */
    @Override
    public void enterReturnStmt(OFPParser.ReturnStmtContext ctx) {
        if (currentScope == globalScope) {
            System.err.println("Error: 'return' statement is used outside of a function at line "
                    + ctx.getStart().getLine() + ", column " + ctx.getStart().getCharPositionInLine() + ".");
            checkRefErrorCount++;
        }
    }

    /**
     * Checks if a variable referenced in an expression is declared.
     *
     * @param ctx the ID expression context
     */
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

    /**
     * Reports the total number of semantic errors found.
     */
    public void reportErrors() {
        System.out.println("\nSemantic analysis completed with " + checkRefErrorCount + " errors.\n");
    }
}
