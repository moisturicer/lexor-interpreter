package lexor.core.ast.expression;

import lexor.core.ast.Node;
import lexor.core.ast.NodeVisitor;
import lexor.core.lexer.TokenType;

/**
 * Represents a unary expression: <op> <operand>.
 *
 * Covers:
 *   NOT <bool-expr>
 *   - <numeric-expr>   (unary minus)
 *
 * Examples:
 *   NOT flag
 *   -x
 */
public class UnaryOpNode extends Node {

    private final TokenType operator;
    private final Node operand;

    public UnaryOpNode(int line, TokenType operator, Node operand) {
        super(line);
        this.operator = operator;
        this.operand  = operand;
    }

    public TokenType getOperator() { return operator; }
    public Node getOperand()       { return operand; }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitUnaryOp(this);
    }
}