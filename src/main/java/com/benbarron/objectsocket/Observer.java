package com.benbarron.objectsocket;

public interface Observer<T> {

    void complete();

    void error(Throwable throwable);

    void next(T item);
}
