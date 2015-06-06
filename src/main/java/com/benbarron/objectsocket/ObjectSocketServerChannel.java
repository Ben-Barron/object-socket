package com.benbarron.objectsocket;

import com.benbarron.flow.Flow;
import com.benbarron.flow.SimpleFlow;

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public interface ObjectSocketServerChannel {

    Flow<ObjectSocketChannel> connections();

    void start();


    static ObjectSocketServerChannel open(int port) throws Exception {
        SimpleFlow<ObjectSocketChannel> flow = new SimpleFlow<>();
        AsynchronousServerSocketChannel serverSocketChannel = AsynchronousServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));

        return new ObjectSocketServerChannel() {
            @Override
            public Flow<ObjectSocketChannel> connections() {
                return flow;
            }

            @Override
            public void start() {
                serverSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
                    @Override
                    public void completed(AsynchronousSocketChannel result, Object attachment) {
                        flow.next(new ObjectSocketChannel(result));
                        serverSocketChannel.accept(null, this);
                    }

                    @Override
                    public void failed(Throwable exc, Object attachment) {
                        flow.error(exc);
                    }
                });
            }
        };
    }
}
