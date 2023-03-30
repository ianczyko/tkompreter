package com.anczykowski.lexer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.MessageFormat;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.anczykowski.lexer.helpers.SourceHelpers;

class SourceTest {

    @Test
    void sourceGetFirstCharacter() {
        // given
        try (var src = SourceHelpers.thereIsSource("abc")) {
            // when
            src.fetchCharacter();

            // then
            assertEquals("a", src.getCurrentCharacter());
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
            assertEquals("b", src.getCurrentCharacter());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"\n", "\r", "\n\r", "\r\n"})
    void handleNewLines(String newline) {
        // given
        var str = MessageFormat.format("a{0}b{0}c", newline);
        try (var src = SourceHelpers.thereIsSource(str)) {
            // when
            src.fetchCharacter();
            src.fetchCharacter();

            // then
            assertEquals("\n", src.getCurrentCharacter());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"\n", "\r", "\n\r", "\r\n"})
    void handleNewLinesSecond(String newline) {
        // given
        var str = MessageFormat.format("a{0}b{0}c", newline);
        try (var src = SourceHelpers.thereIsSource(str)) {
            // when
            src.fetchCharacter();
            src.fetchCharacter();
            src.fetchCharacter();
            src.fetchCharacter();

            // then
            assertEquals("\n", src.getCurrentCharacter());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"\n", "\r", "\n\r", "\r\n"})
    void handleNewLinesEnd(String newline) {
        // given
        var str = MessageFormat.format("a{0}b{0}c", newline);
        try (var src = SourceHelpers.thereIsSource(str)) {
            // when
            src.fetchCharacter();
            src.fetchCharacter();
            src.fetchCharacter();
            src.fetchCharacter();
            src.fetchCharacter();
            src.fetchCharacter();

            // then
            assertTrue(src.isEOF());
        }
    }
}