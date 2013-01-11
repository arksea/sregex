package arkco.sregex;

import java.util.HashMap;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: </p>
 *
 * @author ark arkqq@hotmail.com
 * @version $Revision: 1.1.5.1 $
 */

final class CacheEntry
{
    int index;
    Object value;
    Object key;

    CacheEntry(int index)
    {
        index = index;
        value = null;
        key = null;
    }
}

public class PatternCache
{
    public static final int DEFAULT_CAPACITY = 20;
    static int numEntries;

    /**
     * @directed
     */
    static CacheEntry[] cache;
    static HashMap table;
    static private int curent;

    static
    {
        numEntries = 0;
        curent=0;
        table = new HashMap(DEFAULT_CAPACITY);
        cache = new CacheEntry[DEFAULT_CAPACITY];
        int capacity = DEFAULT_CAPACITY;
        while (--capacity >= 0)
        {
            cache[capacity] = new CacheEntry(capacity);
        }
    }

    public static final synchronized Codes get(String pattern) throws PatternCompiler_E
    {
        Object obj=table.get(pattern);
        if(obj==null)
        {
            PatternCompiler compiler = new PatternCompiler();
            Codes codes = compiler.compile(pattern);
            addElement(pattern,codes);
            return codes;
        }
        else
        {
            return (Codes)((CacheEntry)obj).value;
        }
    }
    public static final synchronized void addElement(String key, Codes value)
    {
        int index;
        if (!isFull())
        {
            index = numEntries;
            ++numEntries;
        }
        else
        {
            index = curent;
            if (++curent >= cache.length)
            {
                curent = 0;
            }

            table.remove(cache[index].key);
        }
        cache[index].value = value;
        cache[index].key = key;
        table.put(key, cache[index]);
    }

    public static final boolean isFull()
    {
        return (numEntries >= cache.length);
    }
}

