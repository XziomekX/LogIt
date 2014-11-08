package io.github.lucaseasedup.logit.util;

import java.lang.reflect.Array;

public final class ArrayUtils
{
    private ArrayUtils()
    {
    }
    
    public static int[][] copy(int[][] src)
    {
        if (src == null)
            throw new IllegalArgumentException();
        
        int[][] dest = new int[src.length][src[0].length];
        
        for (int i = 0; i < src.length; i++)
        {
            System.arraycopy(src[i], 0, dest[i], 0, src[i].length);
        }
        
        return dest;
    }
    
    public static <T> T[][] copy(Class<T> clazz, T[][] src)
    {
        if (clazz == null || src == null)
            throw new IllegalArgumentException();
        
        @SuppressWarnings("unchecked")
        T[][] dest = (T[][]) Array.newInstance(clazz, src.length, src[0].length);
        
        for (int i = 0; i < src.length; i++)
        {
            System.arraycopy(src[i], 0, dest[i], 0, src[i].length);
        }
        
        return dest;
    }
    
    public static final String[] NO_STRINGS = new String[0];
}
