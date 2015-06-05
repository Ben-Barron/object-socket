package com.benbarron.objectsocket;

import java.util.Iterator;

class ThreadSafeIterable<T> implements Iterable<T> {

    public void add(T item) {

    }

    @Override
    public Iterator<T> iterator() {
        return null;
    }

    public boolean remove(Object item) {
        return true;
    }
}
