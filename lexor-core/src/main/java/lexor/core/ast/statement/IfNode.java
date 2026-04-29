package lexor.core.ast.statement;

import lexor.core.ast.Node;
import lexor.core.ast.NodeVisitor;

import java.util.List;

/**
 * Represents an IF / ELSE IF / ELSE chain.
 *
 * Grammar:
 *   IF (<condition>):
 *     <body>
 *   ELSE IF (<condition>):
 *     <body>
 *   ELSE:
 *     <body>
 *   END
 *
 * elseIfBranches may be empty; elseBranch may be null.
 */
public class IfNode extends Node {

    /**
     * A single condition + body pair (used for both IF and ELSE IF branches).
     */
    public static class Branch {
        private final Node condition;
        private final List<Node> body;

        public Branch(Node condition, List<Node> body) {
            this.condition = condition;
            this.body      = body;
        }

        public Node getCondition()  { return condition; }
        public List<Node> getBody() { return body; }
    }

    private final Branch ifBranch;
    private final List<Branch> elseIfBranches;
    private final List<Node> elseBranch; // null if no ELSE

    public IfNode(int line,
                  Branch ifBranch,
                  List<Branch> elseIfBranches,
                  List<Node> elseBranch) {
        super(line);
        this.ifBranch       = ifBranch;
        this.elseIfBranches = elseIfBranches;
        this.elseBranch     = elseBranch;
    }

    public Branch getIfBranch()              { return ifBranch; }
    public List<Branch> getElseIfBranches()  { return elseIfBranches; }
    public List<Node> getElseBranch()        { return elseBranch; }
    public boolean hasElse()                 { return elseBranch != null; }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitIf(this);
    }
}