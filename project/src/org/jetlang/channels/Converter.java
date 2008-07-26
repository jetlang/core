package org.jetlang.channels;

/**
 * User: mrettig
 * Date: Jul 22, 2008
 * Time: 3:59:33 PM
 */
public interface Converter<T, K> {
    public K Convert(T msg);
}
