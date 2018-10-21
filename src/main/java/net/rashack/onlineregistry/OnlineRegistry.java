package net.rashack.onlineregistry;

import java.io.Serializable;
import java.util.concurrent.ConcurrentMap;

import com.hazelcast.core.HazelcastInstance;

public class OnlineRegistry<T extends Serializable> {
	private static final String KEY_PREFIX = "net.rashack.onlineregistry.map_";

	static String toKey(final Class<?> keyType) {
		return KEY_PREFIX + keyType.getName();
	}

	private final HazelcastInstance instance;
	private final Class<T> keyType;

	private final ConcurrentMap<T, String> registryMap;

	OnlineRegistry(final HazelcastInstance instance, final Class<T> keyType) {
		this.instance = instance;
		this.keyType = keyType;

		registryMap = instance.getMap(toKey(this.keyType));
	}

	boolean isKeyType(final Class<?> keyType) {
		return this.keyType.equals(keyType);
	}

	public boolean isOnline(final T connectionId) {
		return registryMap.containsKey(connectionId);
	}

	@Override
	public String toString() {
		return "Registry for " + keyType.getSimpleName() + " on key " + toKey(keyType);
	}

	public boolean tryToComeOnline(final T connectionId) {
		final String alreadyOnline = registryMap.putIfAbsent(connectionId, instance.getName());
		return alreadyOnline == null;
	}
}
