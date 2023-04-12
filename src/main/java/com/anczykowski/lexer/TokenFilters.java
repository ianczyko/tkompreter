package com.anczykowski.lexer;

import java.util.function.Predicate;

public class TokenFilters {

    public static Predicate<Token> getCommentFilter() {
        return token -> !token.getType().equals(TokenType.COMMENT);
    }
}
