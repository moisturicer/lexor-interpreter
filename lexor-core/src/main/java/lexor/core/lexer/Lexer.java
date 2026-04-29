package lexor.core.lexer;

import lexor.core.error.LexerException;
import java.util.ArrayList;
import java.util.List;

public class    Lexer {
    private final String source;
    private int pos;
    private int line;
    private final List<Token> tokens;

    public Lexer(String source) {
        this.source = source;
        this.pos = 0;
        this.line = 1;
        this.tokens = new ArrayList<>();
    }

    public List<Token> tokenize() {
        while (pos < source.length()) {
            skipWhitespaceAndComments();
            if (pos >= source.length()) break;

            char c = peek();

            if (Character.isLetter(c) || c == '_') {
                scanIdentifierOrKeyword();
            } else if (Character.isDigit(c)) {
                scanNumber();
            } else if (c == '\'') {
                scanChar();
            } else if (c == '"') {
                scanBoolOrString();
            } else if (c == '[') {
                scanEscapeCode();
            } else {
                scanSymbol();
            }
        }
        tokens.add(new Token(TokenType.EOF, "EOF", line));
        return tokens;
    }

    private char peek() {
        return source.charAt(pos);
    }

    private char peekNext() {
        if (pos + 1 < source.length()) return source.charAt(pos + 1);
        return '\0';
    }

    private char advance() {
        char c = source.charAt(pos);
        pos++;
        if (c == '\n') line++;
        return c;
    }

    private void skipWhitespaceAndComments() {
        while (pos < source.length()) {
            char c = peek();
            if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
                advance();
            } else if (c == '%' && peekNext() == '%') {
                // comment — skip until end of line
                while (pos < source.length() && peek() != '\n') {
                    advance();
                }
            } else {
                break;
            }
        }
    }

    private void scanIdentifierOrKeyword() {
        int startLine = line;
        StringBuilder sb = new StringBuilder();
        while (pos < source.length() &&
                (Character.isLetterOrDigit(peek()) || peek() == '_')) {
            sb.append(advance());
        }
        String word = sb.toString();
        tokens.add(new Token(resolveKeyword(word), word, startLine));
    }

    private TokenType resolveKeyword(String word) {
        switch (word) {
            case "SCRIPT":  return TokenType.SCRIPT;
            case "AREA":    return TokenType.AREA;
            case "START":   return TokenType.START;
            case "END":     return TokenType.END;
            case "DECLARE": return TokenType.DECLARE;
            case "PRINT":   return TokenType.PRINT;
            case "SCAN":    return TokenType.SCAN;
            case "IF":      return TokenType.IF;
            case "ELSE":    return TokenType.ELSE;
            case "FOR":     return TokenType.FOR;
            case "REPEAT":  return TokenType.REPEAT;
            case "WHEN":    return TokenType.WHEN;
            case "AND":     return TokenType.AND;
            case "OR":      return TokenType.OR;
            case "NOT":     return TokenType.NOT;
            case "TRUE":    return TokenType.TRUE;
            case "FALSE":   return TokenType.FALSE;
            case "INT":     return TokenType.INT;
            case "FLOAT":   return TokenType.FLOAT;
            case "CHAR":    return TokenType.CHAR;
            case "BOOL":    return TokenType.BOOL;
            default:        return TokenType.IDENTIFIER;
        }
    }

    private void scanNumber() {
        int startLine = line;
        StringBuilder sb = new StringBuilder();
        while (pos < source.length() && Character.isDigit(peek())) {
            sb.append(advance());
        }
        if (pos < source.length() && peek() == '.' &&
                pos + 1 < source.length() && Character.isDigit(peekNext())) {
            sb.append(advance()); // consume '.'
            while (pos < source.length() && Character.isDigit(peek())) {
                sb.append(advance());
            }
            tokens.add(new Token(TokenType.FLOAT_LITERAL, sb.toString(), startLine));
        } else {
            tokens.add(new Token(TokenType.INT_LITERAL, sb.toString(), startLine));
        }
    }

    private void scanChar() {
        int startLine = line;
        advance(); // consume opening '
        if (pos >= source.length()) {
            throw new LexerException(startLine, "Unclosed char literal");
        }
        char value = advance();
        if (pos >= source.length() || peek() != '\'') {
            throw new LexerException(startLine, "Char literal must be a single character");
        }
        advance(); // consume closing '
        tokens.add(new Token(TokenType.CHAR_LITERAL, String.valueOf(value), startLine));
    }

    private void scanBoolOrString() {
        int startLine = line;
        advance(); // consume opening "
        StringBuilder sb = new StringBuilder();
        while (pos < source.length() && peek() != '"') {
            sb.append(advance());
        }
        if (pos >= source.length()) {
            throw new LexerException(startLine, "Unclosed string literal");
        }
        advance(); // consume closing "
        String value = sb.toString();
        if (value.equals("TRUE")) {
            tokens.add(new Token(TokenType.TRUE, value, startLine));
        } else if (value.equals("FALSE")) {
            tokens.add(new Token(TokenType.FALSE, value, startLine));
        } else {
            tokens.add(new Token(TokenType.STRING_LITERAL, value, startLine));
        }
    }

    private void scanEscapeCode() {
        int startLine = line;
        advance(); // consume '['
        StringBuilder sb = new StringBuilder();
        while (pos < source.length() && peek() != ']') {
            sb.append(advance());
        }
        if (pos >= source.length()) {
            throw new LexerException(startLine, "Unclosed escape code");
        }
        advance(); // consume ']'
        tokens.add(new Token(TokenType.STRING_LITERAL, sb.toString(), startLine));
    }

    private void scanSymbol() {
        int startLine = line;
        char c = advance();
        switch (c) {
            case '+': tokens.add(new Token(TokenType.PLUS, "+", startLine)); break;
            case '-': tokens.add(new Token(TokenType.MINUS, "-", startLine)); break;
            case '*': tokens.add(new Token(TokenType.MULTIPLY, "*", startLine)); break;
            case '/': tokens.add(new Token(TokenType.DIVIDE, "/", startLine)); break;
            case '%': tokens.add(new Token(TokenType.MODULO, "%", startLine)); break;
            case '&': tokens.add(new Token(TokenType.AMPERSAND, "&", startLine)); break;
            case '$': tokens.add(new Token(TokenType.DOLLAR, "$", startLine)); break;
            case '(': tokens.add(new Token(TokenType.LPAREN, "(", startLine)); break;
            case ')': tokens.add(new Token(TokenType.RPAREN, ")", startLine)); break;
            case ':': tokens.add(new Token(TokenType.COLON, ":", startLine)); break;
            case ',': tokens.add(new Token(TokenType.COMMA, ",", startLine)); break;
            case '=': tokens.add(new Token(TokenType.ASSIGN, "=", startLine)); break;
            case '>':
                if (pos < source.length() && peek() == '=') {
                    advance();
                    tokens.add(new Token(TokenType.GREATER_EQUAL, ">=", startLine));
                } else {
                    tokens.add(new Token(TokenType.GREATER, ">", startLine));
                }
                break;
            case '<':
                if (pos < source.length() && peek() == '=') {
                    advance();
                    tokens.add(new Token(TokenType.LESS_EQUAL, "<=", startLine));
                } else if (pos < source.length() && peek() == '>') {
                    advance();
                    tokens.add(new Token(TokenType.NOT_EQUAL, "<>", startLine));
                } else {
                    tokens.add(new Token(TokenType.LESS, "<", startLine));
                }
                break;
            default:
                throw new LexerException(startLine,
                        "Unexpected character: '" + c + "'");
        }
    }
}