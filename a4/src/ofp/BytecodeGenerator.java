package ofp;

import java.io.PrintStream;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import generated.OFPBaseVisitor;
import generated.OFPParser;

/**
 * Visitor that generates JVM bytecode from the OFP parse tree using ASM.
 * Handles translation of OFP constructs to Java bytecode, including functions,
 * variables, and control flow.
 */
public class BytecodeGenerator extends OFPBaseVisitor<Type> implements Opcodes {
    private String fileName;
    private ClassWriter cw;
    private GeneratorAdapter mg;
    private ParseTreeProperty<Scope> scopes;
    private Scope globalScope;
    private Scope currentScope = null;
    private FunctionSymbol currentFunctionSymbol;
    private int pointer;

    /**
     * Constructs a BytecodeGenerator with the given file name, scopes, and global
     * scope.
     * 
     * @param fileName    the output class file name
     * @param scopes      the mapping of parse tree nodes to scopes
     * @param globalScope the global scope
     */
    public BytecodeGenerator(String fileName, ParseTreeProperty<Scope> scopes, Scope globalScope) {
        this.fileName = fileName;
        this.scopes = scopes;
        this.globalScope = globalScope;
    }

    public ClassWriter getClassWriter() {
        return cw;
    }

    /**
     * Generates bytecode for the program, including class and method definitions.
     * 
     * @param ctx the program context
     * @return null
     */
    @Override
    public Type visitProgram(OFPParser.ProgramContext ctx) {
        currentScope = scopes.get(ctx);
        cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(V1_1, ACC_PUBLIC, fileName, null, "java/lang/Object", null);

        Method constructor = Method.getMethod("void <init> ()");
        mg = new GeneratorAdapter(ACC_PUBLIC, constructor, null, null, cw);
        mg.loadThis();
        mg.invokeConstructor(Type.getType(Object.class), constructor);
        mg.returnValue();
        mg.endMethod();

        visitChildren(ctx);
        cw.visitEnd();

        return null;
    }

    /**
     * Generates bytecode for the main function.
     * 
     * @param ctx the main context
     * @return null
     */
    @Override
    public Type visitMain(OFPParser.MainContext ctx) {
        currentFunctionSymbol = new FunctionSymbol("main", OFPType.VOID);
        Method main = Method.getMethod("void main (String[])");
        mg = new GeneratorAdapter(ACC_PUBLIC + ACC_STATIC, main, null, null, cw);
        pointer = 1;

        visitChildren(ctx);
        mg.returnValue();
        mg.endMethod();

        return null;
    }

    /**
     * Generates bytecode for a function declaration.
     * 
     * @param ctx the function declaration context
     * @return null
     */
    @Override
    public Type visitFuncDecl(OFPParser.FuncDeclContext ctx) {
        currentFunctionSymbol = (FunctionSymbol) globalScope.resolve(ctx.getChild(1).getText());
        String returnType = ctx.getChild(0).getText();
        String funcName = ctx.getChild(1).getText();
        FunctionSymbol functionSymbol = (FunctionSymbol) globalScope.resolve(funcName);
        List<Symbol> funcParams = functionSymbol.getParameters();
        pointer = funcParams.size();
        StringBuilder funcParamsStringBuilder = new StringBuilder();

        for (int i = 0; i < funcParams.size(); i++) {
            OFPType paramType = funcParams.get(i).getType();
            funcParamsStringBuilder.append(ofpTypeToJavaType(paramType.toString()));
            if (i < funcParams.size() - 1)
                funcParamsStringBuilder.append(", ");
        }

        Method func = Method.getMethod(
                ofpTypeToJavaType(returnType) + " " + funcName + " (" + funcParamsStringBuilder.toString() + ")");
        mg = new GeneratorAdapter(ACC_PRIVATE + ACC_STATIC, func, null, null, cw);

        visit(ctx.getChild(ctx.getChildCount() - 1));
        mg.returnValue();
        mg.endMethod();

        return null;
    }

    /**
     * Generates bytecode for a function call.
     * 
     * @param ctx the function call context
     * @return the return type of the function
     */
    @Override
    public Type visitFuncCall(OFPParser.FuncCallContext ctx) {
        String funcName = ctx.getChild(0).getText();
        FunctionSymbol functionSymbol = (FunctionSymbol) globalScope.resolve(funcName);

        List<Symbol> funcParams = functionSymbol.getParameters();
        StringBuilder funcParamsStringBuilder = new StringBuilder();
        OFPType returnType = functionSymbol.getType();

        for (int i = 0; i < funcParams.size(); i++) {
            String paramType = ofpTypeToJavaType(funcParams.get(i).getType().toString());
            funcParamsStringBuilder.append(paramType);
            if (i < funcParams.size() - 1)
                funcParamsStringBuilder.append(", ");
            funcParams.get(i).setPointer(pointer);
            visit(ctx.expr(i));
        }

        mg.invokeStatic(Type.getType("L" + fileName + ";"),
                Method.getMethod(ofpTypeToJavaType(returnType.toString()) + " "
                        + funcName + " (" + funcParamsStringBuilder.toString() + ")"));

        return stringTypeToType(returnType.toString());
    }

    /**
     * Generates bytecode for a block of statements.
     * 
     * @param ctx the block context
     * @return null
     */
    @Override
    public Type visitBlock(OFPParser.BlockContext ctx) {
        currentScope = scopes.get(ctx);
        visitChildren(ctx);
        currentScope = currentScope.getEnclosingScope();

        return null;
    }

    /**
     * Generates bytecode for print statements.
     * 
     * @param ctx the print statement context
     * @return null
     */
    @Override
    public Type visitPrintStmt(OFPParser.PrintStmtContext ctx) {
        mg.getStatic(Type.getType(System.class), "out", Type.getType(PrintStream.class));

        Type exprType = visit(ctx.expr());
        String typeString;
        if (exprType == Type.INT_TYPE)
            typeString = "int";
        else if (exprType == Type.DOUBLE_TYPE)
            typeString = "double";
        else if (exprType == Type.BOOLEAN_TYPE)
            typeString = "boolean";
        else if (exprType == Type.CHAR_TYPE)
            typeString = "char";
        else if (exprType.toString().equals("java.lang.String"))
            typeString = "java.lang.String";
        else
            throw new RuntimeException("Unsupported print type: " + exprType);

        mg.invokeVirtual(Type.getType(PrintStream.class),
                Method.getMethod("void " + ctx.getChild(0).getText() + " (" + typeString + ")"));

        return null;
    }

    /**
     * Generates bytecode for assignment statements.
     * 
     * @param ctx the assignment statement context
     * @return the type of the assigned value
     */
    @Override
    public Type visitAssignStmt(OFPParser.AssignStmtContext ctx) {
        Symbol varSymbol = currentScope.resolve(ctx.ID().getText());
        int symbolPointer = varSymbol.getPointer();
        OFPType varType = varSymbol.getType();
        Type exprType;

        if (ctx.expr(1) == null) {
            exprType = visit(ctx.expr(0));
            mg.storeLocal(symbolPointer, exprType);

            return exprType;
        } else {
            Type arrayType = stringTypeToType(varType.toString());
            if (arrayType.equals(Type.getType(int[].class)))
                exprType = Type.INT_TYPE;
            else if (arrayType.equals(Type.getType(double[].class)))
                exprType = Type.DOUBLE_TYPE;
            else if (arrayType.equals(Type.getType(char[].class)))
                exprType = Type.CHAR_TYPE;
            else
                throw new RuntimeException("Unsupported array type: " + varType);

            boolean isParam = isParameter(varSymbol);

            if (!isParam)
                mg.loadLocal(symbolPointer, arrayType);

            visit(ctx.expr(0));
            visit(ctx.expr(1));
            mg.arrayStore(exprType);

            return arrayType;
        }
    }

    /**
     * Generates bytecode for variable declaration statements.
     * 
     * @param ctx the variable declaration statement context
     * @return the type of the variable
     */
    @Override
    public Type visitVarDeclStmt(OFPParser.VarDeclStmtContext ctx) {
        Symbol varSymbol = currentScope.resolve(ctx.ID().getText());
        varSymbol.setPointer(pointer);
        Type varType = stringTypeToType(varSymbol.getType().toString());

        if (ctx.expr() != null) {
            visit(ctx.expr());
            mg.storeLocal(pointer, varType);
        }

        if (varType == Type.DOUBLE_TYPE)
            pointer++;
        pointer++;

        return varType;
    }

    /**
     * Generates bytecode for if statements.
     * 
     * @param ctx the if statement context
     * @return null
     */
    @Override
    public Type visitIfStmt(OFPParser.IfStmtContext ctx) {
        Label ifLabel = new Label();
        Label endLabel = new Label();

        visit(ctx.expr());
        mg.push(0);
        mg.ifICmp(GeneratorAdapter.EQ, ifLabel);

        visit(ctx.block(0));
        mg.goTo(endLabel);
        mg.mark(ifLabel);

        if (ctx.block(1) != null)
            visit(ctx.block(1));
        mg.mark(endLabel);

        return null;
    }

    /**
     * Generates bytecode for while statements.
     * 
     * @param ctx the while statement context
     * @return null
     */
    @Override
    public Type visitWhileStmt(OFPParser.WhileStmtContext ctx) {
        Label whileStartLabel = new Label();
        Label whileEndLabel = new Label();

        mg.mark(whileStartLabel);

        visit(ctx.expr().getChild(0));
        visit(ctx.expr().getChild(2));
        mg.ifICmp(GeneratorAdapter.EQ, whileEndLabel);

        visit(ctx.block());
        mg.goTo(whileStartLabel);
        mg.mark(whileEndLabel);

        return null;
    }

    /**
     * Generates bytecode for return statements.
     * 
     * @param ctx the return statement context
     * @return the return type
     */
    @Override
    public Type visitReturnStmt(OFPParser.ReturnStmtContext ctx) {
        Type returnType = visit(ctx.expr());
        mg.returnValue();

        return returnType;
    }

    /**
     * Generates bytecode for array initialization expressions.
     * 
     * @param ctx the array initialization expression context
     * @return the array type
     */
    @Override
    public Type visitArrayInitExpr(OFPParser.ArrayInitExprContext ctx) {
        if (ctx.getChild(0).getText().equals("new")) {
            String arrayTypeString = ctx.getChild(1).getText();
            Type arrayType;
            Type arrayTypeClass;

            if (arrayTypeString.equals("int")) {
                arrayType = Type.INT_TYPE;
                arrayTypeClass = Type.getType(int[].class);
            } else if (arrayTypeString.equals("float")) {
                arrayType = Type.DOUBLE_TYPE;
                arrayTypeClass = Type.getType(double[].class);
            } else if (arrayTypeString.equals("char")) {
                arrayType = Type.CHAR_TYPE;
                arrayTypeClass = Type.getType(char[].class);
            } else
                throw new RuntimeException("Unsupported array type: " + arrayTypeString);

            visit(ctx.expr(0));
            mg.newArray(arrayType);

            return arrayTypeClass;
        } else {
            int arraySize = ctx.expr().size();
            Type arrayType = visit(ctx.expr(0));

            if (arrayType == Type.DOUBLE_TYPE)
                mg.pop2();
            else
                mg.pop();

            mg.push(arraySize);
            mg.newArray(arrayType);

            for (int i = 0; i < arraySize; i++) {
                mg.dup();
                mg.push(i);
                visit(ctx.expr(i));
                mg.arrayStore(arrayType);
            }

            if (arrayType == Type.INT_TYPE)
                return Type.getType(int[].class);
            else if (arrayType == Type.DOUBLE_TYPE)
                return Type.getType(double[].class);
            else if (arrayType == Type.CHAR_TYPE)
                return Type.getType(char[].class);
            else
                throw new RuntimeException("Unsupported array type: " + arrayType);
        }
    }

    /**
     * Generates bytecode for array access expressions.
     * 
     * @param ctx the array access expression context
     * @return the element type
     */
    @Override
    public Type visitArrayAccessExpr(OFPParser.ArrayAccessExprContext ctx) {
        Symbol varSymbol = currentScope.resolve(ctx.ID().getText());
        int symbolPointer = varSymbol.getPointer();
        OFPType varType = varSymbol.getType();

        if (varType == OFPType.STRING) {
            boolean isParam = isParameter(varSymbol);

            if (!isParam)
                mg.loadLocal(symbolPointer, Type.getType(String.class));

            visit(ctx.expr());
            mg.invokeVirtual(Type.getType(String.class), Method.getMethod("char charAt (int)"));

            return Type.CHAR_TYPE;
        } else {
            Type arrayType;
            Type arrayTypeClass;

            if (varType == OFPType.INT_ARRAY) {
                arrayType = Type.INT_TYPE;
                arrayTypeClass = Type.getType(int[].class);
            } else if (varType == OFPType.FLOAT_ARRAY) {
                arrayType = Type.DOUBLE_TYPE;
                arrayTypeClass = Type.getType(double[].class);
            } else if (varType == OFPType.CHAR_ARRAY) {
                arrayType = Type.CHAR_TYPE;
                arrayTypeClass = Type.getType(char[].class);
            } else
                throw new RuntimeException("Unsupported array type: " + varType);

            boolean isParam = isParameter(varSymbol);

            if (!isParam)
                mg.loadLocal(symbolPointer, arrayTypeClass);

            visit(ctx.expr());
            mg.arrayLoad(arrayType);

            return arrayType;
        }
    }

    /**
     * Generates bytecode for array length expressions.
     * 
     * @param ctx the array length expression context
     * @return int type
     */
    @Override
    public Type visitArrayLengthExpr(OFPParser.ArrayLengthExprContext ctx) {
        Type arrayType = visit(ctx.expr());

        if (arrayType.equals(Type.getType(String.class))) {
            mg.invokeVirtual(Type.getType(String.class), Method.getMethod("int length ()"));
        } else
            mg.arrayLength();

        return Type.INT_TYPE;
    }

    /**
     * Generates bytecode for parenthesized expressions.
     * 
     * @param ctx the parenthesized expression context
     * @return the type of the inner expression
     */
    @Override
    public Type visitParenExpr(OFPParser.ParenExprContext ctx) {
        return visit(ctx.expr());
    }

    /**
     * Generates bytecode for unary expressions.
     * 
     * @param ctx the unary expression context
     * @return the type of the expression
     */
    @Override
    public Type visitUnaryExpr(OFPParser.UnaryExprContext ctx) {
        Type exprType = visit(ctx.expr());
        mg.math(GeneratorAdapter.NEG, exprType);

        return exprType;
    }

    /**
     * Generates bytecode for multiplication/division expressions.
     * 
     * @param ctx the multiplication/division expression context
     * @return the type of the expression
     */
    @Override
    public Type visitMultExpr(OFPParser.MultExprContext ctx) {
        Type leftType = visit(ctx.expr(0));
        visit(ctx.expr(1));
        String operator = ctx.getChild(1).getText();

        if (operator.equals("*"))
            mg.math(GeneratorAdapter.MUL, leftType);
        else
            mg.math(GeneratorAdapter.DIV, leftType);

        return leftType;
    }

    /**
     * Generates bytecode for addition/subtraction expressions.
     * 
     * @param ctx the addition/subtraction expression context
     * @return the type of the expression
     */
    @Override
    public Type visitAddiExpr(OFPParser.AddiExprContext ctx) {
        Type leftType = visit(ctx.expr(0));
        visit(ctx.expr(1));
        String operator = ctx.getChild(1).getText();
        if (operator.equals("+"))
            mg.math(GeneratorAdapter.ADD, leftType);
        else
            mg.math(GeneratorAdapter.SUB, leftType);

        return leftType;
    }

    /**
     * Generates bytecode for relational expressions.
     * 
     * @param ctx the relational expression context
     * @return boolean type
     */
    @Override
    public Type visitRelExpr(OFPParser.RelExprContext ctx) {
        Type leftType = visit(ctx.expr(0));
        visit(ctx.expr(1));
        String operator = ctx.getChild(1).getText();
        Label falseLabel = new Label();
        Label trueLabel = new Label();

        if (operator.equals("<"))
            mg.ifCmp(leftType, GeneratorAdapter.GT, trueLabel);
        else if (operator.equals(">"))
            mg.ifCmp(leftType, GeneratorAdapter.LT, trueLabel);
        else
            throw new RuntimeException("Unsupported relational operator: " + operator);

        mg.push(true);
        mg.goTo(falseLabel);

        mg.mark(trueLabel);
        mg.push(false);
        mg.mark(falseLabel);

        return Type.BOOLEAN_TYPE;
    }

    /**
     * Generates bytecode for equality expressions.
     * 
     * @param ctx the equality expression context
     * @return boolean type
     */
    @Override
    public Type visitEqExpr(OFPParser.EqExprContext ctx) {
        Type leftType = visit(ctx.expr(0));
        visit(ctx.expr(1));
        String operator = ctx.getChild(1).getText();
        Label trueLabel = new Label();
        Label falseLabel = new Label();

        if (operator.equals("=="))
            mg.ifCmp(leftType, GeneratorAdapter.NE, trueLabel);

        mg.push(true);
        mg.goTo(falseLabel);

        mg.mark(trueLabel);
        mg.push(false);
        mg.mark(falseLabel);

        return Type.BOOLEAN_TYPE;
    }

    /**
     * Generates bytecode for integer literals.
     * 
     * @param ctx the integer expression context
     * @return int type
     */
    @Override
    public Type visitIntExpr(OFPParser.IntExprContext ctx) {
        mg.push(Integer.parseInt(ctx.getText()));

        return Type.INT_TYPE;
    }

    /**
     * Generates bytecode for float literals.
     * 
     * @param ctx the float expression context
     * @return double type
     */
    @Override
    public Type visitFloatExpr(OFPParser.FloatExprContext ctx) {
        mg.push(Double.parseDouble(ctx.getText()));

        return Type.DOUBLE_TYPE;
    }

    /**
     * Generates bytecode for boolean literals.
     * 
     * @param ctx the boolean expression context
     * @return boolean type
     */
    @Override
    public Type visitBoolExpr(OFPParser.BoolExprContext ctx) {
        mg.push(Boolean.parseBoolean(ctx.getText()));

        return Type.BOOLEAN_TYPE;
    }

    /**
     * Generates bytecode for char literals.
     * 
     * @param ctx the char expression context
     * @return char type
     */
    @Override
    public Type visitCharExpr(OFPParser.CharExprContext ctx) {
        mg.push(ctx.getText().charAt(1));

        return Type.CHAR_TYPE;
    }

    /**
     * Generates bytecode for string literals.
     * 
     * @param ctx the string expression context
     * @return string type
     */
    @Override
    public Type visitStringExpr(OFPParser.StringExprContext ctx) {
        String str = ctx.getText().substring(1, ctx.getText().length() - 1);
        mg.push(str);

        return Type.getType("java.lang.String");
    }

    /**
     * Generates bytecode for variable references.
     * 
     * @param ctx the ID expression context
     * @return the variable type
     */
    @Override
    public Type visitIDExpr(OFPParser.IDExprContext ctx) {
        Symbol varSymbol = currentScope.resolve(ctx.ID().getText());
        int symbolPointer = varSymbol.getPointer();
        OFPType varType = varSymbol.getType();

        Type javaType = stringTypeToType(varType.toString());

        for (int i = 0; i < currentFunctionSymbol.getParameters().size(); i++) {
            if (currentFunctionSymbol.getParameters().get(i).getName().equals(varSymbol.getName())) {
                mg.loadArg(i);
                return javaType;
            }
        }

        mg.loadLocal(symbolPointer, javaType);

        return javaType;
    }

    /**
     * Converts an OFP type string to a Java type string for ASM.
     * 
     * @param ofpType the OFP type string
     * @return the Java type string
     */
    private String ofpTypeToJavaType(String ofpType) {
        switch (ofpType) {
            case "float":
                return "double";
            case "bool":
                return "boolean";
            case "string":
                return "String";
            case "float[]":
                return "double[]";

            default:
                return ofpType;
        }
    }

    /**
     * Converts a type string to an ASM Type.
     * 
     * @param stringType the type string
     * @return the ASM Type
     */
    private Type stringTypeToType(String stringType) {
        switch (stringType) {
            case "void":
                return Type.VOID_TYPE;
            case "int":
                return Type.INT_TYPE;
            case "float":
                return Type.DOUBLE_TYPE;
            case "bool":
                return Type.BOOLEAN_TYPE;
            case "char":
                return Type.CHAR_TYPE;
            case "string":
                return Type.getType(String.class);
            case "int[]":
                return Type.getType(int[].class);
            case "float[]":
                return Type.getType(double[].class);
            case "char[]":
                return Type.getType(char[].class);

            default:
                return null;
        }
    }

    /**
     * Checks if a symbol is a function parameter.
     * 
     * @param varSymbol the symbol to check
     * @return true if the symbol is a parameter, false otherwise
     */
    private boolean isParameter(Symbol varSymbol) {
        boolean isParam = false;
        for (int i = 0; i < currentFunctionSymbol.getParameters().size(); i++) {
            if (currentFunctionSymbol.getParameters().get(i).getName().equals(varSymbol.getName())) {
                isParam = true;
                mg.loadArg(i);
                break;
            }
        }

        return isParam;
    }
}
