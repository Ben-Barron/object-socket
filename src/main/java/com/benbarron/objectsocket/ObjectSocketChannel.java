package com.benbarron.objectsocket;

import java.io.Serializable;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.CompletableFuture;

public class ObjectSocketChannel implements AutoCloseable {

    private final DelimitedSocketChannel channel;
    private final ConcurrentFactoryClassLoader classLoader;

    public ObjectSocketChannel(AsynchronousSocketChannel underlyingChannel) {
        this.channel = new DelimitedSocketChannel(underlyingChannel);
        this.classLoader = new ConcurrentFactoryClassLoader(request -> {
            CompletableFuture<ClassLoaderResponse> response = messages(ClassLoaderResponse.class)
                .filter(clr -> clr.getName().equals(request.getName()))
                    .firstCompletableFuture();

            write(request);

            return response;
        });
    }

    @Override
    public void close() throws Exception {
        channel.close();
    }

    public Stream<Object> messages() {
        return channel.messages()
            .map(bytes -> new Object());
    }

    public <T> Stream<T> messages(Class<T> type) {
        return messages().ofType(type);
    }

    public void start() {
        channel.start();
    }

    public <T extends Serializable> void write(T message) {
        byte[] array = null;
        channel.write(array);
    }
}
