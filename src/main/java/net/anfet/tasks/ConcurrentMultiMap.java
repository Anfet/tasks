package net.anfet.tasks;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Thread safe map for mapping lists to any object
 */
public final class ConcurrentMultiMap<T, V> implements Serializable {

	private final Map<T, List<V>> map;

	public ConcurrentMultiMap() {
		map = new HashMap<T, List<V>>();
	}

	public Set<T> keys() {
		synchronized (map) {
			return new HashSet<T>(map.keySet());
		}
	}

	public void remove(T key) {
		synchronized (map) {
			map.remove(key);
		}
	}

	public void clear(T key) {
		synchronized (map) {
			map.put(key, new LinkedList<V>());
		}
	}

	/**
	 * removes value element from key set
	 * @param key
	 * @param value
	 */
	public boolean remove(T key, V value) {
		if (key == null || value == null) {
			throw new NullPointerException("Key or value is null");
		}

		synchronized (map) {
			List<V> list = map.get(key);
			if (list != null) {
				return list.remove(value);
			}
		}

		return false;
	}

	/**
	 * adds {@link V} to collection of {@link T}
	 * @param key   key
	 * @param value adding value
	 */
	public V add(T key, V value) {
		if (key == null || value == null) {
			throw new NullPointerException("Key or value is null");
		}

		synchronized (map) {
			List<V> list = map.get(key);
			if (list == null) {
				map.put(key, (list = new LinkedList<V>()));
			}

			list.add(value);
		}

		return value;
	}

	/**
	 * @param key key
	 * @return defensive copy of the internal list
	 */
	public List<V> get(T key) {
		List<V> list;
		synchronized (map) {
			if (key == null || ((list = map.get(key)) == null)) {
				return new LinkedList<V>();
			}
		}
		return new LinkedList<V>(list);
	}

	/**
	 * clears the map
	 */
	public void clear() {
		synchronized (map) {
			map.clear();
		}
	}

	public boolean contains(T key) {
		synchronized (map) {
			return map.containsKey(key);
		}
	}
}
