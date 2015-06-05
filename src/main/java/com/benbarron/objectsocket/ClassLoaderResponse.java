package com.benbarron.objectsocket;

import java.io.Serializable;

class ClassLoaderResponse implements Serializable {

    private final String name;
    private final byte[] definition;

    public ClassLoaderResponse(String name, byte[] definition) {
        this.name = name;
        this.definition = definition;
    }

    public String getName() {
        return name;
    }

    public byte[] getDefinition() {
        return definition;
    }
}
