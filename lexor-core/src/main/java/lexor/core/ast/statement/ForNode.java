package lexor.core.ast.statement;

import lexor.core.ast.Node;
import lexor.core.ast.NodeVisitor;

import java.util.List;

/**
 * Represents a FOR loop.
 *
 * Grammar (assumed from typical LEXOR spec):
 *   FOR <var> = <start> TO <end> [STEP <step>]:
 *     <body>
 *   END
 *
 * If no STEP is provided, stepExpr will be null (interpreter defaults to 1).
 *
 * Example:
 *   FOR i = 1 TO 10:
 *     PRINT i $
 *   END
 */
public class ForNode extends Node {

    private final String varName;
    private final Node startExpr;
    private final Node endExpr;
    private final Node stepExpr;    // null means default step of 1
    private final List<Node> body;

    public ForNode(int line,
                   String varName,
                   Node startExpr,
                   Node endExpr,
                   Node stepExpr,
                   List<Node> body) {
        super(line);
        this.varName   = varName;
        this.startExpr = startExpr;
        this.endExpr   = endExpr;
        this.stepExpr  = stepExpr;
        this.body      = body;
    }

    public String getVarName()  { return varName; }
    public Node getStartExpr()  { return startExpr; }
    public Node getEndExpr()    { return endExpr; }
    public Node getStepExpr()   { return stepExpr; }   // may be null
    public boolean hasStep()    { return stepExpr != null; }
    public List<Node> getBody() { return body; }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitFor(this);
    }
}