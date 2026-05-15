package lexor.core.parser;

import lexor.core.ast.*;
import lexor.core.ast.expression.*;
import lexor.core.ast.statement.*;
import lexor.core.error.ParseException;
import lexor.core.lexer.Token;
import lexor.core.lexer.TokenType;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    private final List<Token> tokens;
    private int pos;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.pos = 0;
    }

    // entry point
    public ProgramNode parse() {
        int line = peek().getLine();

        expect(TokenType.SCRIPT);
        expect(TokenType.AREA);

        List<DeclareNode> declarations = new ArrayList<>();
        expect(TokenType.START);
        expect(TokenType.SCRIPT);

        // declarations come right after START SCRIPT
        while (check(TokenType.DECLARE)) {
            declarations.addAll(parseDeclare());
        }

        // executable statements
        List<Node> statements = new ArrayList<>();
        while (!check(TokenType.END)) {
            statements.add(parseStatement());
        }

        expect(TokenType.END);
        expect(TokenType.SCRIPT);

        // ── reject anything after END SCRIPT ──────────────────
        if (!check(TokenType.EOF)) {
            throw new ParseException(peek().getLine(),
                    "unexpected token after END SCRIPT: " + peek().getValue());
        }
        // ──────────────────────────────────────────────────────

        return new ProgramNode(line, "LEXOR", declarations, statements);
    }

    // DECLARE INT x, y=5, z
    private List<DeclareNode> parseDeclare() {
        int line = peek().getLine();
        expect(TokenType.DECLARE);

        TokenType dataType = parseDataType();
        List<DeclareNode> nodes = new ArrayList<>();

        do {
            Token name = expect(TokenType.IDENTIFIER);
            LiteralNode init = null;

            if (match(TokenType.ASSIGN)) {
                init = parseLiteralOnly(line);
            }

            nodes.add(new DeclareNode(line, dataType, name.getValue(), init));
        } while (match(TokenType.COMMA));

        return nodes;
    }

    private TokenType parseDataType() {
        if (match(TokenType.INT))   return TokenType.INT;
        if (match(TokenType.FLOAT)) return TokenType.FLOAT;
        if (match(TokenType.CHAR))  return TokenType.CHAR;
        if (match(TokenType.BOOL))  return TokenType.BOOL;
        throw new ParseException(peek().getLine(), "expected data type, got: " + peek().getValue());
    }

    // only literals allowed in declarations — supports -5, +5, -1.5, +3.14
    private LiteralNode parseLiteralOnly(int line) {
        // consume optional leading sign
        boolean negative = false;
        if (match(TokenType.MINUS)) {
            negative = true;
        } else {
            match(TokenType.PLUS); // consume + and ignore it
        }

        Token t = peek();

        if (match(TokenType.INT_LITERAL)) {
            String val = negative ? "-" + t.getValue() : t.getValue();
            return new LiteralNode(t.getLine(), TokenType.INT_LITERAL, val);
        }
        if (match(TokenType.FLOAT_LITERAL)) {
            String val = negative ? "-" + t.getValue() : t.getValue();
            return new LiteralNode(t.getLine(), TokenType.FLOAT_LITERAL, val);
        }

        // sign is not valid for non-numeric literals
        if (negative) {
            throw new ParseException(line,
                    "cannot apply '-' to non-numeric literal: " + t.getValue());
        }

        if (match(TokenType.CHAR_LITERAL))   return new LiteralNode(t.getLine(), TokenType.CHAR_LITERAL, t.getValue());
        if (match(TokenType.STRING_LITERAL)) return new LiteralNode(t.getLine(), TokenType.STRING_LITERAL, t.getValue());
        if (match(TokenType.TRUE))           return new LiteralNode(t.getLine(), TokenType.TRUE, t.getValue());
        if (match(TokenType.FALSE))          return new LiteralNode(t.getLine(), TokenType.FALSE, t.getValue());

        throw new ParseException(line, "expected literal value, got: " + t.getValue());
    }

    private Node parseStatement() {
        Token t = peek();

        if (check(TokenType.IDENTIFIER) && checkNext(TokenType.ASSIGN)) return parseAssign();
        if (check(TokenType.PRINT))   return parsePrint();
        if (check(TokenType.SCAN))    return parseScan();
        if (check(TokenType.IF))      return parseIf();
        if (check(TokenType.FOR))     return parseFor();
        if (check(TokenType.REPEAT))  return parseRepeat();

        throw new ParseException(t.getLine(), "unexpected token: " + t.getValue());
    }

    // x = expr  or  x = y = expr  (chained assign)
    private Node parseAssign() {
        int line = peek().getLine();
        Token name = expect(TokenType.IDENTIFIER);
        expect(TokenType.ASSIGN);

        // check for chained assignment: x = y = expr
        if (check(TokenType.IDENTIFIER) && checkNext(TokenType.ASSIGN)) {
            Node right = parseAssign();
            return new AssignNode(line, name.getValue(), right);
        }

        Node expr = parseExpression();
        return new AssignNode(line, name.getValue(), expr);
    }

    // PRINT: expr & expr & $ & expr
    private PrintNode parsePrint() {
        int line = peek().getLine();
        expect(TokenType.PRINT);
        expect(TokenType.COLON);

        List<Node> items = new ArrayList<>();
        boolean newline = false;

        // first item
        if (check(TokenType.DOLLAR)) {
            advance();
            newline = true;
        } else {
            items.add(parseExpression());
        }

        while (match(TokenType.AMPERSAND)) {
            if (check(TokenType.DOLLAR)) {
                advance();
                newline = true;
            } else {
                items.add(parseExpression());
            }
        }

        return new PrintNode(line, items, newline);
    }

    // SCAN: x, y
    private ScanNode parseScan() {
        int line = peek().getLine();
        expect(TokenType.SCAN);
        expect(TokenType.COLON);

        List<String> vars = new ArrayList<>();
        vars.add(expect(TokenType.IDENTIFIER).getValue());

        while (match(TokenType.COMMA)) {
            vars.add(expect(TokenType.IDENTIFIER).getValue());
        }

        return new ScanNode(line, vars);
    }

    // IF (cond) START IF ... END IF  ELSE IF ...  ELSE START IF ... END IF
    private IfNode parseIf() {
        int line = peek().getLine();
        expect(TokenType.IF);

        expect(TokenType.LPAREN);
        Node condition = parseExpression();
        expect(TokenType.RPAREN);

        expect(TokenType.START);
        expect(TokenType.IF);
        List<Node> ifBody = parseBlock(TokenType.END);
        expect(TokenType.END);
        expect(TokenType.IF);

        List<IfNode.Branch> elseIfs = new ArrayList<>();
        List<Node> elseBody = null;

        while (check(TokenType.ELSE)) {
            advance(); // consume ELSE
            if (check(TokenType.IF)) {
                advance(); // consume IF
                expect(TokenType.LPAREN);
                Node elseIfCond = parseExpression();
                expect(TokenType.RPAREN);
                expect(TokenType.START);
                expect(TokenType.IF);
                List<Node> elseIfBody = parseBlock(TokenType.END);
                expect(TokenType.END);
                expect(TokenType.IF);
                elseIfs.add(new IfNode.Branch(elseIfCond, elseIfBody));
            } else {
                expect(TokenType.START);
                expect(TokenType.IF);
                elseBody = parseBlock(TokenType.END);
                expect(TokenType.END);
                expect(TokenType.IF);
                break;
            }
        }

        return new IfNode(line, new IfNode.Branch(condition, ifBody), elseIfs, elseBody);
    }

    // FOR (init; cond; update) START FOR ... END FOR
    private ForNode parseFor() {
        int line = peek().getLine();
        expect(TokenType.FOR);
        expect(TokenType.LPAREN);

        // init: varName = startExpr
        Token varName = expect(TokenType.IDENTIFIER);
        expect(TokenType.ASSIGN);
        Node startExpr = parseExpression();

        expect(TokenType.COMMA);
        Node condExpr = parseExpression();

        expect(TokenType.COMMA);
        Node updateExpr = parseExpression();

        expect(TokenType.RPAREN);
        expect(TokenType.START);
        expect(TokenType.FOR);

        List<Node> body = parseBlock(TokenType.END);

        expect(TokenType.END);
        expect(TokenType.FOR);

        return new ForNode(line, varName.getValue(), startExpr, condExpr, updateExpr, body);
    }

    // REPEAT WHEN (cond) START REPEAT ... END REPEAT
    private RepeatNode parseRepeat() {
        int line = peek().getLine();
        expect(TokenType.REPEAT);
        expect(TokenType.WHEN);
        expect(TokenType.LPAREN);
        Node condition = parseExpression();
        expect(TokenType.RPAREN);

        expect(TokenType.START);
        expect(TokenType.REPEAT);

        List<Node> body = parseBlock(TokenType.END);

        expect(TokenType.END);
        expect(TokenType.REPEAT);

        return new RepeatNode(line, condition, body);
    }

    // parse statements until we hit a stop token
    private List<Node> parseBlock(TokenType stopToken) {
        List<Node> stmts = new ArrayList<>();
        while (!check(stopToken) && !check(TokenType.ELSE) && !check(TokenType.EOF)) {
            stmts.add(parseStatement());
        }
        return stmts;
    }

    // expression parsing — precedence climbing
    // OR → AND → NOT → comparison → addition → multiplication → unary → primary

    private Node parseExpression() {
        return parseOr();
    }

    private Node parseOr() {
        Node left = parseAnd();
        while (check(TokenType.OR)) {
            int line = peek().getLine();
            advance();
            left = new BinaryOpNode(line, left, TokenType.OR, parseAnd());
        }
        return left;
    }

    private Node parseAnd() {
        Node left = parseNot();
        while (check(TokenType.AND)) {
            int line = peek().getLine();
            advance();
            left = new BinaryOpNode(line, left, TokenType.AND, parseNot());
        }
        return left;
    }

    private Node parseNot() {
        if (check(TokenType.NOT)) {
            int line = peek().getLine();
            advance();
            return new UnaryOpNode(line, TokenType.NOT, parseNot());
        }
        return parseComparison();
    }

    private Node parseComparison() {
        Node left = parseAddition();
        while (checkAny(TokenType.GREATER, TokenType.LESS,
                TokenType.GREATER_EQUAL, TokenType.LESS_EQUAL,
                TokenType.EQUAL, TokenType.NOT_EQUAL)) {
            int line = peek().getLine();
            TokenType op = advance().getType();
            left = new BinaryOpNode(line, left, op, parseAddition());
        }
        return left;
    }

    private Node parseAddition() {
        Node left = parseMultiplication();
        while (checkAny(TokenType.PLUS, TokenType.MINUS)) {
            int line = peek().getLine();
            TokenType op = advance().getType();
            left = new BinaryOpNode(line, left, op, parseMultiplication());
        }
        return left;
    }

    private Node parseMultiplication() {
        Node left = parseUnary();
        while (checkAny(TokenType.MULTIPLY, TokenType.DIVIDE, TokenType.MODULO)) {
            int line = peek().getLine();
            TokenType op = advance().getType();
            left = new BinaryOpNode(line, left, op, parseUnary());
        }
        return left;
    }

    // handles unary -x, +x in expressions (already supported — unchanged)
    private Node parseUnary() {
        if (check(TokenType.MINUS)) {
            int line = peek().getLine();
            advance();
            return new UnaryOpNode(line, TokenType.MINUS, parseUnary());
        }
        if (check(TokenType.PLUS)) {
            advance();
            return parseUnary();
        }
        return parsePrimary();
    }

    private Node parsePrimary() {
        Token t = peek();

        if (match(TokenType.INT_LITERAL))    return new LiteralNode(t.getLine(), TokenType.INT_LITERAL, t.getValue());
        if (match(TokenType.FLOAT_LITERAL))  return new LiteralNode(t.getLine(), TokenType.FLOAT_LITERAL, t.getValue());
        if (match(TokenType.CHAR_LITERAL))   return new LiteralNode(t.getLine(), TokenType.CHAR_LITERAL, t.getValue());
        if (match(TokenType.STRING_LITERAL)) return new LiteralNode(t.getLine(), TokenType.STRING_LITERAL, t.getValue());
        if (match(TokenType.TRUE))           return new LiteralNode(t.getLine(), TokenType.TRUE, "TRUE");
        if (match(TokenType.FALSE))          return new LiteralNode(t.getLine(), TokenType.FALSE, "FALSE");

        if (match(TokenType.IDENTIFIER))     return new VariableNode(t.getLine(), t.getValue());

        if (match(TokenType.LPAREN)) {
            Node expr = parseExpression();
            expect(TokenType.RPAREN);
            return expr;
        }

        throw new ParseException(t.getLine(), "unexpected token in expression: " + t.getValue());
    }

    // helpers

    private Token peek() {
        return tokens.get(pos);
    }

    private Token advance() {
        Token t = tokens.get(pos);
        if (t.getType() != TokenType.EOF) pos++;
        return t;
    }

    private boolean check(TokenType type) {
        return peek().getType() == type;
    }

    private boolean checkNext(TokenType type) {
        if (pos + 1 >= tokens.size()) return false;
        return tokens.get(pos + 1).getType() == type;
    }

    private boolean checkAny(TokenType... types) {
        for (TokenType t : types) {
            if (check(t)) return true;
        }
        return false;
    }

    private boolean match(TokenType type) {
        if (check(type)) {
            advance();
            return true;
        }
        return false;
    }

    private Token expect(TokenType type) {
        if (!check(type)) {
            throw new ParseException(peek().getLine(),
                    "expected " + type + " but got " + peek().getValue());
        }
        return advance();
    }
}