package com.anczykowski.interpreter.value;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class FloatValue implements Value {
    private float value;
}
