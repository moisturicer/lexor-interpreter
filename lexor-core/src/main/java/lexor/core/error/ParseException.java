package lexor.core.error;

public class ParseException extends LexorException {
    public ParseException(int line, String message) {
        super(line, "[Line " + line + "] Parse error: " + message);
    }
}