package com.benbarron.objectsocket;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ForkJoinPool;

class DelimitedSocketChannel implements AutoCloseable {

    private static final int DEFAULT_BUFFER_SIZE = 8192;

    private final ByteBuffer readBuffer = ByteBuffer.allocateDirect(DEFAULT_BUFFER_SIZE);
    private final AsynchronousSocketChannel channel;
    private final SimpleStream<byte[]> messagesStream = new SimpleStream<>();

    public DelimitedSocketChannel(AsynchronousSocketChannel underlyingChannel) {
        this.channel = underlyingChannel;
    }

    @Override
    public void close() throws Exception {
        channel.close();
    }

    public Stream<byte[]> messages() {
        return messagesStream;
    }

    public void start() {
        channel.read(readBuffer, null, new CompletionHandler<Integer, Object>() {

            private volatile ByteBuffer currentObjectBuffer = null;

            @Override
            public void completed(Integer result, Object attachment) {
                do {
                    if (currentObjectBuffer == null) {
                        if (readBuffer.remaining() < Integer.BYTES) {
                            readBuffer.compact();
                            break;
                        }

                        currentObjectBuffer = ByteBuffer.allocate(readBuffer.getInt());
                    }

                    currentObjectBuffer.put(readBuffer);

                    if (!currentObjectBuffer.hasRemaining()) {
                        byte[] objectBytes = currentObjectBuffer.array();
                        ForkJoinPool.commonPool().execute(() -> messagesStream.next(objectBytes));

                        currentObjectBuffer = null;
                    }

                    if (readBuffer.hasRemaining()) {
                        continue;
                    }

                    readBuffer.clear();
                    break;
                } while (true);

                channel.read(readBuffer, null, this);
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                messagesStream.error(exc);
            }
        });
    }

    public void write(byte[] message) {
        ByteBuffer buffer = ByteBuffer.allocate(4 + message.length);
        buffer.putInt(message.length);
        buffer.put(message);
        buffer.flip();

        channel.write(buffer);
    }
}
