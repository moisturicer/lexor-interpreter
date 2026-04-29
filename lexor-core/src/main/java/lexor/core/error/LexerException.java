package lexor.core.error;

public class LexerException extends LexorException {
    public LexerException(int line, String message) {
        super(line, "[Line " + line + "] Lexer error: " + message);
    }
}