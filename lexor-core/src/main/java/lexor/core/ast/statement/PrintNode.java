package lexor.core.ast.statement;

import lexor.core.ast.Node;
import lexor.core.ast.NodeVisitor;

import java.util.List;

/**
 * Represents a PRINT statement.
 *
 * Grammar:
 *   PRINT <item> [& <item> ...] [$]
 *
 * Each item in the list is an expression node.
 * The boolean flag indicates whether a trailing $ (newline) was present.
 *
 * Examples:
 *   PRINT "Hello" & name $
 *   PRINT x + y
 */
public class PrintNode extends Node {

    private final List<Node> items;   // one or more expressions joined by &
    private final boolean newline;    // true if trailing $ was present

    public PrintNode(int line, List<Node> items, boolean newline) {
        super(line);
        this.items   = items;
        this.newline = newline;
    }

    public List<Node> getItems() { return items; }
    public boolean hasNewline()  { return newline; }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitPrint(this);
    }
}
