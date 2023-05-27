package com.anczykowski.interpreter.value;

import com.anczykowski.interpreter.Context;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class ClassValue implements Value {

    @Getter
    private final String classIdentifier;

    @Getter
    private Context classContext;
}
