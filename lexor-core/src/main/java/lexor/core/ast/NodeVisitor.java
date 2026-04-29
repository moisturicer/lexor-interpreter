package lexor.core.ast;

import lexor.core.ast.expression.*;
import lexor.core.ast.statement.*;

/**
 * Visitor interface for the AST.
 * The Interpreter implements this to evaluate each node type.
 *
 * @param <T> return type of each visit method
 */
public interface NodeVisitor<T> {

    // ── Program ────────────────────────────────────────────────
    T visitProgram(ProgramNode node);

    // ── Statements ─────────────────────────────────────────────
    T visitDeclare(DeclareNode node);
    T visitAssign(AssignNode node);
    T visitPrint(PrintNode node);
    T visitScan(ScanNode node);
    T visitIf(IfNode node);
    T visitFor(ForNode node);
    T visitRepeat(RepeatNode node);

    // ── Expressions ────────────────────────────────────────────
    T visitBinaryOp(BinaryOpNode node);
    T visitUnaryOp(UnaryOpNode node);
    T visitLiteral(LiteralNode node);
    T visitVariable(VariableNode node);
}