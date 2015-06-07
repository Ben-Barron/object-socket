package com.benbarron.objectsocket;

import com.benbarron.flow.Producer;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.CompletionHandler;

class DelimitedReadCompletionHandler implements CompletionHandler<Integer, Object> {

    private final AsynchronousByteChannel channel;
    private final Producer<byte[]> producer;
    private final ByteBuffer channelReadBuffer; // this is a direct buffer.

    private volatile ByteBuffer currentMessageBuffer;

    public DelimitedReadCompletionHandler(AsynchronousByteChannel channel,
                                          Producer<byte[]> producer,
                                          ByteBuffer channelReadBuffer) {

        this.channel = channel;
        this.producer = producer;
        this.channelReadBuffer = channelReadBuffer;
    }

    @Override
    public void completed(Integer result,
                          Object attachment) {

        if (result < 0) {
            producer.complete();
            return;
        }

        channelReadBuffer.flip();

        do {
            if (currentMessageBuffer == null) {
                if (channelReadBuffer.remaining() < Integer.BYTES) {
                    channelReadBuffer.compact();
                    break;
                }

                currentMessageBuffer = ByteBuffer.allocate(channelReadBuffer.getInt());
            }

            int copyLength = Integer.min(currentMessageBuffer.remaining(), channelReadBuffer.remaining());

            for (int i = 0; i < copyLength; i++) {
                currentMessageBuffer.put(channelReadBuffer.get());
            }

            if (!currentMessageBuffer.hasRemaining()) {
                producer.next(currentMessageBuffer.array());
                currentMessageBuffer = null;
            }

            if (channelReadBuffer.hasRemaining()) {
                continue;
            }

            channelReadBuffer.clear();
            break;
        } while (true);

        channel.read(channelReadBuffer, null, this);
    }

    @Override
    public void failed(Throwable exc,
                       Object attachment) {

        producer.error(exc);
    }
}
