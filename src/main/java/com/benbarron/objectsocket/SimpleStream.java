package com.benbarron.objectsocket;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

class SimpleStream<T> implements Stream<T>, Observer<T> {

    private final ThreadSafeIterable<Observer<T>> observers = new ThreadSafeIterable<>();
    private final AtomicBoolean isStopped = new AtomicBoolean(false);

    @Override
    public void complete() {
        if (!isStopped.compareAndSet(false, true)) {
            return;
        }

        observers.forEach(Observer::complete);
    }

    @Override
    public void error(Throwable throwable) {
        if (!isStopped.compareAndSet(false, true)) {
            return;
        }

        observers.forEach(observer -> observer.error(throwable));
    }

    @Override
    public void next(T item) {
        if (isStopped.get()) {
            return;
        }

        observers.forEach(observer -> observer.next(item));
    }

    @Override
    public <R> Stream<R> extend(Function<Observer<R>, Observer<T>> extension) {
        SimpleStream<R> newStream = new SimpleStream<>();
        observers.add(extension.apply(newStream));
        return newStream;
    }
}
