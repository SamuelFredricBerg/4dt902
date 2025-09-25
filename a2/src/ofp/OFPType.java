package ofp;

/**
 * Represents of the different types allowed in the OFP language.
 * Provides constants for supported types and utility methods for type lookup.
 */
public class OFPType {
    public static final OFPType INT = new OFPType("int");
    public static final OFPType INT_ARRAY = new OFPType("int[]");

    public static final OFPType FLOAT = new OFPType("float");
    public static final OFPType FLOAT_ARRAY = new OFPType("float[]");

    public static final OFPType BOOLEAN = new OFPType("bool");

    public static final OFPType STRING = new OFPType("string");

    public static final OFPType ARGS = new OFPType("String[]");

    public static final OFPType CHAR = new OFPType("char");
    public static final OFPType CHAR_ARRAY = new OFPType("char[]");

    public static final OFPType VOID = new OFPType("void");

    public static final OFPType ERROR = new OFPType("error");

    /**
     * Returns the OFPType constant for the given type name.
     * 
     * @param typeName the name of the type
     * @return the corresponding OFPType, or null if not found
     */
    public static OFPType getTypeFor(String typeName) {
        switch (typeName) {
            case "int":
                return INT;

            case "int[]":
                return INT_ARRAY;

            case "float":
                return FLOAT;

            case "float[]":
                return FLOAT_ARRAY;

            case "bool":
                return BOOLEAN;

            case "string":
                return STRING;

            case "char":
                return CHAR;

            case "char[]":
                return CHAR_ARRAY;

            case "string[]":
                return ARGS;

            case "void":
                return VOID;

            default:
                return null;
        }
    }

    private final String name;

    private OFPType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
