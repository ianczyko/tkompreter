package com.anczykowski.errormodule;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.anczykowski.lexer.Location;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorElement {

    private Location location;

    private String codeLineBuffer;

    private String underlineFragment;

    private String explanation;

    private ErrorType errorType;

    private final LocalDateTime timestamp = LocalDateTime.now();

    @SuppressWarnings("UnnecessaryDefault")
    @Override
    public String toString() {
        var explanationText = getExplanationText();
        var underline = getUnderlineText();
        var msg = switch (errorType) {
            case TOKEN_TOO_LONG -> "error: too long token";
            case INCONSISTENT_LINE_ENDINGS -> "error: inconsistent line endings";
            case UNKNOWN_CHARACTER -> "error: unknown character";
            case UNCLOSED_STRING -> "error: string not closed";
            case UNEXPECTED_TOKEN -> "error: unexpected token";
            case CONSTANT_TOO_BIG -> "error: constant exceeded max size";
            case MALFORMED_NUMBER -> "error: malformed number";
            case ALREADY_DECLARED -> "error: already declared";
            case DUPLICATE_LABEL -> "error: duplicate label";
            case UNSUPPORTED_CHAINING -> "error: unsupported chaining";
            case UNMATCHED_ARGUMENTS -> "error: unmatched arguments";
            case UNSUPPORTED_OPERATION -> "error: unsupported operation";
            case UNDECLARED_VARIABLE -> "error: undeclared variable";
            case DIVISION_BY_ZERO -> "error: division by zero";
            case UNDEFINED_SYMBOL -> "error: undefined symbol";
            default -> "unknownError";
        };
        var locationString = location == null ? "" : location.toString();
        var codeLineBufferString = codeLineBuffer == null ? "" : codeLineBuffer.trim().lines().map(String::trim).collect(Collectors.joining("\n"));
        return "%s: %s%s\n%s%s\n".formatted(locationString, msg, explanationText, codeLineBufferString, underline);

    }

    public String getExplanationText() {
        return explanation == null ? "" : " (" + explanation + ")";
    }

    public String getUnderlineText() {
        AtomicReference<String> underlineResult = new AtomicReference<>("");
        if (underlineFragment != null) {
            var codeLineBufferString = codeLineBuffer == null ? "" : codeLineBuffer.trim().lines().map(String::trim).collect(Collectors.joining("\n"));
            codeLineBufferString.lines().forEach(codeLineFragment -> {
                var underlineIndex = codeLineFragment.lastIndexOf(underlineFragment);
                if (underlineIndex > -1) {
                    underlineResult.set("\n" + " ".repeat(underlineIndex) + "~".repeat(underlineFragment.length()));
                }
            });
        }

        return underlineResult.get();
    }
}
