package lexor.core.ast.statement;

import lexor.core.ast.Node;
import lexor.core.ast.NodeVisitor;

/**
 * Represents a variable assignment statement.
 *
 * Grammar:
 *   <name> = <expression>
 *
 * Example:
 *   x = x + 1
 */
public class AssignNode extends Node {

    private final String varName;
    private final Node expression;

    public AssignNode(int line, String varName, Node expression) {
        super(line);
        this.varName    = varName;
        this.expression = expression;
    }

    public String getVarName()  { return varName; }
    public Node getExpression() { return expression; }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitAssign(this);
    }
}