package com.benbarron.objectsocket.example;

import com.benbarron.objectsocket.ObjectSocketChannel;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

interface Client {

    static void main(String[] args) throws Exception {
        AsynchronousSocketChannel socketChannel = AsynchronousSocketChannel.open();
        socketChannel.connect(new InetSocketAddress(8000), null, new CompletionHandler<Void, Object>() {
            @Override
            public void completed(Void result, Object attachment) {
                ObjectSocketChannel channel = new ObjectSocketChannel(socketChannel);
                channel.start();
                channel.write(new S());
                channel.write(new S());
                channel.write(new S());
                channel.write(new S());
            }

            @Override
            public void failed(Throwable exc, Object attachment) {

            }
        });

        Thread.sleep(Long.MAX_VALUE);
    }

    public class S implements Serializable { }
}
