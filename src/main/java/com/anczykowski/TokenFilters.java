package com.anczykowski;

import java.util.function.Predicate;

public class TokenFilters {

    public static Predicate<Token> getWhitespaceFilter() {
        return token -> !token.type.equals(TokenType.WHITESPACE);
    }
}
