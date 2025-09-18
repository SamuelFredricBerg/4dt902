package ofp;

import java.util.List;
import java.util.HashSet;
import java.util.Arrays;

import org.antlr.v4.runtime.tree.ParseTreeProperty;

import generated.OFPBaseVisitor;
import generated.OFPParser;

public class PythonGenerator extends OFPBaseVisitor<String> {
    private int depth = 0;
    private ParseTreeProperty<Scope> scopes;
    private Scope currentScope;

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

    private String indent() {
        return " ".repeat(depth * 4);
    }

    private static String getSafeId(String id) {
        if (reservedIds.contains(id)) {
            return "ofp_" + id; // Prefix with ofp_
        }
        return id;
    }

    public PythonGenerator(ParseTreeProperty<Scope> scopes) {
        this.scopes = scopes;
    }

    @Override
    public String visitIDExpr(OFPParser.IDExprContext ctx) {
        return getSafeId(ctx.ID().getText());
    }

    @Override
    public String visitIntExpr(OFPParser.IntExprContext ctx) {
        return ctx.INT().getText();
    }

    @Override
    public String visitFloatExpr(OFPParser.FloatExprContext ctx) {
        return ctx.FLOAT().getText();
    }

    @Override
    public String visitBoolExpr(OFPParser.BoolExprContext ctx) {
        if ("true".equals(ctx.BOOLEAN().getText())) {
            return "True";
        } else {
            return "False";
        }
    }

    @Override
    public String visitCharExpr(OFPParser.CharExprContext ctx) {
        return ctx.CHAR().getText();
    }

    @Override
    public String visitStringExpr(OFPParser.StringExprContext ctx) {
        return ctx.STRING().getText();
    }

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

    @Override
    public String visitMain(OFPParser.MainContext ctx) {
        StringBuilder mainStmt = new StringBuilder();

        mainStmt.append("#\n# Program entry point - main\n#\n");

        int previousDepth = depth;
        depth = -1;

        mainStmt.append(visit(ctx.block()));

        depth = previousDepth;

        return mainStmt.toString();
    }

    @Override
    public String visitFuncDecl(OFPParser.FuncDeclContext ctx) {
        StringBuilder functionDeclStmt = new StringBuilder();

        String functionName = getSafeId(ctx.ID(0).getText());

        currentScope = scopes.get(ctx);
        FunctionSymbol functionSymbol = (FunctionSymbol) currentScope.resolve(ctx.ID(0).getText());

        functionDeclStmt.append(indent())
                .append("def ")
                .append(functionName)
                .append("(");

        List<Symbol> params = functionSymbol.getParameters();
        for (int i = 0; i < params.size(); i++) {
            String paramName = getSafeId(params.get(i).getName());
            functionDeclStmt.append(paramName);
            if (i < params.size() - 1) {
                functionDeclStmt.append(", ");
            }
        }

        functionDeclStmt.append("):\n");

        functionDeclStmt.append(visit(ctx.block()));

        functionDeclStmt.append("\n");

        return functionDeclStmt.toString();
    }

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

    @Override
    public String visitFuncCallStmt(OFPParser.FuncCallStmtContext ctx) {
        return indent() + visit(ctx.funcCall()) + "\n";
    }

    @Override
    public String visitReturnStmt(OFPParser.ReturnStmtContext ctx) {
        StringBuilder returnStmt = new StringBuilder();

        returnStmt.append(indent()).append("return ");
        returnStmt.append(visit(ctx.expr()));
        returnStmt.append("\n");

        return returnStmt.toString();
    }

    @Override
    public String visitBlock(OFPParser.BlockContext ctx) {
        StringBuilder blockStmt = new StringBuilder();

        depth++;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            String stmt = visit(ctx.getChild(i));
            if (stmt != null && !stmt.isEmpty()) {
                blockStmt.append(indent()).append(stmt);
            }
        }
        depth--;

        return blockStmt.toString();
    }

    @Override
    public String visitVarDeclStmt(OFPParser.VarDeclStmtContext ctx) {
        String varName = getSafeId(ctx.ID().getText()); // vänsterled
        String expr = ctx.expr() != null ? visit(ctx.expr()) : "None"; // högerled

        return indent() + varName + " = " + expr + "\n";
    }

    @Override
    public String visitAssignStmt(OFPParser.AssignStmtContext ctx) {
        // Handle array assignments, look at TypeCheckingVisitor.

        String varName = getSafeId(ctx.ID().getText());
        String expr = visit(ctx.expr(1));

        return indent() + varName + " = " + expr + "\n";
    }

    // // Visit array access, for example a[0] = 5;
    // // line 33 in parser.g4
    // TODO: @Override
    // public String visitArrayAssign(OFPParser.ArrayAssignContext ctx) {
    // StringBuilder arrayAssignStmt = new StringBuilder();

    // String arrayVarStmt = visit(ctx.arrayVar());

    // String valueExpr = visit(ctx.expr());

    // arrayAssignStmt.append(indent())
    // .append(arrayVarStmt)
    // .append(" = ")
    // .append(valueExpr)
    // .append("\n");

    // return arrayAssignStmt.toString();
    // }

    @Override
    public String visitArrayAccessExpr(OFPParser.ArrayAccessExprContext ctx) {
        // look at TypeCheckingVisitor.

        return null;
    }

    // // Visit array declaration, for example int[] a;
    // TODO: @Override
    // public String visitArrayVar(OFPParser.ArrayVarContext ctx) {
    // StringBuilder arrayAccessStmt = new StringBuilder();

    // String arrayName = getSafeId(ctx.ID().getText());

    // arrayAccessStmt.append(arrayName)
    // .append("[")
    // .append(visit(ctx.expr()))
    // .append("]");

    // // f[expr?]

    // return arrayAccessStmt.toString();
    // }

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

    @Override
    public String visitArrayLengthExpr(OFPParser.ArrayLengthExprContext ctx) {
        StringBuilder lengthExpr = new StringBuilder();

        String expr = visit(ctx.expr());

        lengthExpr.append("len(")
                .append(expr)
                .append(")");

        return lengthExpr.toString();
    }

    @Override
    public String visitUnaryExpr(OFPParser.UnaryExprContext ctx) {
        StringBuilder unaryExpr = new StringBuilder();

        unaryExpr.append("-");

        unaryExpr.append(visit(ctx.expr()));

        return unaryExpr.toString();
    }

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

    public String visitParenExpr(OFPParser.ParenExprContext ctx) {
        StringBuilder parenthesesExpr = new StringBuilder();

        parenthesesExpr.append("(")
                .append(visit(ctx.expr()))
                .append(")");

        return parenthesesExpr.toString();
    }

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

    @Override
    public String visitMultExpr(OFPParser.MultExprContext ctx) {
        StringBuilder multExpr = new StringBuilder();

        multExpr.append(visit(ctx.expr(0)))
                .append(ctx.getChild(1).getText())
                .append(visit(ctx.expr(1)));

        return multExpr.toString();
    }

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
}
