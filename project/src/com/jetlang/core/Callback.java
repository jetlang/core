package com.jetlang.core;

public interface Callback<T> {
    void onMessage(T message);
}
