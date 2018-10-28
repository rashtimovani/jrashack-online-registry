package net.rashack.onlineregistry.impl.hazelcast;

import static java.util.Collections.newSetFromMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;

import net.rashack.onlineregistry.OnlineRegistry;
import net.rashack.onlineregistry.impl.ConnectionMetaData;
import net.rashack.onlineregistry.listener.WentOfflineListener;

public final class HazelcastOnlineRegistry<T extends Serializable> implements OnlineRegistry<T> {
	private static final String KEY_PREFIX = "net.rashack.onlineregistry.hazelcast.map_";
	private static final ConnectionMetaData NOT_ONLINE = new ConnectionMetaData("");

	public static <U extends Serializable> HazelcastOnlineRegistry<U> create(final HazelcastInstance instance,
			final Class<U> keyType) {
		requireNonNull(instance, "Hazelcast instance over which registry works must be provided!");
		requireNonNull(keyType, "Key type must be provided");

		final HazelcastContextHandler<U> contextHandler = new HazelcastContextHandler<>(instance.getUserContext());
		return contextHandler.getOrCreate(keyType, () -> new HazelcastOnlineRegistry<>(instance, keyType));
	}

	static String toKey(final Class<?> keyType) {
		return KEY_PREFIX + keyType.getName();
	}

	private final Set<WentOfflineListener<T>> wentOfflineListeners = newSetFromMap(new ConcurrentHashMap<>());
	final Collection<WentOfflineListener<T>> wentOfflineListenersReadOnly = unmodifiableSet(wentOfflineListeners);

	private final HazelcastInstance instance;
	private final Class<T> keyType;

	private final IMap<T, ConnectionMetaData> registryMap;
	private final IExecutorService distributedExecutor;

	private HazelcastOnlineRegistry(final HazelcastInstance instance, final Class<T> keyType) {
		this.instance = requireNonNull(instance);
		this.keyType = requireNonNull(keyType);

		registryMap = instance.getMap(toKey(this.keyType));
		distributedExecutor = instance.getExecutorService(KEY_PREFIX + "/maintainer");
	}

	@Override
	public void addWentOfflineListener(final WentOfflineListener<T> listener) {
		wentOfflineListeners.add(requireNonNull(listener, "Listener must be provided!"));
	}

	@Override
	public boolean comeOnline(final T connectionId) {
		final ConnectionMetaData metaData = new ConnectionMetaData(getOwnerUuid());
		final ConnectionMetaData alreadyOnline = registryMap.putIfAbsent(connectionId, metaData);
		return alreadyOnline == null;
	}

	private void disconnect(final Member member, final T connectionId) {
		if (member.localMember()) {
			final DisconnectCommand<T> command = new DisconnectCommand<>(keyType, connectionId);
			command.setHazelcastInstance(instance);
			command.run();
		} else {
			distributedExecutor.executeOnMember(new DisconnectCommand<>(keyType, connectionId), member);
		}
	}

	@Override
	public void forceOnline(final T connectionId) {
		final ConnectionMetaData alreadyOnline = registryMap.put(connectionId, new ConnectionMetaData(getOwnerUuid()));
		if (alreadyOnline != null) {
			getMemberWithUuid(alreadyOnline.getOwner()).ifPresent(member -> disconnect(member, connectionId));
		}
	}

	Optional<Member> getMemberWithUuid(final String uuid) {
		return instance.getCluster()
				.getMembers()
				.stream()
				.filter(candidate -> uuid.equals(candidate.getUuid()))
				.findFirst();
	}

	String getOwnerUuid() {
		return instance.getCluster()
				.getLocalMember()
				.getUuid();
	}

	boolean isKeyType(final Class<?> keyType) {
		return this.keyType.equals(keyType);
	}

	@Override
	public boolean isLocallyOnline(final T connectionId) {
		return registryMap.getOrDefault(connectionId, NOT_ONLINE)
				.getOwner()
				.equals(getOwnerUuid());
	}

	@Override
	public boolean isOnline(final T connectionId) {
		return registryMap.containsKey(connectionId);
	}

	@Override
	public String toString() {
		return "Registry for " + keyType.getSimpleName() + " on key " + toKey(keyType);
	}

	@Override
	public boolean wentOffline(final T connectionId) {
		final ConnectionMetaData online = registryMap.getOrDefault(connectionId, NOT_ONLINE);
		if (online.getOwner()
				.equals(getOwnerUuid()) && registryMap.remove(connectionId, online)) {
			disconnect(instance.getCluster()
					.getLocalMember(), connectionId);
			return true;
		}

		return false;
	}
}
