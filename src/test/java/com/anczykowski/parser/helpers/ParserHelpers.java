package com.anczykowski.parser.helpers;

import java.util.List;

import com.anczykowski.lexer.Lexer;
import com.anczykowski.lexer.Token;

public class ParserHelpers {
    public static Lexer thereIsLexer(List<Token> tokens){
        return new MockLexer(tokens.iterator());
    }
}
