package com.anczykowski.errormodule;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.anczykowski.lexer.Location;

class ErrorModuleTest {

    @Test
    void getUnderlineText() {
        // given
        var errorElement = ErrorElement.builder()
            .errorType(ErrorType.TOKEN_TOO_LONG)
            .codeLineBuffer("var AAAAA = 5;")
            .underlineFragment("AAAAA")
            .location(new Location())
            .build();

        // when
        var underlineText = errorElement.getUnderlineText();

        // then
        //                       var AAAAA = 5;
        assertEquals("\n    ~~~~~", underlineText);

    }

    @Test
    void getUnderlineTextTwoIdentifiers() {
        // given
        var errorElement = ErrorElement.builder()
            .errorType(ErrorType.TOKEN_TOO_LONG)
            .codeLineBuffer("var AAAAA = AAAAA;")
            .underlineFragment("AAAAA")
            .location(new Location())
            .explanation("%d character limit exceeded".formatted(5))
            .build();

        // when
        var underlineText = errorElement.getUnderlineText();

        // then
        //                       var AAAAA = AAAAA;
        assertEquals("\n            ~~~~~", underlineText);

    }

    @Test
    void getExplanationText() {
        // given
        var errorElement = ErrorElement.builder()
            .errorType(ErrorType.TOKEN_TOO_LONG)
            .codeLineBuffer("var AAAAA = 5;")
            .underlineFragment("AAAAA")
            .location(new Location())
            .explanation("%d character limit exceeded".formatted(5))
            .build();

        // when
        var underlineText = errorElement.getExplanationText();

        // then
        assertEquals(" (5 character limit exceeded)", underlineText);

    }

}