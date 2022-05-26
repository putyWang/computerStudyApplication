package com.learning.commons.cache;

import com.google.common.collect.Maps;
import org.springframework.beans.BeansException;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultCacheHelper
        extends CacheHelper
        implements ApplicationContextAware{

    private Cache cache;

    public DefaultCacheHelper() {
        this.cache = new ConcurrentMapCache("concurrentMapCache");
    }

    public DefaultCacheHelper(Cache cache) {
        this.cache = cache;
    }

    @Override
    public String stringGet(String key) {
        Cache.ValueWrapper valueWrapper = this.cache.get(key);
        return valueWrapper != null ? (String)valueWrapper.get() : super.stringGet(key);
    }

    @Override
    public Boolean setIfAbsent(String key, String value) {
        this.cache.putIfAbsent(key, value);
        return true;
    }

    @Override
    public boolean exist(String key) {
        String cacheHoldTime = this.stringGet(key + "_HoldTime");
        if (cacheHoldTime != null && Long.parseLong(cacheHoldTime) > 0L && Long.parseLong(cacheHoldTime) < System.currentTimeMillis()) {
            this.delete(key + "_HoldTime");
            this.delete(key);
            return false;
        } else {
            return this.cache.get(key) != null;
        }
    }

    @Override
    public void stringSet(String key, String value) {
        this.cache.put(key, value);
    }

    @Override
    public String regKey(String key) {
        return super.regKey(key);
    }

    @Override
    public void stringSetExpire(String key, String value, long seconds) {
        this.stringSet(key, value);
        if (seconds > 0L) {
            this.stringSet(key + "_HoldTime", String.valueOf(System.currentTimeMillis() + seconds * 1000L));
        }

    }

    @Override
    public Map<String, String> hashGet(String key) {
        Cache.ValueWrapper t = this.cache.get(key);
        return (Map)(t != null ? (Map)t.get() : Maps.newHashMap());
    }

    @Override
    public String hashGetString(String key, String hashKey) {
        Map<String, String> stringStringMap = this.hashGet(key);
        return (String)stringStringMap.get(hashKey);
    }

    @Override
    public void hashDel(String key, String hashKey) {
        Map<String, String> stringStringMap = this.hashGet(key);
        stringStringMap.remove(hashKey);
    }

    @Override
    public void hashBatchDel(String key, Set<String> hashKeys) {
        Map<String, String> stringStringMap = this.hashGet(key);
        hashKeys.forEach(stringStringMap::remove);
    }

    @Override
    public boolean hashExist(String key, String hashKey) {
        if (this.exist(key)) {
            Map<String, String> map = this.hashGet(key);
            return map.containsKey(hashKey);
        } else {
            return false;
        }
    }

    @Override
    public boolean hashAnyExist(String key, String[] hashKeys) {
        return super.hashAnyExist(key, hashKeys);
    }

    @Override
    public void hashSet(String key, String hashKey, String hashValue) {
        Object map;
        if (this.exist(key)) {
            map = this.hashGet(key);
        } else {
            map = new HashMap();
        }

        ((Map)map).put(hashKey, hashValue);
        this.hashSet(key, (Map)map);
    }

    @Override
    public void hashSet(String key, Map<String, String> hash) {
        this.cache.put(key, hash);
    }

    @Override
    public boolean delete(String key) {
        if (this.exist(key)) {
            this.cache.evict(key);
        }

        return true;
    }

    @Override
    public boolean delete(List<String> keys) {
        keys.forEach(this::delete);
        return true;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.cache = (Cache)applicationContext.getBean("concurrentMapCache");
    }
}
