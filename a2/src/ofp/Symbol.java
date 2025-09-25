package ofp;

/**
 * Represents a symbol in the OFP language, such as a variable and parameter.
 * Stores the symbol's name, type, and pointer (e.g., memory location).
 */
public class Symbol {
    private String name;
    private OFPType type;
    private int pointer;

    /**
     * Constructs a Symbol with the given name and type.
     * The pointer is initialized to -1.
     *
     * @param name the name of the symbol
     * @param type the type of the symbol
     */
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
