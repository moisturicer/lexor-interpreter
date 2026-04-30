package lexor.core.interpreter;

import lexor.core.ast.*;
import lexor.core.ast.expression.*;
import lexor.core.ast.statement.*;
import lexor.core.lexer.TokenType;

import java.util.List;
import java.util.Scanner;

public class Interpreter implements NodeVisitor<LexorValue> {

    private Environment env;
    private final StringBuilder output;
    private final Scanner inputScanner;

    public Interpreter() {
        this.env          = new Environment();
        this.output       = new StringBuilder();
        this.inputScanner = new Scanner(System.in);
    }

    public Interpreter(String input) {
        this.env          = new Environment();
        this.output       = new StringBuilder();
        this.inputScanner = new Scanner(input.replace(",", "\n"));
    }

    public String run(ProgramNode program) {
        output.setLength(0);
        program.accept(this);
        return output.toString();
    }

    @Override
    public LexorValue visitProgram(ProgramNode node) {
        for (DeclareNode decl : node.getDeclarations()) {
            decl.accept(this);
        }
        for (Node stmt : node.getStatements()) {
            stmt.accept(this);
        }
        return null;
    }

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
        TokenType declaredType = env.getType(node.getVarName());
        value = TypeChecker.checkAssign(node.getVarName(), declaredType, value, node.getLine());
        env.set(node.getVarName(), value);
        return value;
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
                    case INT:
                        value = LexorValue.ofInt(Integer.parseInt(raw));
                        break;
                    case FLOAT:
                        value = LexorValue.ofFloat(Double.parseDouble(raw));
                        break;
                    case CHAR:
                        if (raw.length() != 1)
                            throw new RuntimeException("CHAR input must be a single character");
                        value = LexorValue.ofChar(raw.charAt(0));
                        break;
                    case BOOL:
                        if (raw.equalsIgnoreCase("TRUE"))
                            value = LexorValue.ofBool(true);
                        else if (raw.equalsIgnoreCase("FALSE"))
                            value = LexorValue.ofBool(false);
                        else
                            throw new RuntimeException("BOOL input must be TRUE or FALSE");
                        break;
                    default:
                        throw new RuntimeException("unknown type for SCAN");
                }
            } catch (NumberFormatException e) {
                throw new RuntimeException("[Line " + node.getLine()
                        + "] invalid input '" + raw
                        + "' for variable '" + varName + "' of type " + declaredType);
            }

            env.set(varName, value);
        }
        return null;
    }

    @Override
    public LexorValue visitIf(IfNode node) {
        LexorValue cond = node.getIfBranch().getCondition().accept(this);
        TypeChecker.checkBool(cond, "IF", node.getLine());

        if (cond.asBool()) {
            executeBlock(node.getIfBranch().getBody());
            return null;
        }

        for (IfNode.Branch branch : node.getElseIfBranches()) {
            LexorValue elseIfCond = branch.getCondition().accept(this);
            TypeChecker.checkBool(elseIfCond, "ELSE IF", node.getLine());
            if (elseIfCond.asBool()) {
                executeBlock(branch.getBody());
                return null;
            }
        }

        if (node.hasElse()) {
            executeBlock(node.getElseBranch());
        }

        return null;
    }

    @Override
    public LexorValue visitFor(ForNode node) {
        // set loop variable to start value
        LexorValue startVal = node.getStartExpr().accept(this);
        TokenType declaredType = env.getType(node.getVarName());
        startVal = TypeChecker.checkAssign(node.getVarName(), declaredType, startVal, node.getLine());
        env.set(node.getVarName(), startVal);

        while (true) {
            // condition
            LexorValue cond = node.getEndExpr().accept(this);
            TypeChecker.checkBool(cond, "FOR condition", node.getLine());
            if (!cond.asBool()) break;

            // body
            executeBlock(node.getBody());

            // update — step expression is an assignment node
            if (node.hasStep()) {
                node.getStepExpr().accept(this);
            }
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

    @Override
    public LexorValue visitBinaryOp(BinaryOpNode node) {
        TokenType op   = node.getOperator();
        int       line = node.getLine();

        // short-circuit logical operators
        if (op == TokenType.AND) {
            LexorValue left = node.getLeft().accept(this);
            TypeChecker.checkBool(left, "AND", line);
            if (!left.asBool()) return LexorValue.ofBool(false);
            LexorValue right = node.getRight().accept(this);
            TypeChecker.checkBool(right, "AND", line);
            return LexorValue.ofBool(right.asBool());
        }

        if (op == TokenType.OR) {
            LexorValue left = node.getLeft().accept(this);
            TypeChecker.checkBool(left, "OR", line);
            if (left.asBool()) return LexorValue.ofBool(true);
            LexorValue right = node.getRight().accept(this);
            TypeChecker.checkBool(right, "OR", line);
            return LexorValue.ofBool(right.asBool());
        }

        LexorValue left  = node.getLeft().accept(this);
        LexorValue right = node.getRight().accept(this);

        switch (op) {
            case PLUS:
            case MINUS:
            case MULTIPLY:
            case DIVIDE:
            case MODULO:
                return arithmetic(left, right, op, line);

            case GREATER:
            case LESS:
            case GREATER_EQUAL:
            case LESS_EQUAL:
            case EQUAL:
            case NOT_EQUAL:
                return compare(left, right, op, line);

            default:
                throw new RuntimeException("[Line " + line + "] unknown operator: " + op);
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
                        + "] unary minus requires numeric operand");
            default:
                throw new RuntimeException("[Line " + line
                        + "] unknown unary operator: " + node.getOperator());
        }
    }

    @Override
    public LexorValue visitLiteral(LiteralNode node) {
        switch (node.getType()) {
            case INT_LITERAL:    return LexorValue.ofInt(Integer.parseInt(node.getValue()));
            case FLOAT_LITERAL:  return LexorValue.ofFloat(Double.parseDouble(node.getValue()));
            case CHAR_LITERAL:   return LexorValue.ofChar(node.getValue().charAt(0));
            case STRING_LITERAL: return LexorValue.ofString(node.getValue());
            case TRUE:           return LexorValue.ofBool(true);
            case FALSE:          return LexorValue.ofBool(false);
            default:
                throw new RuntimeException("[Line " + node.getLine()
                        + "] unknown literal type: " + node.getType());
        }
    }

    @Override
    public LexorValue visitVariable(VariableNode node) {
        return env.get(node.getName());
    }

    // helpers

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
                    if (r == 0) throw new RuntimeException("[Line " + line + "] division by zero");
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
                    if (r == 0) throw new RuntimeException("[Line " + line + "] division by zero");
                    return LexorValue.ofInt(l / r);
                case MODULO:   return LexorValue.ofInt(l % r);
                default: break;
            }
        }
        throw new RuntimeException("[Line " + line + "] unknown arithmetic op: " + op);
    }

    private LexorValue compare(LexorValue left, LexorValue right,
                               TokenType op, int line) {
        TypeChecker.checkComparable(left, right, op.name(), line);

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

        if (left.getType() == LexorValue.Type.BOOL) {
            boolean l = left.asBool(), r = right.asBool();
            if (op == TokenType.EQUAL)     return LexorValue.ofBool(l == r);
            if (op == TokenType.NOT_EQUAL) return LexorValue.ofBool(l != r);
        }

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

        throw new RuntimeException("[Line " + line + "] cannot compare: " + op);
    }
}