package com.anczykowski.interpreter.value;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class ListValue implements Value {

    @Getter
    @SuppressWarnings("UnusedAssignment")
    ArrayList<ValueProxy> values = new ArrayList<>();

    public ListValue(ArrayList<ValueProxy> values) {
        this.values = new ArrayList<>(values);
    }

    @Override
    public String toString() {
        return "list(" + StringUtils.join(values.stream().map(vp -> vp.getValue().toString()).toList(), ", ") + ")";
    }
}
