package com.anczykowski.parser.structures.expressions;

import com.anczykowski.lexer.Location;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@AllArgsConstructor
public class LeftRightExpression extends Expression{
    @Getter
    private final Expression left;

    @Getter
    private final Expression right;

    @Getter
    private Location location = null;

    @Getter
    private String characterBuffer = null;
}
