package lexor.core.error;

public class LexerException extends RuntimeException {
    public LexerException(int line, String message) {
        super("[Line " + line + "] Lexer error: " + message);
    }
}