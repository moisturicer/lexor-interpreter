package lexor.core.ast.expression;

import lexor.core.ast.Node;
import lexor.core.ast.NodeVisitor;

/**
 * Represents a reference to a declared variable by name.
 *
 * Example:
 *   PRINT x + 1   →  x is a VariableNode
 */
public class VariableNode extends Node {

    private final String name;

    public VariableNode(int line, String name) {
        super(line);
        this.name = name;
    }

    public String getName() { return name; }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitVariable(this);
    }
}