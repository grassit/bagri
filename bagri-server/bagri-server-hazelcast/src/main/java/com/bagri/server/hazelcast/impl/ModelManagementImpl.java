package com.bagri.server.hazelcast.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import com.bagri.client.hazelcast.impl.IdGeneratorImpl;
import com.bagri.core.model.Path;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.core.server.api.impl.ModelManagementBase;
import com.bagri.support.idgen.IdGenerator;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.IAtomicLong;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapEvent;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;
import com.hazelcast.map.listener.MapClearedListener;
import com.hazelcast.map.listener.MapEvictedListener;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;
import com.hazelcast.query.impl.predicates.RegexPredicate;

public class ModelManagementImpl extends ModelManagementBase implements ModelManagement { 

	protected IMap<String, Path> pathCache;
	private IdGenerator<Long> pathGen;
	private ConcurrentMap<Integer, Path> cachePath = new ConcurrentHashMap<>();
	private ConcurrentMap<String, Set<Path>> cacheType = new ConcurrentHashMap<>();
	
	public ModelManagementImpl() {
		super();
	}
	
	protected Map<String, Path> getPathCache() {
		return pathCache;
	}
	
	protected IdGenerator<Long> getPathGen() {
		return pathGen;
	}
	
	public void setPathCache(IMap<String, Path> pathCache) {
		this.pathCache = pathCache;
		this.pathCache.addEntryListener(new PathCacheListener(), true);
	}
	
	public void setPathGen(IAtomicLong pathGen) {
		this.pathGen = new IdGeneratorImpl(pathGen);
	}
	
	private Path getPathInternal(int pathId) {
		Predicate<String, Path> f = Predicates.equal("pathId", pathId);
		Collection<Path> entries = pathCache.values(f);
		if (entries.isEmpty()) {
			return null;
		}
		// check size > 1 ??
		return entries.iterator().next();
	}
	
	@Override
	public Path getPath(int pathId) {
		Path result = cachePath.get(pathId);
		if (result == null) {
			result = getPathInternal(pathId);
			if (result != null) {
				cachePath.putIfAbsent(pathId, result);
			}
		}
		return result;
	}
	
	private Set<Path> getTypePathsInternal(String root) {
		Predicate<String, Path> f = Predicates.equal("root", root);
		Collection<Path> entries = pathCache.values(f);
		if (entries.isEmpty()) {
			return Collections.emptySet();
		}
		// check size > 1 ??
		Set<Path> result = new HashSet<>(entries);
		//Collections.sort(result);
		//if (logger.isTraceEnabled()) {
		//	logger.trace("getTypePath; returning {} for type {}", result, typeId);
		//}
		return result;
	}
	
	@Override
	public Collection<Path> getTypePaths(String root) {
		Collection<Path> result = cacheType.get(root);
		if (result == null) {
		    result = getTypePathsInternal(root);
			cacheType.putIfAbsent(root, (Set<Path>) result);
		}
		// to prevent concurrent access to results..
		return new ArrayList<>(result);
	}
	
	@Override
	protected Set<Map.Entry<String, Path>> getTypedPathEntries(String root) {
		Predicate<String, Path> f = Predicates.equal("root",  root);
		Set<Map.Entry<String, Path>> entries = pathCache.entrySet(f);
		return entries;
	}

	@Override
	protected Set<Map.Entry<String, Path>> getTypedPathWithRegex(String regex, String root) {
		regex = regex.replaceAll("\\{", Matcher.quoteReplacement("\\{"));
		regex = regex.replaceAll("\\}", Matcher.quoteReplacement("\\}"));
		Predicate<String, Path> filter = new RegexPredicate("path", regex);
		if (root != null) {
			filter = Predicates.and(filter, Predicates.equal("root", root));
		}
		Set<Map.Entry<String, Path>> entries = pathCache.entrySet(filter);
		return entries;
	}

	//@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected <K> boolean lock(Map<K, ?> cache, K key) {
		try {
			return ((IMap) cache).tryLock(key, timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException ex) {
			logger.error("Interrupted on lock", ex);
			return false;
		}
	}

	//@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected <K> void unlock(Map<K, ?> cache, K key) {
		((IMap) cache).unlock(key);
	}

	@Override
	protected <K, V> V putIfAbsent(Map<K, V> map, K key, V value) {
		IMap<K, V> cache = (IMap<K, V>) map;
		V val2 = cache.putIfAbsent(key, value);
		//V val2 = cache.put(key, value);
		if (val2 == null) {
			return value;
		}
		logger.debug("putIfAbsent; got collision on cache: {}, key: {}; returning: {}", cache.getName(), key, val2);
		return val2;
	}

	@Override
	public void updatePath(Path path) {
		String pathKey = getPathKey(path.getRoot(), path.getPath());
		((IMap<String, Path>) getPathCache()).set(pathKey, path);
	}
	

	private class PathCacheListener implements MapClearedListener, MapEvictedListener,
		EntryAddedListener<String, Path>, EntryRemovedListener<String, Path>, EntryUpdatedListener<String, Path> {
	
		@Override
		public void mapEvicted(MapEvent event) {
			// don't think we have to clear everything in this case
		}
	
		@Override
		public void mapCleared(MapEvent event) {
			cachePath.clear();
			cacheType.clear();
		}
		
		@Override
		public void entryUpdated(EntryEvent<String, Path> event) {
			Path path = event.getValue();
			cachePath.put(path.getPathId(), path);
			Set<Path> paths = cacheType.get(path.getRoot());
			if (paths == null) {
				paths = new HashSet<>();
				Set<Path> paths2 = cacheType.putIfAbsent(path.getRoot(), paths);
				if (paths2 != null) {
					paths = paths2;
				}
			}
			paths.add(path);
		}
	
		@Override
		public void entryRemoved(EntryEvent<String, Path> event) {
			Path path = event.getValue();
			cachePath.remove(path.getPathId());
			Set<Path> paths = cacheType.get(path.getRoot());
			if (paths != null) {
				paths.remove(path);
			}
		}
	
		@Override
		public void entryAdded(EntryEvent<String, Path> event) {
			Path path = event.getValue();
			cachePath.putIfAbsent(path.getPathId(), path);
			Set<Path> paths = cacheType.get(path.getRoot());
			if (paths == null) {
				paths = new HashSet<>();
				Set<Path> paths2 = cacheType.putIfAbsent(path.getRoot(), paths);
				if (paths2 != null) {
					paths = paths2;
				}
			}
			paths.add(path);
		}
	
	}
	
	
}
