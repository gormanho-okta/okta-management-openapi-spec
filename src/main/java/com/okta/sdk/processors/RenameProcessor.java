package com.okta.sdk.processors;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public abstract class RenameProcessor implements Processor {
    private final List<Map<String, String>> renames;

    public RenameProcessor(Object parameters) {
        this.renames = (List<Map<String, String>>) parameters;
    }

    protected void forEachRename(Consumer<Map<String, String>> rename) {
        if (renames != null) {
            ((List<Map<String, String>>) renames).forEach(rename);
        }
    }
}
