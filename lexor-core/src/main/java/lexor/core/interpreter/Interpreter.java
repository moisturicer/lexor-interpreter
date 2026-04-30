package lexor.core.interpreter;

import lexor.core.ast.*;
import lexor.core.ast.expression.*;
import lexor.core.ast.statement.*;
import lexor.core.lexer.TokenType;

import java.util.List;
import java.util.Scanner;

/**
 * Tree-walking interpreter.
 * Implements NodeVisitor<LexorValue> — each visit() method evaluates its node
 * and returns the resulting LexorValue (or null for statement nodes).
 */
public class Interpreter implements NodeVisitor<LexorValue> {

    private Environment env;
    private final StringBuilder output;
    private final Scanner inputScanner;

    public Interpreter() {
        this.env          = new Environment();
        this.output       = new StringBuilder();
        this.inputScanner = new Scanner(System.in);
    }

    // ── entry point ───────────────────────────────────────────

    /**
     * Run the program. Called by LexorRunner after parsing.
     * Returns all printed output as a single String.
     */
    public String run(ProgramNode program) {
        output.setLength(0);
        program.accept(this);
        return output.toString();
    }

    // ── program ───────────────────────────────────────────────

    @Override
    public LexorValue visitProgram(ProgramNode node) {
        // process declarations first
        for (DeclareNode decl : node.getDeclarations()) {
            decl.accept(this);
        }
        // then execute statements
        for (Node stmt : node.getStatements()) {
            stmt.accept(this);
        }
        return null;
    }

    // ── statements ────────────────────────────────────────────

    @Override
    public LexorValue visitDeclare(DeclareNode node) {
        LexorValue value = node.hasInitializer()
                ? node.getInitializer().accept(this)
                : LexorValue.defaultFor(node.getDataType());

        value = TypeChecker.checkAssign(
                node.getVarName(), node.getDataType(), value, node.getLine());

        env.declare(node.getVarName(), node.getDataType(), value);
        return null;
    }

    @Override
    public LexorValue visitAssign(AssignNode node) {
        LexorValue value = node.getExpression().accept(this);

        // handle chained assign: the inner AssignNode already stored its var;
        // we only need to store the outermost name with the final value.
        // Unwrap to the raw value (AssignNode returns null — use the expr result).
        TokenType declaredType = env.getType(node.getVarName());
        value = TypeChecker.checkAssign(node.getVarName(), declaredType, value, node.getLine());
        env.set(node.getVarName(), value);
        return value; // return value so chained assign works
    }

    @Override
    public LexorValue visitPrint(PrintNode node) {
        StringBuilder sb = new StringBuilder();

        for (Node item : node.getItems()) {
            LexorValue val = item.accept(this);
            sb.append(val.toString());
        }

        if (node.hasNewline()) {
            sb.append("\n");
        }

        output.append(sb);
        return null;
    }

    @Override
    public LexorValue visitScan(ScanNode node) {
        for (String varName : node.getVarNames()) {
            String raw = inputScanner.next();
            TokenType declaredType = env.getType(varName);
            LexorValue value;

            try {
                switch (declaredType) {
                    case INT:   value = LexorValue.ofInt(Integer.parseInt(raw));     break;
                    case FLOAT: value = LexorValue.ofFloat(Double.parseDouble(raw)); break;
                    case CHAR:
                        if (raw.length() != 1)
                            throw new RuntimeException("CHAR input must be a single character");
                        value = LexorValue.ofChar(raw.charAt(0));
                        break;
                    case BOOL:
                        if (raw.equalsIgnoreCase("TRUE"))       value = LexorValue.ofBool(true);
                        else if (raw.equalsIgnoreCase("FALSE"))  value = LexorValue.ofBool(false);
                        else throw new RuntimeException("BOOL input must be TRUE or FALSE");
                        break;
                    default:
                        throw new RuntimeException("Unknown type for SCAN");
                }
            } catch (NumberFormatException e) {
                throw new RuntimeException("[Line " + node.getLine()
                        + "] Runtime error: invalid input '" + raw
                        + "' for variable '" + varName + "' of type " + declaredType);
            }

            env.set(varName, value);
        }
        return null;
    }

    @Override
    public LexorValue visitIf(IfNode node) {
        // evaluate the main IF branch
        LexorValue cond = node.getIfBranch().getCondition().accept(this);
        TypeChecker.checkBool(cond, "IF", node.getLine());

        if (cond.asBool()) {
            executeBlock(node.getIfBranch().getBody());
            return null;
        }

        // ELSE IF branches
        for (IfNode.Branch branch : node.getElseIfBranches()) {
            LexorValue elseIfCond = branch.getCondition().accept(this);
            TypeChecker.checkBool(elseIfCond, "ELSE IF", node.getLine());
            if (elseIfCond.asBool()) {
                executeBlock(branch.getBody());
                return null;
            }
        }

        // ELSE branch
        if (node.hasElse()) {
            executeBlock(node.getElseBranch());
        }

        return null;
    }

    @Override
    public LexorValue visitFor(ForNode node) {
        // init — assign start value to loop variable
        LexorValue startVal = node.getStartExpr().accept(this);
        TokenType declaredType = env.getType(node.getVarName());
        startVal = TypeChecker.checkAssign(node.getVarName(), declaredType, startVal, node.getLine());
        env.set(node.getVarName(), startVal);

        while (true) {
            // condition
            LexorValue cond = node.getEndExpr().accept(this);
            TypeChecker.checkBool(cond, "FOR condition", node.getLine());
            if (!cond.asBool()) break;

            // body in child scope
            executeBlock(node.getBody());

            // update
            node.getStepExpr().accept(this);
        }
        return null;
    }

    @Override
    public LexorValue visitRepeat(RepeatNode node) {
        while (true) {
            LexorValue cond = node.getCondition().accept(this);
            TypeChecker.checkBool(cond, "REPEAT WHEN", node.getLine());
            if (!cond.asBool()) break;
            executeBlock(node.getBody());
        }
        return null;
    }

    // ── expressions ───────────────────────────────────────────

    @Override
    public LexorValue visitBinaryOp(BinaryOpNode node) {
        LexorValue left  = node.getLeft().accept(this);
        LexorValue right = node.getRight().accept(this);
        TokenType  op    = node.getOperator();
        int        line  = node.getLine();

        switch (op) {
            // arithmetic
            case PLUS:     return arithmetic(left, right, op, line);
            case MINUS:    return arithmetic(left, right, op, line);
            case MULTIPLY: return arithmetic(left, right, op, line);
            case DIVIDE:   return arithmetic(left, right, op, line);
            case MODULO:   return arithmetic(left, right, op, line);

            // comparison
            case GREATER:       return compare(left, right, op, line);
            case LESS:          return compare(left, right, op, line);
            case GREATER_EQUAL: return compare(left, right, op, line);
            case LESS_EQUAL:    return compare(left, right, op, line);
            case EQUAL:         return compare(left, right, op, line);
            case NOT_EQUAL:     return compare(left, right, op, line);

            // logical
            case AND:
                TypeChecker.checkBool(left,  "AND", line);
                TypeChecker.checkBool(right, "AND", line);
                return LexorValue.ofBool(left.asBool() && right.asBool());
            case OR:
                TypeChecker.checkBool(left,  "OR", line);
                TypeChecker.checkBool(right, "OR", line);
                return LexorValue.ofBool(left.asBool() || right.asBool());

            default:
                throw new RuntimeException("[Line " + line + "] Unknown operator: " + op);
        }
    }

    @Override
    public LexorValue visitUnaryOp(UnaryOpNode node) {
        LexorValue operand = node.getOperand().accept(this);
        int line = node.getLine();

        switch (node.getOperator()) {
            case NOT:
                TypeChecker.checkBool(operand, "NOT", line);
                return LexorValue.ofBool(!operand.asBool());
            case MINUS:
                if (operand.getType() == LexorValue.Type.INT)
                    return LexorValue.ofInt(-operand.asInt());
                if (operand.getType() == LexorValue.Type.FLOAT)
                    return LexorValue.ofFloat(-operand.asFloat());
                throw new RuntimeException("[Line " + line
                        + "] Type error: unary minus requires numeric operand");
            default:
                throw new RuntimeException("[Line " + line
                        + "] Unknown unary operator: " + node.getOperator());
        }
    }

    @Override
    public LexorValue visitLiteral(LiteralNode node) {
        switch (node.getType()) {
            case INT_LITERAL:    return LexorValue.ofInt(Integer.parseInt(node.getValue()));
            case FLOAT_LITERAL:  return LexorValue.ofFloat(Double.parseDouble(node.getValue()));
            case CHAR_LITERAL:   return LexorValue.ofChar(node.getValue().charAt(0));
            case STRING_LITERAL: return LexorValue.ofChar('\0'); // strings used only in PRINT
            case TRUE:           return LexorValue.ofBool(true);
            case FALSE:          return LexorValue.ofBool(false);
            default:
                throw new RuntimeException("[Line " + node.getLine()
                        + "] Unknown literal type: " + node.getType());
        }
    }

    @Override
    public LexorValue visitVariable(VariableNode node) {
        return env.get(node.getName());
    }

    // ── helpers ───────────────────────────────────────────────

    private void executeBlock(List<Node> stmts) {
        Environment previous = this.env;
        this.env = new Environment(previous);
        try {
            for (Node stmt : stmts) {
                stmt.accept(this);
            }
        } finally {
            this.env = previous;
        }
    }

    private LexorValue arithmetic(LexorValue left, LexorValue right,
                                  TokenType op, int line) {
        TypeChecker.checkNumeric(left, right, op.name(), line);

        boolean isFloat = left.getType()  == LexorValue.Type.FLOAT
                || right.getType() == LexorValue.Type.FLOAT;

        if (isFloat) {
            double l = left.asFloat(), r = right.asFloat();
            switch (op) {
                case PLUS:     return LexorValue.ofFloat(l + r);
                case MINUS:    return LexorValue.ofFloat(l - r);
                case MULTIPLY: return LexorValue.ofFloat(l * r);
                case DIVIDE:
                    if (r == 0) throw new RuntimeException("[Line " + line + "] Division by zero");
                    return LexorValue.ofFloat(l / r);
                case MODULO:   return LexorValue.ofFloat(l % r);
                default: break;
            }
        } else {
            int l = left.asInt(), r = right.asInt();
            switch (op) {
                case PLUS:     return LexorValue.ofInt(l + r);
                case MINUS:    return LexorValue.ofInt(l - r);
                case MULTIPLY: return LexorValue.ofInt(l * r);
                case DIVIDE:
                    if (r == 0) throw new RuntimeException("[Line " + line + "] Division by zero");
                    return LexorValue.ofInt(l / r);
                case MODULO:   return LexorValue.ofInt(l % r);
                default: break;
            }
        }
        throw new RuntimeException("[Line " + line + "] Unknown arithmetic op: " + op);
    }

    private LexorValue compare(LexorValue left, LexorValue right,
                               TokenType op, int line) {
        TypeChecker.checkComparable(left, right, op.name(), line);

        // numeric comparison
        if (left.isNumeric() && right.isNumeric()) {
            double l = left.asFloat(), r = right.asFloat();
            switch (op) {
                case GREATER:       return LexorValue.ofBool(l > r);
                case LESS:          return LexorValue.ofBool(l < r);
                case GREATER_EQUAL: return LexorValue.ofBool(l >= r);
                case LESS_EQUAL:    return LexorValue.ofBool(l <= r);
                case EQUAL:         return LexorValue.ofBool(l == r);
                case NOT_EQUAL:     return LexorValue.ofBool(l != r);
                default: break;
            }
        }

        // BOOL comparison
        if (left.getType() == LexorValue.Type.BOOL) {
            boolean l = left.asBool(), r = right.asBool();
            if (op == TokenType.EQUAL)     return LexorValue.ofBool(l == r);
            if (op == TokenType.NOT_EQUAL) return LexorValue.ofBool(l != r);
        }

        // CHAR comparison
        if (left.getType() == LexorValue.Type.CHAR) {
            char l = left.asChar(), r = right.asChar();
            switch (op) {
                case GREATER:       return LexorValue.ofBool(l > r);
                case LESS:          return LexorValue.ofBool(l < r);
                case GREATER_EQUAL: return LexorValue.ofBool(l >= r);
                case LESS_EQUAL:    return LexorValue.ofBool(l <= r);
                case EQUAL:         return LexorValue.ofBool(l == r);
                case NOT_EQUAL:     return LexorValue.ofBool(l != r);
                default: break;
            }
        }

        throw new RuntimeException("[Line " + line + "] Cannot compare: " + op);
    }
}