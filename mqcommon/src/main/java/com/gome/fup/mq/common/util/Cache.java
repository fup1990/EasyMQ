package com.gome.fup.mq.common.util;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * 缓存类
 * 单例
 * @author fupeng-ds
 */
public class Cache {

	private LoadingCache<String, Object> loadingCache;
	
	private static Cache cache = new Cache();
	
	private Cache() {
		super();
		loadingCache = CacheBuilder.newBuilder().maximumSize(1000).build(new CacheLoader<String, Object>() {

			@Override
			public Object load(String key) throws Exception {
				return loadingCache.get(key);
			}
		});
	}

	public static Cache getCache() {
		return cache;
	}
	
	public void set(String key, Object value) {
		loadingCache.put(key, value);
	}
	
	public Object get(String key) {
		Object result = null;
		try {
			result = loadingCache.get(key);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return result;
	}

	public boolean hasKey(String key) {
		Set<String> keySet = loadingCache.asMap().keySet();
		if (keySet.contains(key)) {
			return true;
		}
		return false;
	}

}
