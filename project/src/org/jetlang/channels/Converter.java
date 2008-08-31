package org.jetlang.channels;

/**
 * Interface to convert from T to K.
 */
public interface Converter<T, K> {
    K Convert(T msg);
}
