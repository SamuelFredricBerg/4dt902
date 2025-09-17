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
    public OFPType visitFuncCall(OFPParser.FuncCallContext ctx) {
        String functionName = ctx.ID().getText();
        Symbol functionSymbol = currentScope.resolve(functionName);

        if (functionSymbol == null || !(functionSymbol instanceof FunctionSymbol)) {
            System.err.println("Error: Function '" + functionName + "' is not declared.");
            return OFPType.ERROR;
        }

        FunctionSymbol funcSym = (FunctionSymbol) functionSymbol;
        if (ctx.expr().size() != funcSym.getParameters().size()) {
            System.err.println("Error: Function '" + functionName + "' expects " + funcSym.getParameters().size()
                    + " arguments, but " + ctx.expr().size() + " were provided.");
            return OFPType.ERROR;
        }

        for (int i = 0; i < ctx.expr().size(); i++) {
            OFPType argType = visit(ctx.expr(i));
            OFPType paramType = funcSym.getParameters().get(i).getType();

            if (argType.equals(OFPType.VOID)) {
                System.err.println("Error: Cannot pass void as an argument to function '" + functionName + "'.");
                return OFPType.ERROR;
            }

            if (!argType.equals(paramType)) {
                System.err.println("Error: Argument type mismatch in function '" + functionName + "'. Expected '"
                        + paramType + "', but got '" + argType + "'.");
                return OFPType.ERROR;
            }
        }

        return funcSym.getReturnType();
    }

    @Override
    public OFPType visitReturnStmt(OFPParser.ReturnStmtContext ctx) {
        Scope returnScope = scopes.get(ctx);

        if (returnScope == null) {
            System.err.println("Error: 'return' statement is outside of a valid scope.");
            return OFPType.ERROR;
        }

        FunctionSymbol currentFunction = returnScope.getEnclosingScope().getFunctionSymbol();

        if (currentFunction.getReturnType().equals(OFPType.VOID)) {
            if (ctx.expr() != null) {
                System.err.println("Error: Cannot return a value from a void function.");
                return OFPType.ERROR;
            }
        } else {
            if (ctx.expr() == null) {
                System.err.println("Error: Function '" + currentFunction.getName() + "' requires a return value.");
                return OFPType.ERROR;
            }

            OFPType returnType = visit(ctx.expr());

            if (!returnType.equals(currentFunction.getReturnType())) {
                System.err.println("Error: Return type mismatch in function '" + currentFunction.getName()
                        + "'. Expected '" + currentFunction.getReturnType() + "' but got '" + returnType + "'.");
                return OFPType.ERROR;
            }
        }

        return null;
    }

    @Override
    public OFPType visitBlock(OFPParser.BlockContext ctx) {
        currentScope = scopes.get(ctx);
        super.visitBlock(ctx);
        currentScope = currentScope.getEnclosingScope();
        return null;
    }

    @Override
    public OFPType visitIDExpr(OFPParser.IDExprContext ctx) {
        String varName = ctx.ID().getText();
        Symbol varSymbol = currentScope.resolve(varName);

        if (varSymbol == null) {
            System.err.println("Error: Variable '" + varName + "' not declared.");
            return OFPType.ERROR;
        }

        return varSymbol.getType();
    }

    @Override
    public OFPType visitVarDeclStmt(OFPParser.VarDeclStmtContext ctx) {
        return null; // varType;
    }

    @Override
    public OFPType visitAssignStmt(OFPParser.AssignStmtContext ctx) {
        return null; // exprType;
    }

    /*
     * More Override functions to add
     */

    @Override
    public OFPType visitRelExpr(OFPParser.RelExprContext ctx) {
        OFPType leftType = visit(ctx.expr(0));
        OFPType rightType = visit(ctx.expr(1));

        if (leftType.equals(OFPType.ERROR) || rightType.equals(OFPType.ERROR)) {
            System.err.println("Error: Invalid comparison expression.");
            return OFPType.ERROR;
        }

        if (!leftType.equals(rightType)) {
            if (leftType.equals(OFPType.INT_ARRAY) || leftType.equals(OFPType.FLOAT_ARRAY)
                    || leftType.equals(OFPType.CHAR_ARRAY) || leftType.equals(OFPType.STRING)
                    || rightType.equals(OFPType.INT_ARRAY) || rightType.equals(OFPType.FLOAT_ARRAY)
                    || rightType.equals(OFPType.CHAR_ARRAY) || rightType.equals(OFPType.STRING)) {
                if (!(ctx.getParent() instanceof OFPParser.ArrayLengthExprContext)) {
                    System.err.println("Error: Type mismatch in comparison. Both sides must have the same type.");
                    return OFPType.ERROR;
                }
            }
        }

        if (!leftType.equals(OFPType.INT) && !leftType.equals(OFPType.FLOAT)
                && !leftType.equals(OFPType.CHAR)) {
            if (leftType.equals(OFPType.STRING)) {
                if (ctx.getChild(1).getText().equals(">") || ctx.getChild(1).getText().equals("<")) {
                    System.err.println("Error: Cannot use '>' or '<' with string type.");
                    return OFPType.ERROR;
                }
            } else {
                System.err.println("Error: Comparison operators can only be used with int, float, or char types.");
                return OFPType.ERROR;
            }
        }

        return OFPType.BOOLEAN;
    }

    @Override
    public OFPType visitEqExpr(OFPParser.EqExprContext ctx) {
        OFPType leftType = visit(ctx.expr(0));
        OFPType rightType = visit(ctx.expr(1));

        if (leftType.equals(OFPType.ERROR) || rightType.equals(OFPType.ERROR)) {
            System.err.println("Error: Invalid equality expression.");
            return OFPType.ERROR;
        }

        if (!leftType.equals(rightType)) {
            if (leftType.equals(OFPType.INT_ARRAY) || leftType.equals(OFPType.FLOAT_ARRAY)
                    || leftType.equals(OFPType.CHAR_ARRAY) || leftType.equals(OFPType.STRING)
                    || rightType.equals(OFPType.INT_ARRAY) || rightType.equals(OFPType.FLOAT_ARRAY)
                    || rightType.equals(OFPType.CHAR_ARRAY) || rightType.equals(OFPType.STRING)) {
                if (!(ctx.getParent() instanceof OFPParser.ArrayLengthExprContext)) {
                    System.err.println("Error: Type mismatch in comparison. Both sides must have the same type.");
                    return OFPType.ERROR;
                }
            }
        }

        if (!leftType.equals(OFPType.INT) && !leftType.equals(OFPType.FLOAT)
                && !leftType.equals(OFPType.CHAR)) {
            if (leftType.equals(OFPType.STRING)) {
                if (ctx.getChild(1).getText().equals("==")) {
                    System.err.println("Error: Cannot use '==' with string type.");
                    return OFPType.ERROR;
                }
            } else {
                System.err.println("Error: Comparison operators can only be used with int, float, or char types.");
                return OFPType.ERROR;
            }
        }

        return OFPType.BOOLEAN;
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
