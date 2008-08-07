package org.jetlang.core;

/**
 * User: mrettig
 * Date: Jul 22, 2008
 * Time: 4:07:41 PM
 */
public interface Disposable {

    /**
     * dispose of object. object is unusable after dispose is called.
     */
    void dispose();
}
