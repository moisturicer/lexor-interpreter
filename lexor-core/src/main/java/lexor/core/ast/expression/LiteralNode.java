package lexor.core.ast.expression;

import lexor.core.ast.Node;
import lexor.core.ast.NodeVisitor;
import lexor.core.lexer.TokenType;

/**
 * Represents a literal value in source code.
 *
 * TokenType indicates the kind of literal:
 *   INT_LITERAL    — e.g. 42
 *   FLOAT_LITERAL  — e.g. 3.14
 *   CHAR_LITERAL   — e.g. 'A'
 *   STRING_LITERAL — e.g. "hello"  (also used for [#] escape sequences)
 *   TRUE / FALSE   — boolean literals
 *
 * The raw string value from the source is stored as-is;
 * the Interpreter converts it to the appropriate Java type.
 */
public class LiteralNode extends Node {

    private final TokenType type;
    private final String value; // raw string from source

    public LiteralNode(int line, TokenType type, String value) {
        super(line);
        this.type  = type;
        this.value = value;
    }

    public TokenType getType() { return type; }
    public String getValue()   { return value; }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitLiteral(this);
    }
}