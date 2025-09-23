package ofp;

public class Symbol {
    private String name;
    private OFPType type;
    private int pointer;

    public Symbol(String name, OFPType type) {
        this.name = name;
        this.type = type;
        this.pointer = -1;
    }

    public void setPointer(int pointer) {
        this.pointer = pointer;
    }

    public int getPointer() {
        return pointer;
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
