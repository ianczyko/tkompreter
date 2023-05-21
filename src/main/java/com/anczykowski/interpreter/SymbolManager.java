package com.anczykowski.interpreter;

import com.anczykowski.parser.structures.ClassDef;
import com.anczykowski.parser.structures.FuncDef;

import java.util.HashMap;

public class SymbolManager {

    private final HashMap<String, FuncDef> functions = new HashMap<>();
    private final HashMap<String, ClassDef> classes = new HashMap<>();

    public void addFunctions(HashMap<String, FuncDef> functions) {
        this.functions.putAll(functions);
    }

    public void addClasses(HashMap<String, ClassDef> classes) {
        this.classes.putAll(classes);
    }

    public FuncDef getFunction(String function) {
        return functions.get(function);
    }

    public ClassDef getClass(String cls) {
        return classes.get(cls);
    }
}
