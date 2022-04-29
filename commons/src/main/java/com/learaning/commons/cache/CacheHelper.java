package com.learaning.commons.cache;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public abstract class CacheHelper {
    public String stringGet(String key) {
        return null;
    }

    public Object objectGet(String key) {
        return null;
    }

    public Boolean setIfAbsent(String key, String value) {
        return false;
    }

    public Long increment(String key) {
        return 0L;
    }

    public void expire(String key, TimeUnit timeUnit, Long timeout) {
    }

    public Long increment(String key, Long step) {
        return 0L;
    }

    public boolean exist(String key) {
        return false;
    }

    public Set<String> keys(String pattern) {
        return new HashSet();
    }

    public void stringSet(String key, String value) {
    }

    public void objectSet(String key, Object object) {
    }

    public void stringSetExpire(String key, String value, long time, TimeUnit timeUnit) {
    }

    public <T> void setObject(String key, T value, Long timeToLive, TimeUnit timeUnit) {
    }

    public String regKey(String key) {
        return key.trim();
    }

    public void stringSetExpire(String key, String value, long seconds) {
    }

    public Map<String, String> hashGet(String key) {
        return new HashMap();
    }

    public String hashGetString(String key, String hashKey) {
        return null;
    }

    public void hashDel(String key, String hashKey) {
    }

    public void hashBatchDel(String key, Set<String> hashKeys) {
    }

    public boolean hashExist(String key, String hashKey) {
        return false;
    }

    public boolean hashAnyExist(String key, String[] hashKeys) {
        return false;
    }

    public void hashSet(String key, String hashKey, String hashValue) {
    }

    public void hashSet(String key, Map<String, String> hash) {
    }

    public boolean delete(String key) {
        return false;
    }

    public boolean delete(List<String> keys) {
        return false;
    }

    public Long setAdd(String key, String[] values) {
        return 0L;
    }

    public Long setAdd(String key, String[] values, boolean clear) {
        return 0L;
    }

    public Set<String> setMembers(String key) {
        return new HashSet();
    }

    public Boolean setExist(String key, String value) {
        return false;
    }
}
