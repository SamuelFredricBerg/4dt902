package ofp;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a function symbol in the OFP language.
 * Stores the function's name, return type, and parameters.
 */
public class FunctionSymbol extends Symbol {
    private OFPType returnType;
    private List<Symbol> parameters;

    /**
     * Constructs a FunctionSymbol with the given name and return type.
     * Initializes an empty list of parameters.
     *
     * @param name       the name of the function
     * @param returnType the return type of the function
     */
    public FunctionSymbol(String name, OFPType returnType) {
        super(name, returnType);
        this.returnType = returnType;
        this.parameters = new ArrayList<>();
    }

    public OFPType getReturnType() {
        return returnType;
    }

    public void addParameter(Symbol param) {
        parameters.add(param);
    }

    public List<Symbol> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return "Function: " + getName() + ", Returns type: " + returnType + ", Params: " + parameters;
    }
}
