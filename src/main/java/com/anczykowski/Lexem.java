package com.anczykowski;

import lombok.Builder;

@Builder
public class Lexem {
    LexemType type;

    String value;

    public String toString() {
        return "Lexem(type=" + this.type + ", value=[" + this.value + "])";
    }
}
