package com.anczykowski.lexer.helpers;

import java.io.StringReader;

import com.anczykowski.lexer.Source;

public class SourceHelpers {
    public static Source thereIsSource(String input){
        return new Source(new StringReader(input));
    }
}
