package lexor.core.interpreter;

import lexor.core.lexer.TokenType;

/**
 * Enforces Lexor's strong typing rules.
 * Called by the Interpreter before every assignment and binary operation.
 */
public class TypeChecker {

    /**
     * Checks that a value being assigned to a variable matches its declared type.
     * FLOAT variables may accept INT values (widening).
     */
    public static LexorValue checkAssign(String varName,
                                         TokenType declaredType,
                                         LexorValue value,
                                         int line) {
        LexorValue.Type vt = value.getType();

        switch (declaredType) {
            case INT:
                if (vt == LexorValue.Type.INT) return value;
                break;
            case FLOAT:
                if (vt == LexorValue.Type.FLOAT) return value;
                if (vt == LexorValue.Type.INT)
                    return LexorValue.ofFloat(value.asInt()); // widen
                break;
            case CHAR:
                if (vt == LexorValue.Type.CHAR) return value;
                break;
            case BOOL:
                if (vt == LexorValue.Type.BOOL) return value;
                break;
            default:
                break;
        }

        throw new RuntimeException("[Line " + line + "] Type error: cannot assign "
                + vt + " to variable '" + varName + "' of type " + declaredType);
    }

    /**
     * Ensures both operands of a binary arithmetic operation are numeric.
     */
    public static void checkNumeric(LexorValue left, LexorValue right,
                                    String op, int line) {
        if (!left.isNumeric() || !right.isNumeric()) {
            throw new RuntimeException("[Line " + line + "] Type error: operator '"
                    + op + "' requires numeric operands, got "
                    + left.getType() + " and " + right.getType());
        }
    }

    /**
     * Ensures both operands of a comparison are the same base type.
     */
    public static void checkComparable(LexorValue left, LexorValue right,
                                       String op, int line) {
        boolean ok = (left.isNumeric() && right.isNumeric())
                || (left.getType() == right.getType());
        if (!ok) {
            throw new RuntimeException("[Line " + line + "] Type error: cannot compare "
                    + left.getType() + " and " + right.getType()
                    + " with '" + op + "'");
        }
    }

    /**
     * Ensures an operand is BOOL (for NOT, AND, OR).
     */
    public static void checkBool(LexorValue value, String op, int line) {
        if (value.getType() != LexorValue.Type.BOOL) {
            throw new RuntimeException("[Line " + line + "] Type error: operator '"
                    + op + "' requires BOOL operand, got " + value.getType());
        }
    }
}