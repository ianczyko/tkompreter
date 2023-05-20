package com.anczykowski.interpreter;

import java.util.ArrayList;

import lombok.Getter;


public class ContextManager {
    private ArrayList<Context> contexts = new ArrayList<Context>();

    @Getter
    private final SymbolManager globalSymbolManager = new SymbolManager();
}
