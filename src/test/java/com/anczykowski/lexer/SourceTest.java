package com.anczykowski.lexer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.MessageFormat;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.anczykowski.errormodule.ErrorModule;
import com.anczykowski.lexer.helpers.SourceHelpers;

class SourceTest {

    @Test
    void sourceGetFirstCharacter() {
        // given
        var errorModule = new ErrorModule();
        try (var src = SourceHelpers.thereIsSource("abc", errorModule)) {
            // when
            src.fetchCharacter();

            // then
            assertEquals("a", src.getCurrentCharacter());
        }
    }

    @Test
    void sourceGetSecondCharacter() {
        // given
        var errorModule = new ErrorModule();
        try (var src = SourceHelpers.thereIsSource("abc", errorModule)) {
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
        var errorModule = new ErrorModule();
        var str = MessageFormat.format("a{0}b{0}c", newline);
        try (var src = SourceHelpers.thereIsSource(str, errorModule)) {
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
        var errorModule = new ErrorModule();
        var str = MessageFormat.format("a{0}b{0}c", newline);
        try (var src = SourceHelpers.thereIsSource(str, errorModule)) {
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
        var errorModule = new ErrorModule();
        var str = MessageFormat.format("a{0}b{0}c", newline);
        try (var src = SourceHelpers.thereIsSource(str, errorModule)) {
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

    @Test
    void sourceCurrentPositionOneLine() {
        // given
        var errorModule = new ErrorModule();
        try (var src = SourceHelpers.thereIsSource("abc", errorModule)) {
            // when
            src.fetchCharacter();
            src.fetchCharacter();

            // then
            assertEquals(1, src.getCurrentLocation().getLineNumber());
            assertEquals(2, src.getCurrentLocation().getColumnNumber());
        }
    }

    @Test
    void sourceCurrentPositionNewline() {
        // given
        var errorModule = new ErrorModule();
        try (var src = SourceHelpers.thereIsSource("ab\ncd", errorModule)) {
            // when
            src.fetchCharacter();
            src.fetchCharacter();
            src.fetchCharacter();
            src.fetchCharacter();
            src.fetchCharacter();

            // then
            assertEquals(2, src.getCurrentLocation().getLineNumber());
            assertEquals(2, src.getCurrentLocation().getColumnNumber());
        }
    }

    @Test
    void sourceCurrentPositionNewlineCRLF() {
        // given
        var errorModule = new ErrorModule();
        try (var src = SourceHelpers.thereIsSource("ab\r\ncd", errorModule)) {
            // when
            src.fetchCharacter();
            src.fetchCharacter();
            src.fetchCharacter();
            src.fetchCharacter();
            src.fetchCharacter();

            // then
            assertEquals(2, src.getCurrentLocation().getLineNumber());
            assertEquals(2, src.getCurrentLocation().getColumnNumber());
        }
    }

}