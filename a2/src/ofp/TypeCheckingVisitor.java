package ofp;

import org.antlr.v4.runtime.tree.ParseTreeProperty;

import generated.OFPBaseVisitor;
import generated.OFPParser;

public class TypeCheckingVisitor extends OFPBaseVisitor<OFPType> {
    private ParseTreeProperty<Scope> scopes;
    private Scope currentScope;

    public TypeCheckingVisitor(ParseTreeProperty<Scope> scopes) {
        this.scopes = scopes;
    }

    @Override
    public OFPType visitIntExpr(OFPParser.IntExprContext ctx) {
        return OFPType.INT;
    }

    @Override
    public OFPType visitFloatExpr(OFPParser.FloatExprContext ctx) {
        return OFPType.FLOAT;
    }

    @Override
    public OFPType visitBoolExpr(OFPParser.BoolExprContext ctx) {
        return OFPType.BOOLEAN;
    }

    @Override
    public OFPType visitCharExpr(OFPParser.CharExprContext ctx) {
        return OFPType.CHAR;
    }

    @Override
    public OFPType visitStringExpr(OFPParser.StringExprContext ctx) {
        return OFPType.STRING;
    }

    @Override
    public OFPType visitIfStmt(OFPParser.IfStmtContext ctx) {
        OFPType conditionType = visit(ctx.expr());
        if (!conditionType.equals(OFPType.BOOLEAN)) {
            System.err.println("Error: Condition in if-statement must be of type bool.");
            return OFPType.ERROR;
        }
        return super.visitIfStmt(ctx);
    }

    @Override
    public OFPType visitWhileStmt(OFPParser.WhileStmtContext ctx) {
        OFPType conditionType = visit(ctx.expr());
        if (!conditionType.equals(OFPType.BOOLEAN)) {
            System.err.println("Error: Condition in while-statement must be of type bool.");
            return OFPType.ERROR;
        }
        return super.visitWhileStmt(ctx);
    }
}
