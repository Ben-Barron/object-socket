package com.benbarron.objectsocket;

import java.io.Serializable;

class ClassLoaderRequest implements Serializable {

    private final String name;

    public ClassLoaderRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

