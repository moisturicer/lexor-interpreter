package lexor.core.interpreter;

import lexor.core.lexer.TokenType;

/**
 * Runtime value wrapper for all Lexor data types: INT, FLOAT, CHAR, BOOL.
 */
public class LexorValue {

    public enum Type { INT, FLOAT, CHAR, BOOL }

    private final Type type;
    private final Object value;

    private LexorValue(Type type, Object value) {
        this.type  = type;
        this.value = value;
    }

    // ── factories ──────────────────────────────────────────────

    public static LexorValue ofInt(int v)       { return new LexorValue(Type.INT,   v); }
    public static LexorValue ofFloat(double v)  { return new LexorValue(Type.FLOAT, v); }
    public static LexorValue ofChar(char v)     { return new LexorValue(Type.CHAR,  v); }
    public static LexorValue ofBool(boolean v)  { return new LexorValue(Type.BOOL,  v); }

    public static LexorValue defaultFor(TokenType dataType) {
        switch (dataType) {
            case INT:   return ofInt(0);
            case FLOAT: return ofFloat(0.0);
            case CHAR:  return ofChar('\0');
            case BOOL:  return ofBool(false);
            default: throw new IllegalArgumentException("Unknown data type: " + dataType);
        }
    }

    // ── accessors ──────────────────────────────────────────────

    public Type getType() { return type; }

    public int asInt() {
        if (type == Type.INT) return (int) value;
        throw new RuntimeException("Expected INT, got " + type);
    }

    public double asFloat() {
        if (type == Type.FLOAT) return (double) value;
        if (type == Type.INT)   return ((int) value);   // implicit widening
        throw new RuntimeException("Expected FLOAT, got " + type);
    }

    public char asChar() {
        if (type == Type.CHAR) return (char) value;
        throw new RuntimeException("Expected CHAR, got " + type);
    }

    public boolean asBool() {
        if (type == Type.BOOL) return (boolean) value;
        throw new RuntimeException("Expected BOOL, got " + type);
    }

    public boolean isNumeric() {
        return type == Type.INT || type == Type.FLOAT;
    }

    // ── display ────────────────────────────────────────────────

    @Override
    public String toString() {
        if (type == Type.BOOL) return (boolean) value ? "TRUE" : "FALSE";
        return String.valueOf(value);
    }
}