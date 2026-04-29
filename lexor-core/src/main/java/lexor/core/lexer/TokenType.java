package lexor.core.lexer;

public enum TokenType {
    // data types
    INT, FLOAT, CHAR, BOOL,

    // keywords
    SCRIPT, AREA, START, END,
    DECLARE, PRINT, SCAN,
    IF, ELSE, FOR, REPEAT, WHEN,

    // logical operators
    AND, OR, NOT,

    // bool literals
    TRUE, FALSE,

    // literals
    INT_LITERAL, FLOAT_LITERAL,
    CHAR_LITERAL, STRING_LITERAL,

    // identifier (variable name)
    IDENTIFIER,

    // arithmetic operators
    PLUS, MINUS, MULTIPLY, DIVIDE, MODULO,

    // comparison operators
    GREATER, LESS,
    GREATER_EQUAL, LESS_EQUAL,
    EQUAL, NOT_EQUAL,

    // assignment
    ASSIGN,

    // special LEXOR symbols
    AMPERSAND,   // &  concatenator
    DOLLAR,      // $  newline
    LBRACKET,    // [  escape code start
    RBRACKET,    // ]  escape code end

    // punctuation
    LPAREN, RPAREN,
    COLON, COMMA,

    // end of file
    EOF
}