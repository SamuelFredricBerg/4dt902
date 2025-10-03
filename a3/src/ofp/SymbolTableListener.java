package ofp;

import org.antlr.v4.runtime.tree.ParseTreeProperty;

import generated.OFPBaseListener;
import generated.OFPParser;

/**
 * Listener for building and managing the symbol table during parsing.
 * Handles scope creation, symbol definition, and function/variable
 * declarations.
 */
public class SymbolTableListener extends OFPBaseListener {
    private Scope currentScope;
    private Scope globalScope;
    private FunctionSymbol currentFunctionSymbol = null;
    private ParseTreeProperty<Scope> scopes = new ParseTreeProperty<Scope>();

    /**
     * Initializes the global scope at the start of the program.
     *
     * @param ctx the program context
     */
    @Override
    public void enterProgram(OFPParser.ProgramContext ctx) {
        globalScope = new Scope(null);
        currentScope = globalScope;
        scopes.put(ctx, globalScope);
    }

    /**
     * Handles entering the main function, defining its symbol and scope.
     *
     * @param ctx the main context
     */
    @Override
    public void enterMain(OFPParser.MainContext ctx) {
        Symbol mainSymbol = globalScope.resolve("main");
        if (mainSymbol != null && mainSymbol instanceof FunctionSymbol) {
            System.err.println("Error: 'main' function is already defined.");
            return;
        }
        currentFunctionSymbol = new FunctionSymbol("main", OFPType.VOID);
        globalScope.define(currentFunctionSymbol);

        currentScope = new Scope(globalScope);
        globalScope.addChildScope(currentScope);
        scopes.put(ctx, currentScope);
    }

    /**
     * Handles exiting the main function, restoring the global scope.
     *
     * @param ctx the main context
     */
    @Override
    public void exitMain(OFPParser.MainContext ctx) {
        currentScope = globalScope;
        currentFunctionSymbol = null;
    }

    /**
     * Handles entering a function declaration, defining its symbol, scope, and
     * parameters.
     *
     * @param ctx the function declaration context
     */
    @Override
    public void enterFuncDecl(OFPParser.FuncDeclContext ctx) {
        String functionName = ctx.ID(0).getText();
        String returnTypeStr = ctx.getChild(0).getText();
        OFPType returnType = OFPType.getTypeFor(returnTypeStr);

        if (!(ctx.getChild(ctx.getChildCount() - 1) instanceof OFPParser.FuncBlockContext)) {
            System.err.println("Error: Invalid function declaration for '" + functionName
                    + "'. Expected a function body enclosed in '{ }'.");
            return;
        }

        Symbol existingSymbol = currentScope.resolve(functionName);
        if (existingSymbol instanceof FunctionSymbol) {
            System.err.println("Error: Function '" + functionName + "' is already declared within this scope.");
            return;
        }

        currentFunctionSymbol = new FunctionSymbol(functionName, returnType);
        currentScope.define(currentFunctionSymbol);

        Scope functionScope = new Scope(currentScope);
        functionScope.setFunctionSymbol(currentFunctionSymbol);
        currentScope.addChildScope(functionScope);
        currentScope = functionScope;

        if (returnType.equals(OFPType.VOID)) {
            if (ctx.TYPE().size() > 0) {
                for (int i = 0; i < ctx.TYPE().size(); i++) {
                    String paramName = ctx.ID(i + 1).getText();
                    handleFunctionParameters(ctx, functionName, existingSymbol, paramName, i);
                }
            }
        } else {
            if (ctx.TYPE().size() > 1) {
                for (int i = 1; i < ctx.TYPE().size(); i++) {
                    String paramName = ctx.ID(i).getText();
                    handleFunctionParameters(ctx, functionName, existingSymbol, paramName, i);
                }
            }
        }

        scopes.put(ctx, currentScope);
    }

    /**
     * Handles exiting a function declaration, restoring the global scope.
     *
     * @param ctx the function declaration context
     */
    @Override
    public void exitFuncDecl(OFPParser.FuncDeclContext ctx) {
        currentScope = globalScope;
        currentFunctionSymbol = null;
    }

    /**
     * Handles entering a variable declaration statement, defining the variable
     * symbol.
     *
     * @param ctx the variable declaration statement context
     */
    @Override
    public void enterVarDeclStmt(OFPParser.VarDeclStmtContext ctx) {
        String varName = ctx.ID().getText();
        OFPType varType = OFPType.getTypeFor(ctx.TYPE().getText());

        Symbol existingSymbol = currentScope.paramLocalResolve(varName);
        if (existingSymbol != null && !(existingSymbol instanceof FunctionSymbol)) {
            System.err.println("Error: Variable '" + varName + "' is already declared within this scope.");
            return;
        }

        Symbol varSymbol = new Symbol(varName, varType);

        currentScope.define(varSymbol);
    }

    /**
     * Handles entering a function block, creating a new scope for the function
     * block.
     *
     * @param ctx the function block context
     */
    @Override
    public void enterFuncBlock(OFPParser.FuncBlockContext ctx) {
        Scope blockScope = new Scope(currentScope);
        if (currentScope.getFunctionSymbol() != null) {
            blockScope.setFunctionSymbol(currentScope.getFunctionSymbol());
        }

        currentScope.addChildScope(blockScope);
        currentScope = blockScope;
        scopes.put(ctx, blockScope);
    }

    /**
     * Handles exiting a function block, restoring the enclosing scope.
     *
     * @param ctx the function block context
     */
    @Override
    public void exitFuncBlock(OFPParser.FuncBlockContext ctx) {
        if (currentScope != globalScope) {
            currentScope = currentScope.getEnclosingScope();
        }
    }

    /**
     * Handles entering a block, creating a new scope for the block.
     *
     * @param ctx the block context
     */
    @Override
    public void enterBlock(OFPParser.BlockContext ctx) {
        Scope blockScope = new Scope(currentScope);
        if (currentScope.getFunctionSymbol() != null) {
            blockScope.setFunctionSymbol(currentScope.getFunctionSymbol());
        }

        currentScope.addChildScope(blockScope);
        currentScope = blockScope;
        scopes.put(ctx, blockScope);
    }

    /**
     * Handles exiting a block, restoring the enclosing scope.
     *
     * @param ctx the block context
     */
    @Override
    public void exitBlock(OFPParser.BlockContext ctx) {
        if (currentScope != globalScope) {
            currentScope = currentScope.getEnclosingScope();
        }
    }

    /**
     * Handles entering a return statement, associating the current scope.
     *
     * @param ctx the return statement context
     */
    @Override
    public void enterReturnStmt(OFPParser.ReturnStmtContext ctx) {
        if (currentFunctionSymbol != null) {
            scopes.put(ctx, currentScope);
        }
    }

    /**
     * Defines and adds a function parameter symbol to the current function.
     *
     * @param ctx            the function declaration context
     * @param functionName   the name of the function
     * @param existingSymbol an existing symbol with the same name, if any
     * @param paramName      the name of the parameter
     * @param i              the index of the parameter
     */
    private void handleFunctionParameters(OFPParser.FuncDeclContext ctx, String functionName, Symbol existingSymbol,
            String paramName, int i) {
        String paramTypeStr = ctx.TYPE(i).getText();
        OFPType paramType = OFPType.getTypeFor(paramTypeStr);
        Symbol paramSymbol = new Symbol(paramName, paramType);

        Symbol existingParam = currentScope.localResolve(paramName);
        if (existingParam != null && !(existingSymbol instanceof FunctionSymbol)) {
            System.err.println("Error: Parameter '" + paramName + "' is already declared in function '"
                    + functionName + "'.");
            return;
        }

        currentScope.define(paramSymbol);
        currentFunctionSymbol.addParameter(paramSymbol);
    }

    public ParseTreeProperty<Scope> getScope() {
        return scopes;
    }

    public Scope getGlobalScope() {
        return globalScope;
    }

    /**
     * Prints the symbol table, including all scopes and symbols.
     */
    public void printSymbolTable() {
        System.out.println("\n===== Symbol Table =====");
        printScope(globalScope, 0);
    }

    /**
     * Recursively prints the symbols and child scopes for a given scope.
     *
     * @param scope       the scope to print
     * @param indentLevel the indentation level for printing
     */
    private void printScope(Scope scope, int indentLevel) {
        String indent = "    ".repeat(indentLevel);
        System.out.println(indent + "Scope: " + scope);

        for (Symbol symbol : scope.getSymbols().values()) {
            System.out.println(indent + "Symbol: " + symbol.getName() + " (Type: " + symbol.getType() + ")");
        }

        for (Scope childScope : scope.getChildScopes()) {
            printScope(childScope, indentLevel + 1);
        }
    }
}
