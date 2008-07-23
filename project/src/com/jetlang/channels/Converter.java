package com.jetlang.channels;

/**
 * Created by IntelliJ IDEA.
 * User: mrettig
 * Date: Jul 22, 2008
 * Time: 3:59:33 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Converter<T,K> {
    public K Convert(T msg);
}
