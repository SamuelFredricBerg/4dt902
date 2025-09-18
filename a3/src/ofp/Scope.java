package ofp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Scope {
    private Scope enclosingScope; // null if global scope
    private Map<String, Symbol> symbols = new LinkedHashMap<String, Symbol>();
    private List<Scope> childScopes = new ArrayList<>();
    private FunctionSymbol functionSymbol; // if scope is a function scope

    public Scope(Scope enclosingScope) {
        this.enclosingScope = enclosingScope;
    }

    public Scope getEnclosingScope() {
        return enclosingScope;
    }

    public Map<String, Symbol> getSymbols() {
        return symbols;
    }

    public void addChildScope(Scope childScope) {
        childScopes.add(childScope);
    }

    public List<Scope> getChildScopes() {
        return childScopes;
    }

    public void setFunctionSymbol(FunctionSymbol functionSymbol) {
        this.functionSymbol = functionSymbol;
    }

    public FunctionSymbol getFunctionSymbol() {
        return this.functionSymbol;
    }

    public void define(Symbol sym) {
        if (symbols.containsKey(sym.getName()))
            return;
        symbols.put(sym.getName(), sym);
    }

    public Symbol resolve(String name) {
        Symbol sym = symbols.get(name);
        if (sym != null) {
            return sym;
        } else if (enclosingScope != null) {
            return enclosingScope.resolve(name);
        } else
            return null; // not found
    }

    public Symbol localResolve(String name) {
        Symbol sym = symbols.get(name);
        if (sym != null)
            return sym;
        else
            return null; // not found
    }

    @Override
    public String toString() {
        return symbols.keySet().toString();
    }
}
