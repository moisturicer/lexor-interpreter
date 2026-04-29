package lexor.core.error;

public class LexorException extends RuntimeException {
    private final int line;

    public LexorException(int line, String message) {
        super(message);
        this.line = line;
    }

    public int getLine() {
        return line;
    }
}