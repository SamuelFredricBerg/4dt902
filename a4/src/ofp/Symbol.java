package ofp;

public class Symbol {
    private String name;
    private OFPType type;

    public Symbol(String name, OFPType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public OFPType getType() {
        return type;
    }

    @Override
    public String toString() {
        return name + ": " + type;
    }
}
