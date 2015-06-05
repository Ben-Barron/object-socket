package com.benbarron.objectsocket;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface Stream<T> {

    <R> Stream<R> extend(Function<Observer<R>, Observer<T>> extension);

    default <R> Stream<R> extend(BiConsumer<T, Observer<R>> extension) {
        return extend(next -> {
            return new Observer<T>() {
                @Override
                public void complete() {
                    next.complete();
                }

                @Override
                public void error(Throwable throwable) {
                    next.error(throwable);
                }

                @Override
                public void next(T item) {
                    extension.accept(item, next);
                }
            };
        });
    }

    default CompletableFuture<T> firstCompletableFuture() {
        return null;
    }

    default Stream<T> filter(Predicate<T> predicate) {
        return extend((item, observer) -> {
            if (predicate.test(item)) {
                observer.next(item);
            }
        });
    }

    default <R> Stream<R> map(Function<T, R> mapper) {
        return extend((item, observer) -> observer.next(mapper.apply(item)));
    }

    default <R extends T> Stream<R> ofType(Class<R> type) {
        return filter(item -> item.getClass().isAssignableFrom(type))
            .map(item -> (R) item);
    }

    default Stream<T> onNext(Consumer<T> callback) {
        return extend((item, observer) -> {
            callback.accept(item);
            observer.next(item);
        });
    }
}
