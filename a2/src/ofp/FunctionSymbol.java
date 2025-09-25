package ofp;

import java.util.ArrayList;
import java.util.List;

public class FunctionSymbol extends Symbol {
    private OFPType returnType;
    private List<Symbol> parameters;

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
