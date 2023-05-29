package com.anczykowski.interpreter.value;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class ValueProxy {

    @Getter
    @Setter
    private Value value;
}
