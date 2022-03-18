package com.okta.sdk.processors;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public abstract class RenameProcessor implements Processor {
    private final List<Map<String, String>> renames;

    public RenameProcessor(Object parameters) {
        this.renames = (List<Map<String, String>>) parameters;
    }

    protected Stream<Map<String, String>> getRenames() {
        return renames == null ? Stream.empty() : renames.stream();
    }
}
