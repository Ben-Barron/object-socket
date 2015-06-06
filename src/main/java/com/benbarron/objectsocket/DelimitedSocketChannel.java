package com.benbarron.objectsocket;

import com.benbarron.flow.Flow;
import com.benbarron.flow.SimpleFlow;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ForkJoinPool;

class DelimitedSocketChannel implements AutoCloseable {

    private static final int DEFAULT_BUFFER_SIZE = 8192;

    private final AsynchronousSocketChannel channel;
    private final SimpleFlow<byte[]> messages = new SimpleFlow<>();
    private final ByteBuffer readBuffer;

    public DelimitedSocketChannel(AsynchronousSocketChannel underlyingChannel) {
        this(underlyingChannel, DEFAULT_BUFFER_SIZE);
    }

    public DelimitedSocketChannel(AsynchronousSocketChannel underlyingChannel,
                                  int bufferSize) {

        this.channel = underlyingChannel;
        this.readBuffer = ByteBuffer.allocateDirect(bufferSize);
    }

    @Override
    public void close() throws Exception {
        channel.close();
    }

    public Flow<byte[]> messages() {
        return messages;
    }

    public void start() {
        channel.read(readBuffer, null, new DelimitedReadCompletionHandler(channel, messages, readBuffer));
    }

    public void write(byte[] message) {
        ByteBuffer buffer = ByteBuffer.allocate(4 + message.length);
        buffer.putInt(message.length);
        buffer.put(message);
        buffer.flip();

        channel.write(buffer);
    }
}
