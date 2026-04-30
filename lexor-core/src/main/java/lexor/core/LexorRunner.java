package lexor.core;

import lexor.core.ast.ProgramNode;
import lexor.core.error.LexorException;
import lexor.core.interpreter.Interpreter;
import lexor.core.lexer.Lexer;
import lexor.core.lexer.Token;
import lexor.core.parser.Parser;

import java.util.List;

public class LexorRunner {

    public static String run(String sourceCode) {
        try {
            Lexer lexer = new Lexer(sourceCode);
            List<Token> tokens = lexer.tokenize();

            Parser parser = new Parser(tokens);
            ProgramNode program = parser.parse();

            Interpreter interpreter = new Interpreter();
            return interpreter.run(program);

        } catch (LexorException e) {
            return "error: " + e.getMessage();
        } catch (RuntimeException e) {
            return "error: " + e.getMessage();
        }
    }
}