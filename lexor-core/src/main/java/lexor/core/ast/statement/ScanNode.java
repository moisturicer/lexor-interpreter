package lexor.core.ast.statement;

import lexor.core.ast.Node;
import lexor.core.ast.NodeVisitor;

import java.util.List;

/**
 * Represents a SCAN statement that reads input into one or more variables.
 *
 * Grammar:
 *   SCAN <var> [, <var> ...]
 *
 * Example:
 *   SCAN x, y, z
 */
public class ScanNode extends Node {

    private final List<String> varNames; // comma-separated target variables

    public ScanNode(int line, List<String> varNames) {
        super(line);
        this.varNames = varNames;
    }

    public List<String> getVarNames() { return varNames; }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitScan(this);
    }
}
