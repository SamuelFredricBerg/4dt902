package ofp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a scope in the OFP language.
 * Manages symbols, child scopes, and function association for nested scopes and
 * depth.
 */
public class Scope {
    private Scope enclosingScope;
    private Map<String, Symbol> symbols = new LinkedHashMap<String, Symbol>();
    private List<Scope> childScopes = new ArrayList<>();
    private FunctionSymbol functionSymbol;

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

    /**
     * Defines a symbol in this scope if not already defined.
     *
     * @param sym the symbol to define
     */
    public void define(Symbol sym) {
        if (symbols.containsKey(sym.getName()))
            return;
        symbols.put(sym.getName(), sym);
    }

    /**
     * Resolves a symbol by name, searching this scope and enclosing scopes.
     * Returns the first match found.
     *
     * @param name the name of the symbol
     * @return the resolved symbol, or null if not found
     */
    public Symbol resolve(String name) {
        Symbol sym = symbols.get(name);
        if (sym != null) {
            return sym;
        } else if (enclosingScope != null) {
            return enclosingScope.resolve(name);
        } else
            return null;
    }

    /**
     * Resolves a symbol by name, searching this scope and parameters of the
     * enclosing function scope.
     *
     * @param name the name of the symbol
     * @return the resolved symbol, or null if not found
     */
    public Symbol paramLocalResolve(String name) {
        Symbol sym = symbols.get(name);
        if (sym != null)
            return sym;
        else if (enclosingScope != null && enclosingScope.getFunctionSymbol() != null) {
            for (Symbol param : enclosingScope.getFunctionSymbol().getParameters()) {
                if (param.getName().equals(name)) {
                    return param;
                }
            }
            return null;
        } else
            return null;
    }

    /**
     * Resolves a symbol by name, searching only this scope.
     *
     * @param name the name of the symbol
     * @return the resolved symbol, or null if not found
     */
    public Symbol localResolve(String name) {
        Symbol sym = symbols.get(name);
        if (sym != null)
            return sym;
        else
            return null;
    }

    @Override
    public String toString() {
        return symbols.keySet().toString();
    }
}
