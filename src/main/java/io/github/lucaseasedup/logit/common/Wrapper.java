package io.github.lucaseasedup.logit.common;

public final class Wrapper<T>
{
    public Wrapper()
    {
        this.obj = null;
    }
    
    public Wrapper(T obj)
    {
        this.obj = obj;
    }
    
    public T get()
    {
        return obj;
    }
    
    public void set(T obj)
    {
        this.obj = obj;
    }
    
    private T obj;
}
