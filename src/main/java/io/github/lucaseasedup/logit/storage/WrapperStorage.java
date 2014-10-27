package io.github.lucaseasedup.logit.storage;

import io.github.lucaseasedup.logit.LogItCore;
import io.github.lucaseasedup.logit.logging.CustomLevel;
import io.github.lucaseasedup.logit.util.CollectionUtils;
import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public final class WrapperStorage extends Storage
{
    private WrapperStorage(Storage leading, CacheType cacheType)
    {
        if (leading == null || cacheType == null)
            throw new IllegalArgumentException();
        
        this.executorService = Executors.newSingleThreadExecutor();
        this.leading = leading;
        this.cacheType = cacheType;
        
        if (cacheType == CacheType.PRELOADED)
        {
            preloadedCache = new Hashtable<>();
        }
    }
    
    @Override
    public synchronized void connect() throws IOException
    {
        log(CustomLevel.INTERNAL, "WrapperStorage#connect()");
        
        leading.connect();
        
        for (Storage mirror : mirrors.keySet())
        {
            mirror.connect();
        }
    }
    
    @Override
    public synchronized boolean isConnected() throws IOException
    {
        log(CustomLevel.INTERNAL, "WrapperStorage#isConnected()");
        
        return leading.isConnected();
    }
    
    @Override
    public synchronized void ping() throws IOException
    {
        log(CustomLevel.INTERNAL, "WrapperStorage#ping()");
        
        executorService.submit(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    leading.ping();
                }
                catch (IOException ex)
                {
                    log(Level.WARNING, ex);
                }
                
                for (Storage mirror : mirrors.keySet())
                {
                    try
                    {
                        mirror.ping();
                    }
                    catch (IOException ex)
                    {
                        log(Level.WARNING, ex);
                    }
                }
            }
        });
    }
    
    @Override
    public synchronized void close() throws IOException
    {
        for (StorageObserver o : obs)
        {
            o.beforeClose();
        }
        
        log(CustomLevel.INTERNAL, "WrapperStorage#close()");
        
        leading.close();
        
        for (Storage mirror : mirrors.keySet())
        {
            mirror.close();
        }
    }
    
    @Override
    public synchronized List<String> getUnitNames() throws IOException
    {
        log(CustomLevel.INTERNAL, "WrapperStorage#getUnitNames()");
        
        return leading.getUnitNames();
    }
    
    @Override
    public synchronized Hashtable<String, DataType> getKeys(String unit) throws IOException
    {
        log(CustomLevel.INTERNAL, "WrapperStorage#getKeys(\"" + unit + "\")");
        
        return leading.getKeys(unit);
    }
    
    @Override
    public synchronized String getPrimaryKey(String unit) throws IOException
    {
        log(CustomLevel.INTERNAL, "WrapperStorage#getPrimaryKey(\"" + unit + "\")");
        
        return leading.getPrimaryKey(unit);
    }
    
    @Override
    public synchronized List<Storage.Entry> selectEntries(String unit) throws IOException
    {
        log(CustomLevel.INTERNAL, "WrapperStorage#selectEntries(\"" + unit + "\")");
        
        if (cacheType == CacheType.DISABLED)
        {
            return leading.selectEntries(unit);
        }
        else if (cacheType == CacheType.PRELOADED)
        {
            List<Storage.Entry> preloadedEntries = preloadedCache.get(unit);
            
            if (preloadedEntries == null || preloadedEntries.isEmpty())
            {
                preloadedCache.put(unit, leading.selectEntries(unit));
            }
            
            List<Storage.Entry> entries = new LinkedList<>();
            
            for (Storage.Entry entry : preloadedCache.get(unit))
            {
                entries.add(entry.copy());
            }
            
            return entries;
        }
        else
        {
            throw new RuntimeException("Unknown cache type: " + cacheType);
        }
    }
    
    @Override
    public synchronized List<Storage.Entry> selectEntries(String unit, List<String> keys)
            throws IOException
    {
        log(CustomLevel.INTERNAL, "WrapperStorage#selectEntries("
                                + "\"" + unit + "\", "
                                + Arrays.toString(keys.toArray()) + ")");
        
        if (cacheType == CacheType.DISABLED)
        {
            return leading.selectEntries(unit, keys);
        }
        else if (cacheType == CacheType.PRELOADED)
        {
            if (preloadedCache.containsKey(unit))
            {
                return copyEntries(preloadedCache.get(unit), keys, new SelectorConstant(true));
            }
            else
            {
                return leading.selectEntries(unit, keys);
            }
        }
        else
        {
            throw new RuntimeException("Unknown cache type: " + cacheType);
        }
    }
    
    @Override
    public synchronized List<Storage.Entry> selectEntries(String unit,
                                                          Selector selector)
            throws IOException
    {
        log(CustomLevel.INTERNAL, "WrapperStorage#selectEntries("
                                + "\"" + unit + "\", "
                                + SqlUtils.translateSelector(selector, "`", "'") + ")");
        
        if (cacheType == CacheType.DISABLED)
        {
            return leading.selectEntries(unit, selector);
        }
        else if (cacheType == CacheType.PRELOADED)
        {
            if (preloadedCache.containsKey(unit))
            {
                return copyEntries(preloadedCache.get(unit), null, selector);
            }
            else
            {
                return leading.selectEntries(unit, selector);
            }
        }
        else
        {
            throw new RuntimeException("Unknown cache type: " + cacheType);
        }
    }
    
    @Override
    public synchronized List<Storage.Entry> selectEntries(String unit,
                                                          List<String> keys,
                                                          Selector selector)
            throws IOException
    {
        log(CustomLevel.INTERNAL, "WrapperStorage#selectEntries("
                                + "\"" + unit + "\", "
                                + Arrays.toString(keys.toArray()) + ", "
                                + SqlUtils.translateSelector(selector, "`", "'") + ")");
        
        if (cacheType == CacheType.DISABLED)
        {
            return leading.selectEntries(unit, keys, selector);
        }
        else if (cacheType == CacheType.PRELOADED)
        {
            if (preloadedCache.containsKey(unit))
            {
                return copyEntries(preloadedCache.get(unit), keys, selector);
            }
            else
            {
                return leading.selectEntries(unit, keys, selector);
            }
        }
        else
        {
            throw new RuntimeException("Unknown cache type: " + cacheType);
        }
    }
    
    @Override
    public synchronized void createUnit(final String unit,
                                        final Hashtable<String, DataType> keys,
                                        final String primaryKey)
            throws IOException
    {
        log(CustomLevel.INTERNAL, "WrapperStorage#createUnit("
                                + "\"" + unit + "\", "
                                + "Hashtable {keys: ["
                                        + CollectionUtils.toString(keys.keys())
                                + "]})");
        
        leading.createUnit(unit, keys, primaryKey);
        
        walkMirrors(new UnitWalker()
        {
            @Override
            public void walk(Storage storage, String unit) throws IOException
            {
                storage.createUnit(unit, keys, primaryKey);
            }
        }, unit);
        
        if (cacheType == CacheType.PRELOADED)
        {
            if (!preloadedCache.containsKey(unit))
            {
                preloadedCache.put(unit, new LinkedList<Storage.Entry>());
            }
        }
        
        for (StorageObserver o : obs)
        {
            o.afterCreateUnit(unit, keys);
        }
    }
    
    @Override
    public synchronized void renameUnit(String unit, String newName) throws IOException
    {
        log(CustomLevel.INTERNAL, "WrapperStorage#renameUnit("
                                + "\"" + unit + "\", "
                                + "\"" + newName + "\")");
        
        if (unit.equals(newName))
            throw new IllegalArgumentException();
        
        leading.renameUnit(unit, newName);
        
        for (Map.Entry<Storage, Hashtable<String, String>> e : mirrors.entrySet())
        {
            String unitMapping = e.getValue().get(unit);
            
            if (unitMapping == null)
            {
                unitMapping = unit;
            }
            
            e.getValue().remove(unit);
            e.getValue().put(newName, unitMapping);
        }
        
        if (cacheType == CacheType.PRELOADED)
        {
            if (preloadedCache.containsKey(unit))
            {
                preloadedCache.put(newName, preloadedCache.remove(unit));
            }
        }
        
        for (StorageObserver o : obs)
        {
            o.afterRenameUnit(unit, newName);
        }
    }
    
    @Override
    public synchronized void eraseUnit(final String unit) throws IOException
    {
        log(CustomLevel.INTERNAL, "WrapperStorage#eraseUnit("
                                + "\"" + unit + "\")");
        
        leading.eraseUnit(unit);
        
        walkMirrors(new UnitWalker()
        {
            @Override
            public void walk(Storage storage, String unit) throws IOException
            {
                storage.eraseUnit(unit);
            }
        }, unit);
        
        if (cacheType == CacheType.PRELOADED)
        {
            if (preloadedCache.containsKey(unit))
            {
                preloadedCache.get(unit).clear();
            }
        }
        
        for (StorageObserver o : obs)
        {
            o.afterEraseUnit(unit);
        }
    }
    
    @Override
    public synchronized void removeUnit(final String unit) throws IOException
    {
        log(CustomLevel.INTERNAL, "WrapperStorage#removeUnit("
                                + "\"" + unit + "\")");
        
        leading.removeUnit(unit);
        
        walkMirrors(new UnitWalker()
        {
            @Override
            public void walk(Storage storage, String unit) throws IOException
            {
                storage.removeUnit(unit);
            }
        }, unit);
        
        if (cacheType == CacheType.PRELOADED)
        {
            if (preloadedCache.containsKey(unit))
            {
                preloadedCache.remove(unit);
            }
        }
        
        for (StorageObserver o : obs)
        {
            o.afterRemoveUnit(unit);
        }
    }
    
    @Override
    public synchronized void addKey(final String unit,
                                    final String key,
                                    final DataType type)
            throws IOException
    {
        log(CustomLevel.INTERNAL, "WrapperStorage#addKey("
                                + "\"" + unit + "\", "
                                + "\"" + key + "\", "
                                + type + ")");
        
        leading.addKey(unit, key, type);
        
        walkMirrors(new UnitWalker()
        {
            @Override
            public void walk(Storage storage, String unit) throws IOException
            {
                Hashtable<String, DataType> keys = storage.getKeys(unit);
                
                if (!keys.containsKey(key))
                {
                    storage.addKey(unit, key, type);
                }
            }
        }, unit);
        
        if (cacheType == CacheType.PRELOADED)
        {
            if (preloadedCache.containsKey(unit))
            {
                for (Storage.Entry entry : preloadedCache.get(unit))
                {
                    entry.put(key, "");
                }
            }
        }
        
        for (StorageObserver o : obs)
        {
            o.afterAddKey(unit, key, type);
        }
    }
    
    @Override
    public synchronized void addEntry(final String unit,
                                      final Storage.Entry entry)
            throws IOException
    {
        log(CustomLevel.INTERNAL, "WrapperStorage#addEntry("
                                + "\"" + unit + "\", "
                                + entry + ")");
        
        leading.addEntry(unit, entry);
        
        walkMirrors(new UnitWalker()
        {
            @Override
            public void walk(Storage storage, String unit) throws IOException
            {
                storage.addEntry(unit, entry);
            }
        }, unit);
        
        if (cacheType == CacheType.PRELOADED)
        {
            if (preloadedCache.containsKey(unit))
            {
                preloadedCache.get(unit).add(entry.copy());
            }
        }
        
        for (StorageObserver o : obs)
        {
            o.afterAddEntry(unit, entry);
        }
    }
    
    @Override
    public synchronized void updateEntries(final String unit,
                                           final Storage.Entry entrySubset,
                                           final Selector selector)
            throws IOException
    {
        log(CustomLevel.INTERNAL, "WrapperStorage#updateEntries("
                                + "\"" + unit + "\", "
                                + entrySubset + ", "
                                + SqlUtils.translateSelector(selector, "`", "'") + ")");
        
        leading.updateEntries(unit, entrySubset, selector);
        
        walkMirrors(new UnitWalker()
        {
            @Override
            public void walk(Storage storage, String unit) throws IOException
            {
                storage.updateEntries(unit, entrySubset, selector);
            }
        }, unit);
        
        if (cacheType == CacheType.PRELOADED)
        {
            if (preloadedCache.containsKey(unit))
            {
                for (Storage.Entry entry : preloadedCache.get(unit))
                {
                    if (SqlUtils.resolveSelector(selector, entry))
                    {
                        for (Storage.Entry.Datum datum : entrySubset)
                        {
                            entry.put(datum.getKey(), datum.getValue());
                        }
                    }
                }
            }
        }
        
        for (StorageObserver o : obs)
        {
            o.afterUpdateEntries(unit, entrySubset, selector);
        }
    }
    
    @Override
    public synchronized void removeEntries(final String unit,
                                           final Selector selector)
            throws IOException
    {
        log(CustomLevel.INTERNAL, "WrapperStorage#removeEntries("
                                + "\"" + unit + "\", "
                                + SqlUtils.translateSelector(selector, "`", "'") + ")");
        
        leading.removeEntries(unit, selector);
        
        walkMirrors(new UnitWalker()
        {
            @Override
            public void walk(Storage storage, String unit) throws IOException
            {
                storage.removeEntries(unit, selector);
            }
        }, unit);
        
        if (cacheType == CacheType.PRELOADED)
        {
            if (preloadedCache.containsKey(unit))
            {
                Iterator<Storage.Entry> it = preloadedCache.get(unit).iterator();
                
                while (it.hasNext())
                {
                    Storage.Entry entry = it.next();
                    
                    if (SqlUtils.resolveSelector(selector, entry))
                    {
                        it.remove();
                    }
                }
            }
        }
        
        for (StorageObserver o : obs)
        {
            o.afterRemoveEntries(unit, selector);
        }
    }
    
    @Override
    public synchronized void executeBatch() throws IOException
    {
        leading.executeBatch();
        
        executorService.submit(new Runnable()
        {
            @Override
            public void run()
            {
                for (Storage mirror : mirrors.keySet())
                {
                    try
                    {
                        mirror.executeBatch();
                    }
                    catch (IOException ex)
                    {
                        log(Level.WARNING, ex);
                    }
                }
            }
        });
    }
    
    @Override
    public synchronized void clearBatch() throws IOException
    {
        leading.clearBatch();
        
        for (Storage mirror : mirrors.keySet())
        {
            mirror.clearBatch();
        }
    }
    
    @Override
    public synchronized void setAutobatchEnabled(boolean status)
    {
        super.setAutobatchEnabled(status);
        leading.setAutobatchEnabled(status);
        
        for (Storage mirror : mirrors.keySet())
        {
            mirror.setAutobatchEnabled(status);
        }
    }
    
    public void mirrorStorage(Storage storage,
                              Hashtable<String, String> unitMappings)
    {
        if (storage == null || unitMappings == null)
            throw new IllegalArgumentException();
        
        if (!mirrors.containsKey(storage))
        {
            mirrors.put(storage, unitMappings);
        }
    }
    
    public void mirrorStorage(Storage storage)
    {
        mirrorStorage(storage, new Hashtable<String, String>());
    }
    
    public void unmirrorStorage(Storage o)
    {
        mirrors.remove(o);
    }
    
    public void unmirrorAll()
    {
        mirrors.clear();
    }
    
    public void addObserver(StorageObserver o)
    {
        if (o == null)
            throw new IllegalArgumentException();
        
        if (!obs.contains(o))
        {
            obs.addElement(o);
        }
    }
    
    public void deleteObserver(StorageObserver o)
    {
        obs.removeElement(o);
    }
    
    public void deleteObservers()
    {
        obs.removeAllElements();
    }
    
    public int countObservers()
    {
        return obs.size();
    }
    
    public Storage getLeadingStorage()
    {
        return leading;
    }
    
    private void walkMirrors(final UnitWalker walker, final String unit)
    {
        executorService.submit(new Runnable()
        {
            @Override
            public void run()
            {
                for (Map.Entry<Storage, Hashtable<String, String>> e : mirrors.entrySet())
                {
                    String unitMapping = e.getValue().get(unit);
                    
                    if (unitMapping == null)
                    {
                        unitMapping = unit;
                    }
                    
                    try
                    {
                        walker.walk(e.getKey(), unitMapping);
                    }
                    catch (IOException ex)
                    {
                        log(Level.WARNING, ex);
                    }
                }
            }
        });
    }
    
    private List<Storage.Entry> copyEntries(List<Storage.Entry> entries,
                                            List<String> keys,
                                            Selector selector)
    {
        List<Storage.Entry> copiedEntries = new LinkedList<>();
        
        for (Storage.Entry entry : entries)
        {
            if (SqlUtils.resolveSelector(selector, entry))
            {
                Storage.Entry.Builder copiedEntryBuilder = new Storage.Entry.Builder();
                
                for (Storage.Entry.Datum datum : entry)
                {
                    if (keys == null || keys.contains(datum.getKey()))
                    {
                        copiedEntryBuilder.put(datum.getKey(), datum.getValue());
                    }
                }
                
                copiedEntries.add(copiedEntryBuilder.build());
            }
        }
        
        return copiedEntries;
    }
    
    private void log(Level level, String message)
    {
        LogItCore logItCore = LogItCore.getInstance();
        
        if (logItCore != null)
        {
            logItCore.log(level, message);
        }
    }
    
    private void log(Level level, Throwable throwable)
    {
        LogItCore logItCore = LogItCore.getInstance();
        
        if (logItCore != null)
        {
            logItCore.log(level, throwable);
        }
    }
    
    public static final class Builder
    {
        public WrapperStorage build()
        {
            return new WrapperStorage(leading, cacheType);
        }
        
        public Builder leading(Storage leading)
        {
            if (leading == null)
                throw new IllegalArgumentException();
            
            this.leading = leading;
            
            return this;
        }
        
        public Builder cacheType(CacheType cacheType)
        {
            if (cacheType == null)
                throw new IllegalArgumentException();
            
            this.cacheType = cacheType;
            
            return this;
        }
        
        private Storage leading;
        private CacheType cacheType;
    }
    
    private static interface UnitWalker
    {
        public void walk(Storage storage, String unit) throws IOException;
    }
    
    private final ExecutorService executorService;
    private final Storage leading;
    private final CacheType cacheType;
    
    private final Hashtable<Storage, Hashtable<String, String>> mirrors = new Hashtable<>();
    private final Vector<StorageObserver> obs = new Vector<>();
    
    private Hashtable<String, List<Storage.Entry>> preloadedCache;
}
