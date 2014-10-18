package io.github.lucaseasedup.logit.common;

public interface Disposable
{
    /**
     * Nullifies and/or empties (if possible) all the member fields in this object,
     * creating a chance for the garbage collector
     * to deallocate as many unused objects as possible.
     */
    public void dispose();
}
