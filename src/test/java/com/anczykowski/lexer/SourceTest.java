package com.anczykowski.lexer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.anczykowski.lexer.helpers.SourceHelpers;

class SourceTest {

    @Test
    void sourceGetFirstCharacter() {
        // given
        try (var src = SourceHelpers.thereIsSource("abc")) {
            // when
            src.fetchCharacter();

            // then
            assertEquals(src.getCurrentCharacter(), 'a');
        }
    }

    @Test
    void sourceGetSecondCharacter() {
        // given
        try (var src = SourceHelpers.thereIsSource("abc")) {
            // when
            src.fetchCharacter();
            src.fetchCharacter();

            // then
            assertEquals(src.getCurrentCharacter(), 'b');
        }
    }

}