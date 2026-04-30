package lexor.core.interpreter;

import lexor.core.lexer.TokenType;

public class LexorValue {

    public enum Type { INT, FLOAT, CHAR, BOOL, STRING }

    private final Type type;
    private final Object value;

    private LexorValue(Type type, Object value) {
        this.type  = type;
        this.value = value;
    }

    public static LexorValue ofInt(int v)       { return new LexorValue(Type.INT,    v); }
    public static LexorValue ofFloat(double v)  { return new LexorValue(Type.FLOAT,  v); }
    public static LexorValue ofChar(char v)     { return new LexorValue(Type.CHAR,   v); }
    public static LexorValue ofBool(boolean v)  { return new LexorValue(Type.BOOL,   v); }
    public static LexorValue ofString(String v) { return new LexorValue(Type.STRING, v); }

    public static LexorValue defaultFor(TokenType dataType) {
        switch (dataType) {
            case INT:   return ofInt(0);
            case FLOAT: return ofFloat(0.0);
            case CHAR:  return ofChar('\0');
            case BOOL:  return ofBool(false);
            default: throw new IllegalArgumentException("unknown data type: " + dataType);
        }
    }

    public Type getType() { return type; }

    public int asInt() {
        if (type == Type.INT) return (int) value;
        throw new RuntimeException("expected INT, got " + type);
    }

    public double asFloat() {
        if (type == Type.FLOAT) return (double) value;
        if (type == Type.INT)   return (double)(int) value;
        throw new RuntimeException("expected FLOAT, got " + type);
    }

    public char asChar() {
        if (type == Type.CHAR) return (char) value;
        throw new RuntimeException("expected CHAR, got " + type);
    }

    public boolean asBool() {
        if (type == Type.BOOL) return (boolean) value;
        throw new RuntimeException("expected BOOL, got " + type);
    }

    public String asString() {
        if (type == Type.STRING) return (String) value;
        throw new RuntimeException("expected STRING, got " + type);
    }

    public boolean isNumeric() {
        return type == Type.INT || type == Type.FLOAT;
    }

    @Override
    public String toString() {
        if (type == Type.BOOL)   return (boolean) value ? "TRUE" : "FALSE";
        if (type == Type.CHAR)   return String.valueOf((char) value);
        if (type == Type.STRING) return (String) value;
        return String.valueOf(value);
    }
}