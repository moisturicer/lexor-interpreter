package lexor.core.interpreter;

import lexor.core.lexer.TokenType;

import java.util.HashMap;
import java.util.Map;

/**
 * Symbol table that holds variable names → LexorValue.
 * Supports nested scopes (for loops, if blocks) via a parent reference.
 */
public class Environment {

    private final Map<String, LexorValue> store = new HashMap<>();
    private final Map<String, TokenType>  types = new HashMap<>();
    private final Environment parent;

    /** Root scope (no parent). */
    public Environment() {
        this.parent = null;
    }

    /** Child scope — looks up parent chain on get. */
    public Environment(Environment parent) {
        this.parent = parent;
    }

    // ── declaration ────────────────────────────────────────────

    /** Declare a variable in the current scope with a default value. */
    public void declare(String name, TokenType dataType, LexorValue value) {
        types.put(name, dataType);
        store.put(name, value);
    }

    // ── get ────────────────────────────────────────────────────

    public LexorValue get(String name) {
        if (store.containsKey(name)) return store.get(name);
        if (parent != null)          return parent.get(name);
        throw new RuntimeException("Undefined variable: " + name);
    }

    public TokenType getType(String name) {
        if (types.containsKey(name)) return types.get(name);
        if (parent != null)          return parent.getType(name);
        throw new RuntimeException("Undefined variable: " + name);
    }

    // ── set ────────────────────────────────────────────────────

    /** Assign to the nearest scope that owns the variable. */
    public void set(String name, LexorValue value) {
        if (store.containsKey(name)) {
            store.put(name, value);
            return;
        }
        if (parent != null) {
            parent.set(name, value);
            return;
        }
        throw new RuntimeException("Undefined variable: " + name);
    }

    public boolean isDeclared(String name) {
        if (store.containsKey(name)) return true;
        if (parent != null)          return parent.isDeclared(name);
        return false;
    }
}