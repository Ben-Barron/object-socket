package com.benbarron.objectsocket;

import com.benbarron.flow.Flow;
import org.nustaq.serialization.simpleapi.DefaultCoder;

import java.io.Serializable;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

public class ObjectSocketChannel implements AutoCloseable {

    private final ThreadLocal<DefaultCoder> objectSerializer;
    private final DelimitedSocketChannel channel;

    public ObjectSocketChannel(AsynchronousSocketChannel underlyingChannel) {
        ClassLoader classLoader =  new ConcurrentFactoryClassLoader(request -> {
            CompletableFuture<ClassLoaderResponse> response = messages()
                .ofType(ClassLoaderResponse.class)
                .filter(clr -> clr.getName().equals(request.getName()))
                .firstToCompletableFuture();

            write(request);

            return response;
        });

        this.channel = new DelimitedSocketChannel(underlyingChannel);
        this.objectSerializer = ThreadLocal.withInitial(() -> {
            DefaultCoder defaultCoder = new DefaultCoder();
            defaultCoder.getConf().setClassLoader(classLoader);
            return defaultCoder;
        });
    }

    @Override
    public void close() throws Exception {
        channel.close();
        objectSerializer.remove();
    }

    public Flow<Object> messages() {
        return channel.messages()
            .executeOn(ForkJoinPool.commonPool())
            .map(bytes -> {
                DefaultCoder coder = objectSerializer.get();
                return coder.toObject(bytes);
            });
    }

    public void start() {
        channel.start();
    }

    public <T extends Serializable> void write(T message) {
        channel.write(objectSerializer.get().toByteArray(message));
    }
}
