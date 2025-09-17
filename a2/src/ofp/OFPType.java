package ofp;

public class OFPType {
    public static final OFPType intType = new OFPType("int");
    public static final OFPType intArrType = new OFPType("int[]");

    public static final OFPType floatType = new OFPType("float");
    public static final OFPType floatArrType = new OFPType("float[]");

    public static final OFPType boolType = new OFPType("bool");

    public static final OFPType stringType = new OFPType("string");

    public static final OFPType charType = new OFPType("char");
    public static final OFPType charArrType = new OFPType("char[]");

    // Eventually, we will need to generate bytecode for Java main() method
    public static final OFPType argsType = new OFPType("String[]");

    public static final OFPType voidType = new OFPType("void");

    public static final OFPType errorType = new OFPType("error");

    public static OFPType getTypeFor(String typeName) {
        // typeName = typeName.strip();

        switch (typeName) {
            case "int":
                return intType;

            case "int[]":
                return intArrType;

            case "float":
                return floatType;

            case "float[]":
                return floatArrType;

            case "bool":
                return boolType;

            case "string":
                return stringType;

            case "char":
                return charType;

            case "char[]":
                return charArrType;

            case "string[]":
                return argsType;

            case "void":
                return voidType;

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
        return "OFPType: " + name;
    }
}
