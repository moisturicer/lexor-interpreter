package lexor.core.ast.statement;

import lexor.core.ast.Node;
import lexor.core.ast.NodeVisitor;
import lexor.core.ast.expression.LiteralNode;
import lexor.core.lexer.TokenType;

/**
 * Represents a single variable declaration inside AREA.
 *
 * Grammar:
 *   DECLARE <type> <name>
 *   DECLARE <type> <name> = <literal>
 *
 * Examples:
 *   DECLARE INT x
 *   DECLARE FLOAT ratio = 1.5
 */
public class DeclareNode extends Node {

    private final TokenType dataType;   // INT | FLOAT | CHAR | BOOL
    private final String varName;
    private final LiteralNode initializer; // null if no initializer

    public DeclareNode(int line, TokenType dataType,
                       String varName, LiteralNode initializer) {
        super(line);
        this.dataType    = dataType;
        this.varName     = varName;
        this.initializer = initializer;
    }

    public TokenType getDataType()      { return dataType; }
    public String getVarName()          { return varName; }
    public LiteralNode getInitializer() { return initializer; }
    public boolean hasInitializer()     { return initializer != null; }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitDeclare(this);
    }
}