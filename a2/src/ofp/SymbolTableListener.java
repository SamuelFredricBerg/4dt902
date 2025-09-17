package ofp;

import org.antlr.v4.runtime.tree.ParseTreeProperty;

import generated.OFPBaseListener;
import generated.OFPParser;

//* Note: Only one of the SymbolTableListener.java and SymbolTableVisitor.java needs to be implemented.

public class SymbolTableListener extends OFPBaseListener {
    private Scope currentScope;
    private Scope globalScope;
    private FunctionSymbol currentFunctionSymbol = null;
    private ParseTreeProperty<Scope> scopes = new ParseTreeProperty<Scope>();

    @Override
    public void enterProgram(OFPParser.ProgramContext ctx) {
        // Initialize global scope
        globalScope = new Scope(null);
        currentScope = globalScope;
        scopes.put(ctx, globalScope);
    }

    @Override
    public void enterMain(OFPParser.MainContext ctx) {
        Symbol mainSymbol = globalScope.resolve("main");
        if (mainSymbol != null && mainSymbol instanceof FunctionSymbol) {
            System.err.println("Error: 'main' function is already defined.");
            return;
        }
        currentFunctionSymbol = new FunctionSymbol("main", OFPType.voidType);
        globalScope.define(currentFunctionSymbol);

        currentScope = new Scope(globalScope);
        globalScope.addChildScope(currentScope);
        scopes.put(ctx, currentScope);
    }

    @Override
    public void exitMain(OFPParser.MainContext ctx) {
        currentScope = globalScope;
        currentFunctionSymbol = null;
    }

    @Override
    public void enterFuncDecl(OFPParser.FuncDeclContext ctx) {

    }

    public ParseTreeProperty<Scope> getScope() {
        return scopes;
    }

    public Scope getGlobalScope() {
        return globalScope;
    }

    public void printSymbolTable() {
        System.out.println("===== Symbol Table =====");
        printScope(globalScope, 0);
    }

    private void printScope(Scope scope, int indentLevel) {
        String indent = "  ".repeat(indentLevel);
        System.out.println(indent + "Scope: " + scope);

        for (Symbol symbol : scope.getSymbols().values()) {
            System.out.println(indent + "  Symbol: " + symbol.getName() + " (Type: " + symbol.getType() + ")");
        }

        for (Scope childScope : scope.getChildScopes()) {
            printScope(childScope, indentLevel + 1);
        }
    }
}
