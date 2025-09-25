package ofp;

import java.util.List;
import java.util.HashSet;
import java.util.Arrays;

import org.antlr.v4.runtime.tree.ParseTreeProperty;

import generated.OFPBaseVisitor;
import generated.OFPParser;

/**
 * Visitor that generates Python code from the OFP parse tree.
 * Handles translation of OFP constructs to Python syntax, including indentation
 * and reserved identifiers.
 */
public class PythonGenerator extends OFPBaseVisitor<String> {
    private int depth = 0;
    private ParseTreeProperty<Scope> scopes;
    private Scope currentScope;

    /** Set of Python reserved identifiers to avoid naming conflicts. */
    private static HashSet<String> reservedIds = new HashSet<String>(Arrays.asList("False", "None", "True", "and", "as",
            "assert", "async", "await", "break", "class", "continue", "def", "del", "elif", "else", "except", "finally",
            "for", "from", "global", "if", "import", "in", "is", "lambda", "nonlocal", "not", "or", "pass", "raise",
            "return", "try", "while", "with", "yield", "ArithmeticError", "AssertionError", "AttributeError",
            "BaseException", "BlockingIOError", "BrokenPipeError", "BufferError", "BytesWarning", "ChildProcessError",
            "ConnectionAbortedError", "ConnectionError", "ConnectionRefusedError", "ConnectionResetError",
            "DeprecationWarning", "EOFError", "Ellipsis", "EnvironmentError", "Exception", "False", "FileExistsError",
            "FileNotFoundError", "FloatingPointError", "FutureWarning", "GeneratorExit", "IOError", "ImportError",
            "ImportWarning", "IndentationError", "IndexError", "InterruptedError", "IsADirectoryError", "KeyError",
            "KeyboardInterrupt", "LookupError", "MemoryError", "NameError", "None", "NotADirectoryError",
            "NotImplemented", "NotImplementedError", "OSError", "OverflowError", "PendingDeprecationWarning",
            "PermissionError", "ProcessLookupError", "RecursionError", "ReferenceError", "ResourceWarning",
            "RuntimeError", "RuntimeWarning", "StopAsyncIteration", "StopIteration", "SyntaxError", "SyntaxWarning",
            "SystemError", "SystemExit", "TabError", "TimeoutError", "True", "TypeError", "UnboundLocalError",
            "UnicodeDecodeError", "UnicodeEncodeError", "UnicodeError", "UnicodeTranslateError", "UnicodeWarning",
            "UserWarning", "ValueError", "Warning", "ZeroDivisionError", "__build_class__", "__debug__", "__doc__",
            "__import__", "__loader__", "__name__", "__package__", "__spec__", "abs", "all", "any", "ascii", "bin",
            "bool", "bytearray", "bytes", "callable", "chr", "classmethod", "compile", "complex", "copyright",
            "credits", "delattr", "dict", "dir", "divmod", "enumerate", "eval", "exec", "exit", "filter", "float",
            "format", "frozenset", "getattr", "globals", "hasattr", "hash", "help", "hex", "id", "input", "int",
            "isinstance", "issubclass", "iter", "len", "license", "list", "locals", "map", "max", "memoryview", "min",
            "next", "object", "oct", "open", "ord", "pow", "print", "property", "quit", "range", "repr", "reversed",
            "round", "set", "setattr", "slice", "sorted", "staticmethod", "str", "sum", "super", "tuple", "type",
            "vars", "zip"));

    public PythonGenerator(ParseTreeProperty<Scope> scopes) {
        this.scopes = scopes;
    }

    /**
     * Generates Python code for the program.
     * 
     * @param ctx the program context
     * @return the generated Python code
     */
    @Override
    public String visitProgram(OFPParser.ProgramContext ctx) {
        StringBuilder progStmt = new StringBuilder();

        for (int i = 0; i < ctx.funcDecl().size(); i++) {
            progStmt.append(visit(ctx.funcDecl(i)));
        }

        if (ctx.main() != null) {
            progStmt.append(visit(ctx.main()));
        }

        return progStmt.toString();
    }

    /**
     * Generates Python code for the main function.
     * 
     * @param ctx the main context
     * @return the generated Python code for main
     */
    @Override
    public String visitMain(OFPParser.MainContext ctx) {
        int previousDepth = depth;
        depth = -1;

        String mainStmt = visit(ctx.block());
        depth = previousDepth;

        return mainStmt;
    }

    /**
     * Generates Python code for a function declaration.
     * 
     * @param ctx the function declaration context
     * @return the generated Python code for the function
     */
    @Override
    public String visitFuncDecl(OFPParser.FuncDeclContext ctx) {
        StringBuilder functionDeclStmt = new StringBuilder();

        String functionName = getSafeId(ctx.ID(0).getText());

        currentScope = scopes.get(ctx);
        FunctionSymbol functionSymbol = (FunctionSymbol) currentScope.resolve(ctx.ID(0).getText());

        functionDeclStmt.append(indent() + "def " + functionName + "(");

        List<Symbol> params = functionSymbol.getParameters();
        for (int i = 0; i < params.size(); i++) {
            String paramName = getSafeId(params.get(i).getName());
            functionDeclStmt.append(paramName);
            if (i < params.size() - 1) {
                functionDeclStmt.append(", ");
            }
        }

        functionDeclStmt.append("):\n" + visit(ctx.block()) + "\n");

        return functionDeclStmt.toString();
    }

    /**
     * Generates Python code for a function call.
     * 
     * @param ctx the function call context
     * @return the generated Python code for the function call
     */
    @Override
    public String visitFuncCall(OFPParser.FuncCallContext ctx) {
        StringBuilder functionCallStmt = new StringBuilder();

        String functionName = getSafeId(ctx.ID().getText());

        functionCallStmt.append(functionName).append("(");

        for (int i = 0; i < ctx.expr().size(); i++) {
            functionCallStmt.append(visit(ctx.expr(i)));
            if (i < ctx.expr().size() - 1) {
                functionCallStmt.append(", ");
            }
        }

        functionCallStmt.append(")");

        return functionCallStmt.toString();
    }

    /**
     * Generates Python code for a block of statements.
     * 
     * @param ctx the block context
     * @return the generated Python code for the block
     */
    @Override
    public String visitBlock(OFPParser.BlockContext ctx) {
        StringBuilder blockStmt = new StringBuilder();

        depth++;
        if (ctx.stmt().isEmpty())
            blockStmt.append(indent()).append("\tpass\n");
        else {
            for (int i = 0; i < ctx.getChildCount(); i++) {
                String stmt = visit(ctx.getChild(i));
                if (stmt != null && !stmt.isEmpty()) {
                    blockStmt.append(indent()).append(stmt);
                }
            }
        }
        depth--;

        return blockStmt.toString();
    }

    /**
     * Generates Python code for print and println statements.
     * 
     * @param ctx the print statement context
     * @return the generated Python code for the print statement
     */
    @Override
    public String visitPrintStmt(OFPParser.PrintStmtContext ctx) {
        StringBuilder printStmt = new StringBuilder();

        if ("print".equals(ctx.getChild(0).getText())) {
            printStmt.append(indent()).append("print(");

            if (ctx.expr() != null) {
                printStmt.append(visit(ctx.expr()));
            }

            printStmt.append(", end='')");
        } else if ("println".equals(ctx.getChild(0).getText())) {
            printStmt.append(indent()).append("print(");

            if (ctx.expr() != null) {
                printStmt.append(visit(ctx.expr()));
            }

            printStmt.append(")");
        }

        printStmt.append("\n");

        return printStmt.toString();
    }

    /**
     * Generates Python code for a function call statement.
     * 
     * @param ctx the function call statement context
     * @return the generated Python code for the statement
     */
    @Override
    public String visitFuncCallStmt(OFPParser.FuncCallStmtContext ctx) {
        return indent() + visit(ctx.funcCall()) + "\n";
    }

    /**
     * Generates Python code for an assignment statement.
     * 
     * @param ctx the assignment statement context
     * @return the generated Python code for the assignment
     */
    @Override
    public String visitAssignStmt(OFPParser.AssignStmtContext ctx) {
        String varName = getSafeId(ctx.ID().getText());

        if (ctx.expr(1) == null) {
            String assignExpr = visit(ctx.expr(0));
            return indent() + varName + " = " + assignExpr + "\n";
        } else {
            String assignIndex = ctx.expr(0).getText();
            String assignExpr = visit(ctx.expr(1));
            return indent() + varName + "[" + assignIndex + "]" + " = " + assignExpr + "\n";
        }
    }

    /**
     * Generates Python code for a variable declaration statement.
     * 
     * @param ctx the variable declaration statement context
     * @return the generated Python code for the declaration
     */
    @Override
    public String visitVarDeclStmt(OFPParser.VarDeclStmtContext ctx) {
        String varName = getSafeId(ctx.ID().getText());
        String expr = ctx.expr() != null ? visit(ctx.expr()) : "None";

        return indent() + varName + " = " + expr + "\n";
    }

    /**
     * Generates Python code for an if statement (with optional elif/else).
     * 
     * @param ctx the if statement context
     * @return the generated Python code for the if statement
     */
    @Override
    public String visitIfStmt(OFPParser.IfStmtContext ctx) {
        StringBuilder ifStmt = new StringBuilder();

        ifStmt.append(indent())
                .append("if ")
                .append(visit(ctx.expr()))
                .append(":\n");

        ifStmt.append(visit(ctx.block(0)));

        if (ctx.block(1) != null) {
            if (ctx.block(1).getChild(0) instanceof OFPParser.IfStmtContext) {
                OFPParser.IfStmtContext elif = (OFPParser.IfStmtContext) ctx.block(1).stmt(0);

                depth *= 2;
                ifStmt.append(indent()).append("elif ");
                depth /= 2;

                ifStmt.append(visit(elif.expr()))
                        .append(":\n")
                        .append(visit(elif.block(0)));

                if (elif.block(1) != null) {
                    depth *= 2;
                    ifStmt.append(indent()).append("else:\n");
                    depth /= 2;
                    ifStmt.append(visit(elif.block(1)));
                }

            } else {
                depth *= 2;
                ifStmt.append(indent()).append("else:\n");
                depth /= 2;
                ifStmt.append(visit(ctx.block(1)));

            }
        }

        return ifStmt.toString();
    }

    /**
     * Generates Python code for a while statement.
     * 
     * @param ctx the while statement context
     * @return the generated Python code for the while statement
     */
    @Override
    public String visitWhileStmt(OFPParser.WhileStmtContext ctx) {
        StringBuilder whileStmt = new StringBuilder();

        whileStmt.append(indent())
                .append("while ")
                .append(visit(ctx.expr()))
                .append(":\n");

        whileStmt.append(visit(ctx.block()));

        return whileStmt.toString();
    }

    /**
     * Generates Python code for a return statement.
     * 
     * @param ctx the return statement context
     * @return the generated Python code for the return statement
     */
    @Override
    public String visitReturnStmt(OFPParser.ReturnStmtContext ctx) {
        StringBuilder returnStmt = new StringBuilder();

        returnStmt.append(indent()).append("return ");
        returnStmt.append(visit(ctx.expr()));
        returnStmt.append("\n");

        return returnStmt.toString();
    }

    /**
     * Generates Python code for array initialization expressions.
     * 
     * @param ctx the array initialization expression context
     * @return the generated Python code for the array initialization
     */
    @Override
    public String visitArrayInitExpr(OFPParser.ArrayInitExprContext ctx) {
        StringBuilder arrayInit = new StringBuilder();

        if ("new".equals(ctx.getChild(0).getText())) {
            String size = visit(ctx.expr(0));
            arrayInit.append("[0]*").append(size);
        } else {
            arrayInit.append("[");
            for (int i = 0; i < ctx.expr().size(); i++) {
                if (i > 0) {
                    arrayInit.append(", ");
                }
                arrayInit.append(visit(ctx.expr(i)));
            }
            arrayInit.append("]");
        }

        return arrayInit.toString();
    }

    /**
     * Generates Python code for array access expressions.
     * 
     * @param ctx the array access expression context
     * @return the generated Python code for the array access
     */
    @Override
    public String visitArrayAccessExpr(OFPParser.ArrayAccessExprContext ctx) {
        String varName = getSafeId(ctx.ID().getText());

        return varName + "[" + visit(ctx.expr()) + "]";
    }

    /**
     * Generates Python code for array length expressions.
     * 
     * @param ctx the array length expression context
     * @return the generated Python code for the length expression
     */
    @Override
    public String visitArrayLengthExpr(OFPParser.ArrayLengthExprContext ctx) {
        StringBuilder lengthExpr = new StringBuilder();

        String expr = visit(ctx.expr());

        lengthExpr.append("len(")
                .append(expr)
                .append(")");

        return lengthExpr.toString();
    }

    /**
     * Generates Python code for parenthesized expressions.
     * 
     * @param ctx the parenthesized expression context
     * @return the generated Python code for the expression
     */
    @Override
    public String visitParenExpr(OFPParser.ParenExprContext ctx) {
        StringBuilder parenthesesExpr = new StringBuilder();

        parenthesesExpr.append("(")
                .append(visit(ctx.expr()))
                .append(")");

        return parenthesesExpr.toString();
    }

    /**
     * Generates Python code for unary expressions.
     * 
     * @param ctx the unary expression context
     * @return the generated Python code for the unary expression
     */
    @Override
    public String visitUnaryExpr(OFPParser.UnaryExprContext ctx) {
        StringBuilder unaryExpr = new StringBuilder();

        unaryExpr.append("-");

        unaryExpr.append(visit(ctx.expr()));

        return unaryExpr.toString();
    }

    /**
     * Generates Python code for multiplication/division expressions.
     * 
     * @param ctx the multiplication/division expression context
     * @return the generated Python code for the expression
     */
    @Override
    public String visitMultExpr(OFPParser.MultExprContext ctx) {
        StringBuilder multExpr = new StringBuilder();

        multExpr.append(visit(ctx.expr(0)))
                .append(ctx.getChild(1).getText())
                .append(visit(ctx.expr(1)));

        return multExpr.toString();
    }

    /**
     * Generates Python code for addition/subtraction expressions.
     * 
     * @param ctx the addition/subtraction expression context
     * @return the generated Python code for the expression
     */
    @Override
    public String visitAddiExpr(OFPParser.AddiExprContext ctx) {
        StringBuilder addiExpr = new StringBuilder();

        addiExpr.append(visit(ctx.expr(0)))
                .append(" ")
                .append(ctx.getChild(1).getText())
                .append(" ")
                .append(visit(ctx.expr(1)));

        return addiExpr.toString();
    }

    /**
     * Generates Python code for relational expressions.
     * 
     * @param ctx the relational expression context
     * @return the generated Python code for the expression
     */
    @Override
    public String visitRelExpr(OFPParser.RelExprContext ctx) {
        StringBuilder relationalExpr = new StringBuilder();

        relationalExpr.append(visit(ctx.expr(0)))
                .append(" ")
                .append(ctx.getChild(1).getText())
                .append(" ")
                .append(visit(ctx.expr(1)));

        return relationalExpr.toString();
    }

    /**
     * Generates Python code for equality expressions.
     * 
     * @param ctx the equality expression context
     * @return the generated Python code for the expression
     */
    @Override
    public String visitEqExpr(OFPParser.EqExprContext ctx) {
        StringBuilder equalityExpr = new StringBuilder();

        equalityExpr.append(visit(ctx.expr(0)))
                .append(" ")
                .append(ctx.getChild(1).getText())
                .append(" ")
                .append(visit(ctx.expr(1)));

        return equalityExpr.toString();
    }

    /**
     * Returns the Python code for an integer literal.
     * 
     * @param ctx the integer expression context
     * @return the integer literal as a string
     */
    @Override
    public String visitIntExpr(OFPParser.IntExprContext ctx) {
        return ctx.INT().getText();
    }

    /**
     * Returns the Python code for a float literal.
     * 
     * @param ctx the float expression context
     * @return the float literal as a string
     */
    @Override
    public String visitFloatExpr(OFPParser.FloatExprContext ctx) {
        return ctx.FLOAT().getText();
    }

    /**
     * Returns the Python code for a boolean literal.
     * 
     * @param ctx the boolean expression context
     * @return the boolean literal as a string
     */
    @Override
    public String visitBoolExpr(OFPParser.BoolExprContext ctx) {
        if ("true".equals(ctx.BOOLEAN().getText())) {
            return "True";
        } else {
            return "False";
        }
    }

    /**
     * Returns the Python code for a char literal.
     * 
     * @param ctx the char expression context
     * @return the char literal as a string
     */
    @Override
    public String visitCharExpr(OFPParser.CharExprContext ctx) {
        return ctx.CHAR().getText();
    }

    /**
     * Returns the Python code for a string literal.
     * 
     * @param ctx the string expression context
     * @return the string literal as a string
     */
    @Override
    public String visitStringExpr(OFPParser.StringExprContext ctx) {
        return ctx.STRING().getText();
    }

    /**
     * Returns the Python code for a variable reference, handling reserved
     * identifiers.
     * 
     * @param ctx the ID expression context
     * @return the variable name as a string
     */
    @Override
    public String visitIDExpr(OFPParser.IDExprContext ctx) {
        return getSafeId(ctx.ID().getText());
    }

    /**
     * Returns the current indentation string based on depth.
     * 
     * @return the indentation string
     */
    private String indent() {
        return " ".repeat(depth * 2);
    }

    /**
     * Returns a safe identifier for Python, prefixing reserved words.
     * 
     * @param id the identifier to check
     * @return a safe identifier string
     */
    private static String getSafeId(String id) {
        if (reservedIds.contains(id)) {
            return "ofp_" + id;
        }
        return id;
    }
}
