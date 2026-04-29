package lexor.core.ast.expression;

import lexor.core.ast.Node;
import lexor.core.ast.NodeVisitor;
import lexor.core.lexer.TokenType;

/**
 * Represents a binary expression: left <op> right.
 *
 * Covers arithmetic:  +  -  *  /  %
 * Covers comparison:  >  <  >=  <=  =  <>
 * Covers logical:     AND  OR
 *
 * Examples:
 *   x + 1
 *   a >= b
 *   flag AND (x > 0)
 */
public class BinaryOpNode extends Node {

    private final Node left;
    private final TokenType operator;
    private final Node right;

    public BinaryOpNode(int line, Node left, TokenType operator, Node right) {
        super(line);
        this.left     = left;
        this.operator = operator;
        this.right    = right;
    }

    public Node getLeft()          { return left; }
    public TokenType getOperator() { return operator; }
    public Node getRight()         { return right; }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitBinaryOp(this);
    }
}