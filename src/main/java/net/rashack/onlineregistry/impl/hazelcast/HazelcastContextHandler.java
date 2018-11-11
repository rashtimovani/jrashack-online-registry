package net.rashack.onlineregistry.impl.hazelcast;

import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

class HazelcastContextHandler<T extends Serializable, U extends Serializable> {

	final ConcurrentMap<String, Object> userMap;

	HazelcastContextHandler(final ConcurrentMap<String, Object> userMap) {
		this.userMap = userMap;
	}

	@SuppressWarnings("unchecked")
	private HazelcastOnlineRegistry<T, U> convert(final Object candidate, final Class<T> keyType) {
		if (candidate == null) {
			return null;
		}

		HazelcastOnlineRegistry<T, U> registry;
		try {
			registry = (HazelcastOnlineRegistry<T, U>) candidate;
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

	private Optional<HazelcastOnlineRegistry<T, U>> getExisting(final Class<T> keyType) {
		final Object existing = userMap.get(HazelcastOnlineRegistry.toKey(keyType));
		return Optional.ofNullable(convert(existing, keyType));
	}

	public Optional<HazelcastOnlineRegistry<T, U>> getExisting(final String keyType) {
		return getExisting(getKeyClass(keyType));
	}

	@SuppressWarnings("unchecked")
	private Class<T> getKeyClass(final String keyType) {
		try {
			return (Class<T>) Class.forName(keyType);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(
					"Unknown key class provided " + keyType + ", cannot get registry for it!");
		}
	}

	public HazelcastOnlineRegistry<T, U> getOrCreate(final Class<T> keyType,
			final Supplier<HazelcastOnlineRegistry<T, U>> creator) {
		return getExisting(keyType).orElseGet(() -> initialize(creator.get(), keyType));
	}

	private HazelcastOnlineRegistry<T, U> initialize(final HazelcastOnlineRegistry<T, U> candidate,
			final Class<T> keyType) {
		final Object existing = userMap.putIfAbsent(HazelcastOnlineRegistry.toKey(keyType), candidate);
		return existing != null ? convert(existing, keyType) : candidate;
	}
}
