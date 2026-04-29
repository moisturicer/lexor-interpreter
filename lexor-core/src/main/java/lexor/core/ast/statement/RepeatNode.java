package lexor.core.ast.statement;

import lexor.core.ast.Node;
import lexor.core.ast.NodeVisitor;

import java.util.List;

/**
 * Represents a REPEAT WHEN (while) loop.
 *
 * Grammar:
 *   REPEAT WHEN (<condition>):
 *     <body>
 *   END
 *
 * Example:
 *   REPEAT WHEN (x > 0):
 *     x = x - 1
 *   END
 */
public class RepeatNode extends Node {

    private final Node condition;
    private final List<Node> body;

    public RepeatNode(int line, Node condition, List<Node> body) {
        super(line);
        this.condition = condition;
        this.body      = body;
    }

    public Node getCondition()  { return condition; }
    public List<Node> getBody() { return body; }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitRepeat(this);
    }
}
