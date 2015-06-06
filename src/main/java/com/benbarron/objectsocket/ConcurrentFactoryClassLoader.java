package com.benbarron.objectsocket;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

class ConcurrentFactoryClassLoader extends ClassLoader {

    static {
        registerAsParallelCapable();
    }


    private final Function<ClassLoaderRequest, CompletableFuture<ClassLoaderResponse>> factory;

    public ConcurrentFactoryClassLoader(Function<ClassLoaderRequest, CompletableFuture<ClassLoaderResponse>> factory) {
        this.factory = factory;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] definition;

        System.out.println(name);

        try {
            definition = factory.apply(new ClassLoaderRequest(name)).get().getDefinition();
        } catch (Exception e) {
            throw new ClassNotFoundException(name);
        }

        return defineClass(name, definition, 0, definition.length);
    }
}
