package com.anczykowski.lexer.helpers;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import com.anczykowski.errormodule.ErrorModule;
import com.anczykowski.lexer.Source;

public class SourceHelpers {
    public static Source thereIsSource(String input, ErrorModule errorModule){
        return new Source(errorModule, new StringReader(input));
    }

    public static String createUnicodeString(String str){
        return new String(str.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }

}
