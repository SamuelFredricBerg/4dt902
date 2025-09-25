package ofp;

import org.antlr.v4.runtime.tree.ParseTreeProperty;

import generated.OFPBaseVisitor;
import generated.OFPParser;

/**
 * Visitor for type checking expressions and statements in the OFP language.
 * Reports type errors and ensures semantic correctness during traversal.
 */
public class TypeCheckingVisitor extends OFPBaseVisitor<OFPType> {
    private ParseTreeProperty<Scope> scopes;
    private Scope currentScope;
    private Scope globalScope;

    /**
     * Constructs a TypeCheckingVisitor with the given scopes and global scope.
     * 
     * @param scopes      the mapping of parse tree nodes to scopes
     * @param globalScope the global scope
     */
    public TypeCheckingVisitor(ParseTreeProperty<Scope> scopes, Scope globalScope) {
        this.scopes = scopes;
        this.globalScope = globalScope;
    }

    /**
     * Checks type correctness for function calls.
     * 
     * @param ctx the function call context
     * @return the return type of the function, or error type if invalid
     */
    @Override
    public OFPType visitFuncCall(OFPParser.FuncCallContext ctx) {
        String functionName = ctx.ID().getText();
        Symbol functionSymbol;

        if (globalScope.getSymbols().get(functionName) != null)
            functionSymbol = globalScope.resolve(functionName);
        else
            functionSymbol = currentScope.resolve(functionName);

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

    /**
     * Updates the current scope when visiting a block.
     * 
     * @param ctx the block context
     * @return null
     */
    @Override
    public OFPType visitBlock(OFPParser.BlockContext ctx) {
        currentScope = scopes.get(ctx);
        super.visitBlock(ctx);
        currentScope = currentScope.getEnclosingScope();
        return null;
    }

    /**
     * Checks type correctness for print statements.
     * 
     * @param ctx the print statement context
     * @return null or error type if invalid
     */
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

    /**
     * Checks type correctness for assignment statements.
     * 
     * @param ctx the assignment statement context
     * @return the assigned type or error type if invalid
     */
    @Override
    public OFPType visitAssignStmt(OFPParser.AssignStmtContext ctx) {
        String varName = ctx.ID().getText();
        Symbol varSymbol = currentScope.resolve(varName);

        if (ctx.expr(1) == null) {
            // NormalAssign
            OFPType exprType = visit(ctx.expr(0));

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
            OFPType indexType = visit(ctx.expr(0));
            OFPType exprType = visit(ctx.expr(1));

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

    /**
     * Checks type correctness for variable declaration statements.
     * 
     * @param ctx the variable declaration statement context
     * @return the variable type or error type if invalid
     */
    @Override
    public OFPType visitVarDeclStmt(OFPParser.VarDeclStmtContext ctx) {
        String varName = ctx.ID().getText();
        OFPType exprType;
        OFPType varType = OFPType.getTypeFor(ctx.TYPE().getText());

        if (ctx.expr() != null) {
            exprType = visit(ctx.expr());

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

    /**
     * Checks type correctness for if statements.
     * 
     * @param ctx the if statement context
     * @return error type if condition is not boolean
     */
    @Override
    public OFPType visitIfStmt(OFPParser.IfStmtContext ctx) {
        OFPType conditionType = visit(ctx.expr());
        if (!conditionType.equals(OFPType.BOOLEAN)) {
            System.err.println("Error: Condition in if-statement must be of type bool.");
            return OFPType.ERROR;
        }
        return super.visitIfStmt(ctx);
    }

    /**
     * Checks type correctness for while statements.
     * 
     * @param ctx the while statement context
     * @return error type if condition is not boolean
     */
    @Override
    public OFPType visitWhileStmt(OFPParser.WhileStmtContext ctx) {
        OFPType conditionType = visit(ctx.expr());
        if (!conditionType.equals(OFPType.BOOLEAN)) {
            System.err.println("Error: Condition in while-statement must be of type bool.");
            return OFPType.ERROR;
        }
        return super.visitWhileStmt(ctx);
    }

    /**
     * Checks type correctness for return statements.
     * 
     * @param ctx the return statement context
     * @return error type if return value does not match function return type
     */
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

    /**
     * Checks type correctness for array initialization expressions.
     * 
     * @param ctx the array initialization expression context
     * @return the array type or error type if invalid
     */
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
            // Handle { expr, (expr*)? }
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
        }
    }

    /**
     * Checks type correctness for array access expressions.
     * 
     * @param ctx the array access expression context
     * @return the element type or error type if invalid
     */
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

        throw new IllegalStateException("Unexpected array type: " + varType);
    }

    /**
     * Checks type correctness for array length expressions.
     * 
     * @param ctx the array length expression context
     * @return int type or error type if invalid
     */
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

    /**
     * Checks type correctness for parenthesized expressions.
     * 
     * @param ctx the parenthesized expression context
     * @return the type of the inner expression
     */
    @Override
    public OFPType visitParenExpr(OFPParser.ParenExprContext ctx) {
        return visit(ctx.expr());
    }

    /**
     * Checks type correctness for unary expressions.
     * 
     * @param ctx the unary expression context
     * @return the type of the expression or error type if invalid
     */
    @Override
    public OFPType visitUnaryExpr(OFPParser.UnaryExprContext ctx) {
        OFPType exprType = visit(ctx.expr());
        if (!exprType.equals(OFPType.INT) && !exprType.equals(OFPType.FLOAT)) {
            System.err.println("Error: Unary minus can only be applied to int or float types.");
            return OFPType.ERROR;
        }
        return exprType;
    }

    /**
     * Checks type correctness for multiplication/division expressions.
     * 
     * @param ctx the multiplication/division expression context
     * @return the type of the expression or error type if invalid
     */
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

    /**
     * Checks type correctness for addition/subtraction expressions.
     * 
     * @param ctx the addition/subtraction expression context
     * @return the type of the expression or error type if invalid
     */
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

    /**
     * Checks type correctness for relational expressions.
     * 
     * @param ctx the relational expression context
     * @return boolean type or error type if invalid
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

    /**
     * Checks type correctness for equality expressions.
     * 
     * @param ctx the equality expression context
     * @return boolean type or error type if invalid
     */
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

    /**
     * Returns int type for integer expressions.
     * 
     * @param ctx the integer expression context
     * @return int type
     */
    @Override
    public OFPType visitIntExpr(OFPParser.IntExprContext ctx) {
        return OFPType.INT;
    }

    /**
     * Returns float type for float expressions.
     * 
     * @param ctx the float expression context
     * @return float type
     */
    @Override
    public OFPType visitFloatExpr(OFPParser.FloatExprContext ctx) {
        return OFPType.FLOAT;
    }

    /**
     * Returns boolean type for boolean expressions.
     * 
     * @param ctx the boolean expression context
     * @return boolean type
     */
    @Override
    public OFPType visitBoolExpr(OFPParser.BoolExprContext ctx) {
        return OFPType.BOOLEAN;
    }

    /**
     * Returns char type for char expressions.
     * 
     * @param ctx the char expression context
     * @return char type
     */
    @Override
    public OFPType visitCharExpr(OFPParser.CharExprContext ctx) {
        return OFPType.CHAR;
    }

    /**
     * Returns string type for string expressions.
     * 
     * @param ctx the string expression context
     * @return string type
     */
    @Override
    public OFPType visitStringExpr(OFPParser.StringExprContext ctx) {
        return OFPType.STRING;
    }

    /**
     * Returns the type of a referenced variable.
     * 
     * @param ctx the ID expression context
     * @return the variable type or error type if not declared
     */
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
