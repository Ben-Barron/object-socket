package com.benbarron.objectsocket.example;

import com.benbarron.objectsocket.ObjectSocketServerChannel;

interface Server {

    static void main(String[] args) throws Exception {
        ObjectSocketServerChannel objectSocketServerChannel = ObjectSocketServerChannel.open(8000);
        objectSocketServerChannel.connections()
            .subscribe(socket -> {
                socket.messages().subscribe(System.out::println);
                socket.start();
            });

        objectSocketServerChannel.start();

        Thread.sleep(Long.MAX_VALUE);
    }
}
