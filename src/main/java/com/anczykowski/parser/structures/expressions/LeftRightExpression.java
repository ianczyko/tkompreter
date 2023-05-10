package com.anczykowski.parser.structures.expressions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LeftRightExpression extends Expression{
    @Getter
    private final Expression left;

    @Getter
    private final Expression right;
}
