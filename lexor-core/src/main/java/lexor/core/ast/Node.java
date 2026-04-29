package lexor.core.ast;

/**
 * Abstract base class for all AST nodes.
 * Every node records the source line it came from for error reporting.
 */
public abstract class Node {
    private final int line;

    protected Node(int line) {
        this.line = line;
    }

    public int getLine() {
        return line;
    }

    /**
     * Accept a visitor. Implemented by every concrete node.
     */
    public abstract <T> T accept(NodeVisitor<T> visitor);
}
