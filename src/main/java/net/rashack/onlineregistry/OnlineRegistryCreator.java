package net.rashack.onlineregistry;

import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

import com.hazelcast.core.HazelcastInstance;

public final class OnlineRegistryCreator {

	public static <T extends Serializable> OnlineRegistry<T> onInstanceForType(final HazelcastInstance instance,
			final Class<T> keyType) {
		return fetchExisting(instance, keyType).orElse(initializeNew(instance, keyType));
	}

	private static <T extends Serializable> Optional<OnlineRegistry<T>> fetchExisting(final HazelcastInstance instance,
			final Class<T> keyType) {
		final ConcurrentMap<String, Object> contextMap = instance.getUserContext();
		final Object existing = contextMap.get(OnlineRegistry.toKey(keyType));
		return Optional.ofNullable(convert(existing, keyType));
	}

	private static <T extends Serializable> OnlineRegistry<T> initializeNew(final HazelcastInstance instance,
			final Class<T> keyType) {
		final OnlineRegistry<T> registry = new OnlineRegistry<>(instance, keyType);
		final ConcurrentMap<String, Object> contextMap = instance.getUserContext();
		final Object existing = contextMap.putIfAbsent(OnlineRegistry.toKey(keyType), registry);
		return existing != null ? convert(existing, keyType) : registry;
	}

	@SuppressWarnings("unchecked")
	private static <T extends Serializable> OnlineRegistry<T> convert(Object candidate, final Class<T> keyType) {
		if (candidate == null) {
			return null;
		}

		OnlineRegistry<T> registry;
		try {
			registry = (OnlineRegistry<T>) candidate;
		} catch (ClassCastException e) {
			throw new IllegalStateException(
					"Wrong registry found " + candidate + " for keyType " + keyType.getSimpleName(), e);
		}

		if (registry.isKeyType(keyType)) {
			return registry;
		}

		throw new IllegalStateException(
				"Wrong registry found " + candidate + " for keyType " + keyType.getSimpleName());
	}

	private OnlineRegistryCreator() {
	}
}
