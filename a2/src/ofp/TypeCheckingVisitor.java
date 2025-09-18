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
    public OFPType visitVarDeclStmt(OFPParser.VarDeclStmtContext ctx) {
        String varName = ctx.ID().getText();
        OFPType exprType;
        OFPType varType = OFPType.getTypeFor(ctx.TYPE().getText());

        if (ctx.expr() != null) {
            exprType = visit(ctx.expr());

            // ? Needed??? Thinking about int a = f(); where f() is void.
            if (exprType.equals(OFPType.VOID)) {
                System.err.println("Error: Cannot assign void type to variable '" + varName + "'.");
                return OFPType.ERROR;
            }

            if ((varType.equals(OFPType.INT_ARRAY) || varType.equals(OFPType.FLOAT_ARRAY)
                    || varType.equals(OFPType.CHAR_ARRAY))
                    && (exprType.equals(OFPType.INT) || exprType.equals(OFPType.FLOAT)
                            || exprType.equals(OFPType.CHAR))) {

                if (!(!(varType.equals(OFPType.INT_ARRAY) && exprType.equals(OFPType.INT))
                        || !(varType.equals(OFPType.FLOAT_ARRAY) && exprType.equals(OFPType.FLOAT))
                        || !(varType.equals(OFPType.CHAR_ARRAY) && exprType.equals(OFPType.CHAR)))) {
                    System.err.println("Error: Type mismatch in array assignment. Expected '" + varType + "' but got '"
                            + exprType + "'.");
                    return OFPType.ERROR;
                }

            } else if (!varType.equals(exprType)) {
                System.err.println(
                        "Error: Type mismatch in assignment. Expected '" + varType + "' but got '" + exprType + "'.");
                return OFPType.ERROR;
            }
        }

        return varType;
    }

    // TODO: fix impl does not work as intended currently
    @Override
    public OFPType visitAssignStmt(OFPParser.AssignStmtContext ctx) {
        String varName = ctx.ID().getText();
        Symbol varSymbol = currentScope.resolve(varName);
        if (ctx.expr(1) == null) {
            OFPType exprType = visit(ctx.expr(0)); // type assigned value

            // NormalAssign
            if (exprType != null && exprType.equals(OFPType.VOID)) {
                System.err.println("Error: Cannot assign the result of a void function.");
                return OFPType.ERROR;
            }

            if (varSymbol == null) {
                System.err.println("Error: Variable '" + varName + "' not declared.");
                return OFPType.ERROR;
            }

            OFPType varType = varSymbol.getType();

            if ((varType.equals(OFPType.INT_ARRAY) || varType.equals(OFPType.FLOAT_ARRAY)
                    || varType.equals(OFPType.CHAR_ARRAY))
                    && (exprType.equals(OFPType.INT_ARRAY) || exprType.equals(OFPType.FLOAT_ARRAY)
                            || exprType.equals(OFPType.CHAR_ARRAY))) {

                // Check if array types match
                if (!varType.equals(exprType)) {
                    System.err.println("Error: Type mismatch in array assignment. Expected '" + varType + "' but got '"
                            + exprType + "'.");
                    return OFPType.ERROR;
                }
            } else if (!varType.equals(exprType)) {
                System.err.println(
                        "Error: Type mismatch in assignment. Expected '" + varType + "' but got '" + exprType + "'.");
                return OFPType.ERROR;
            }

            return exprType;
        } else {
            // ArrayAssign
            OFPType indexType = visit(ctx.expr(0)); // Check the array index
            OFPType exprType = visit(ctx.expr(1)); // type assigned value

            if (!indexType.equals(OFPType.INT)) {
                System.err.println("Error: Array index must be of type int.");
            }

            if (varSymbol == null) {
                System.err.println("Error: Array '" + varName + "' not declared.");
                return OFPType.ERROR;
            }

            OFPType arrayType = varSymbol.getType();

            if (arrayType.equals(OFPType.INT_ARRAY) && !exprType.equals(OFPType.INT)) {
                System.err.println("Error: Cannot assign non-int to int array.");
            } else if (arrayType.equals(OFPType.FLOAT_ARRAY) && !exprType.equals(OFPType.FLOAT)) {
                System.err.println("Error: Cannot assign non-float to float array.");
            } else if (arrayType.equals(OFPType.CHAR_ARRAY) && !exprType.equals(OFPType.CHAR)) {
                System.err.println("Error: Cannot assign non-char to char array.");
            }

            return null;
        }
    }

    @Override
    public OFPType visitPrintStmt(OFPParser.PrintStmtContext ctx) {
        if (ctx.expr() != null) {
            OFPType exprType = visit(ctx.expr());
            if (exprType == null) {
                System.err.println("Error: Invalid type in print statement.");
                return OFPType.ERROR;
            }
        }
        return null;
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

    @Override
    public OFPType visitArrayInitExpr(OFPParser.ArrayInitExprContext ctx) {
        if (ctx.getChild(0).getText().equals("new")) {
            // Handle new TYPE[expr]
            OFPType arrayType = OFPType.getTypeFor(ctx.TYPE().getText());
            OFPType sizeType = visit(ctx.expr(0));

            if (!sizeType.equals(OFPType.INT)) {
                System.err.println("Error: Array size must be of type int.");
                return OFPType.ERROR;
            }

            if (!arrayType.equals(OFPType.INT) && !arrayType.equals(OFPType.FLOAT)
                    && !arrayType.equals(OFPType.CHAR)) {
                System.err.println("Error: Invalid array type at line " + ctx.getStart().getLine() + ", column "
                        + ctx.getStart().getCharPositionInLine() + ".");
                return OFPType.ERROR;
            }

            return arrayType;
        } else {
            // Handle { expr, ... }
            OFPType firstExprType = visit(ctx.expr(0));

            for (int i = 1; i < ctx.expr().size(); i++) {
                OFPType elementType = visit(ctx.expr(i));
                if (!elementType.equals(firstExprType)) {
                    System.err.println("Error: All elements in the array must have the same type.");
                    return OFPType.ERROR;
                }
            }

            if (firstExprType.equals(OFPType.INT)) {
                return OFPType.INT_ARRAY;
            } else if (firstExprType.equals(OFPType.FLOAT)) {
                return OFPType.FLOAT_ARRAY;
            } else if (firstExprType.equals(OFPType.CHAR)) {
                return OFPType.CHAR_ARRAY;
            } else {
                System.err.println("Error: Invalid element type in array initialization.");
                return OFPType.ERROR;
            }

            // Need to check if case is handled:
            // int[] a;
            // a = {1.1, 2.2, 3.3}
        }
    }

    @Override
    public OFPType visitArrayAccessExpr(OFPParser.ArrayAccessExprContext ctx) {
        String varName = ctx.ID().getText();
        Symbol varSymbol = currentScope.resolve(varName);

        if (varSymbol == null) {
            System.err.println("Error: Array '" + varName + "' not declared.");
            return OFPType.ERROR;
        }

        OFPType varType = varSymbol.getType();
        if (!varType.equals(OFPType.INT_ARRAY) && !varType.equals(OFPType.FLOAT_ARRAY)
                && !varType.equals(OFPType.CHAR_ARRAY) && !varType.equals(OFPType.STRING)) {
            System.err.println("Error: '" + varName + "' is not an array.");
            return OFPType.ERROR;
        }

        OFPType indexType = visit(ctx.expr());
        if (!indexType.equals(OFPType.INT)) {
            System.err.println("Error: Array index must be of type int.");
            return OFPType.ERROR;
        }

        if (varType.equals(OFPType.INT_ARRAY)) {
            return OFPType.INT;
        }

        if (varType.equals(OFPType.FLOAT_ARRAY)) {
            return OFPType.FLOAT;
        }

        if (varType.equals(OFPType.CHAR_ARRAY) || varType.equals(OFPType.STRING)) {
            return OFPType.CHAR;
        }

        return null; // should not be reached
    }

    @Override
    public OFPType visitArrayLengthExpr(OFPParser.ArrayLengthExprContext ctx) {
        OFPType exprType = visit(ctx.expr());

        if (exprType == null) {
            System.err.println("Error: Invalid expression type in length operation.");
            return OFPType.ERROR;
        }

        if (!exprType.equals(OFPType.STRING) && !exprType.equals(OFPType.INT_ARRAY)
                && !exprType.equals(OFPType.FLOAT_ARRAY) && !exprType.equals(OFPType.CHAR_ARRAY)) {
            System.err.println("Error: Length can only be applied to strings or arrays.");
            return OFPType.ERROR;
        }

        return OFPType.INT;
    }

    @Override
    public OFPType visitUnaryExpr(OFPParser.UnaryExprContext ctx) {
        OFPType exprType = visit(ctx.expr());
        if (!exprType.equals(OFPType.INT) && !exprType.equals(OFPType.FLOAT)) {
            System.err.println("Error: Unary minus can only be applied to int or float types.");
            return OFPType.ERROR;
        }
        return exprType;
    }

    public OFPType visitParenExpr(OFPParser.ParenExprContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public OFPType visitMultExpr(OFPParser.MultExprContext ctx) {
        OFPType leftType = visit(ctx.expr(0));
        OFPType rightType = visit(ctx.expr(1));

        if (leftType.equals(OFPType.ERROR) || rightType.equals(OFPType.ERROR)) {
            System.err.println("Error: Invalid multiplication/division expression.");
            return OFPType.ERROR;
        }

        if (leftType.equals(OFPType.VOID) || rightType.equals(OFPType.VOID)) {
            System.err.println("Error: Cannot use a void function in a multiplication/division expression.");
            return OFPType.ERROR;
        }

        if (!leftType.equals(rightType)) {
            System.err.println("Error: Type mismatch in expression. Both sides must have the same type.");
            return OFPType.ERROR;
        }

        if (!leftType.equals(OFPType.INT) && !leftType.equals(OFPType.FLOAT)
                || !rightType.equals(OFPType.INT) && !rightType.equals(OFPType.FLOAT)) {
            System.err.println("Error: Multiplication is only allowed for int or float types.");
            return OFPType.ERROR;
        }

        return leftType;
    }

    @Override
    public OFPType visitAddiExpr(OFPParser.AddiExprContext ctx) {
        OFPType leftType = visit(ctx.expr(0));
        OFPType rightType = visit(ctx.expr(1));

        if (leftType.equals(OFPType.ERROR) || rightType.equals(OFPType.ERROR)) {
            System.err.println("Error: Invalid arithmetic expression.");
            return OFPType.ERROR;
        }

        if (leftType.equals(OFPType.VOID) || rightType.equals(OFPType.VOID)) {
            System.err.println("Error: Cannot use a void function in an arithmetic expression.");
            return OFPType.ERROR;
        }

        if (!leftType.equals(rightType)) {
            System.err.println("Error: Type mismatch in expression. Both sides must have the same type.");
            return OFPType.ERROR;
        }

        if (!leftType.equals(OFPType.INT) && !leftType.equals(OFPType.FLOAT)) {
            System.err.println("Error: Addition and subtraction are only allowed for int or float types.");
            return OFPType.ERROR;
        }

        return leftType;
    }

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
    public OFPType visitIDExpr(OFPParser.IDExprContext ctx) {
        String varName = ctx.ID().getText();
        Symbol varSymbol = currentScope.resolve(varName);

        if (varSymbol == null) {
            System.err.println("Error: Variable '" + varName + "' not declared.");
            return OFPType.ERROR;
        }

        return varSymbol.getType();
    }
}
