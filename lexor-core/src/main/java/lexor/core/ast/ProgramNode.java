package lexor.core.ast;

import lexor.core.ast.statement.DeclareNode;
import java.util.List;

/**
 * Root node of the AST.
 * Represents the full SCRIPT … AREA … START … END structure.
 *
 * Structure:
 *   SCRIPT <name>
 *   AREA
 *     [declarations]
 *   START
 *     [statements]
 *   END
 */
public class ProgramNode extends Node {

    private final String scriptName;
    private final List<DeclareNode> declarations;
    private final List<Node> statements;

    public ProgramNode(int line, String scriptName,
                       List<DeclareNode> declarations,
                       List<Node> statements) {
        super(line);
        this.scriptName   = scriptName;
        this.declarations = declarations;
        this.statements   = statements;
    }

    public String getScriptName()         { return scriptName; }
    public List<DeclareNode> getDeclarations() { return declarations; }
    public List<Node> getStatements()     { return statements; }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitProgram(this);
    }
}