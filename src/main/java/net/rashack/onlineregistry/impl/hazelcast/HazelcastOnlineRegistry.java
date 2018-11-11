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

import net.rashack.onlineregistry.OnlineEntity;
import net.rashack.onlineregistry.OnlineRegistry;
import net.rashack.onlineregistry.impl.ConnectionMetaData;
import net.rashack.onlineregistry.listener.WentOfflineListener;

public final class HazelcastOnlineRegistry<T extends Serializable, U extends Serializable>
		implements OnlineRegistry<T, U> {
	private static final String KEY_PREFIX = "net.rashack.onlineregistry.hazelcast.map_";

	public static <T extends Serializable, U extends Serializable> HazelcastOnlineRegistry<T, U> create(
			final HazelcastInstance instance, final Class<T> keyType) {
		requireNonNull(instance, "Hazelcast instance over which registry works must be provided!");
		requireNonNull(keyType, "Key type must be provided");

		final HazelcastContextHandler<T, U> contextHandler = new HazelcastContextHandler<>(instance.getUserContext());
		return contextHandler.getOrCreate(keyType, () -> new HazelcastOnlineRegistry<>(instance, keyType));
	}

	static String toKey(final Class<?> keyType) {
		return KEY_PREFIX + keyType.getName();
	}

	private final Set<WentOfflineListener<T, U>> wentOfflineListeners = newSetFromMap(new ConcurrentHashMap<>());
	final Collection<WentOfflineListener<T, U>> wentOfflineListenersReadOnly = unmodifiableSet(wentOfflineListeners);

	private final HazelcastInstance instance;
	private final Class<T> keyType;

	private final IMap<T, ConnectionMetaData<T, U>> registryMap;
	private final IExecutorService distributedExecutor;

	private HazelcastOnlineRegistry(final HazelcastInstance instance, final Class<T> keyType) {
		this.instance = requireNonNull(instance);
		this.keyType = requireNonNull(keyType);

		final String key = toKey(this.keyType);
		registryMap = instance.getMap(key);
		distributedExecutor = instance.getExecutorService(key + "/maintainer");
	}

	@Override
	public void addWentOfflineListener(final WentOfflineListener<T, U> listener) {
		wentOfflineListeners.add(requireNonNull(listener, "Listener must be provided!"));
	}

	@Override
	public boolean comeOnline(final OnlineEntity<T, U> whoCameOnline) {
		final ConnectionMetaData<T, U> metaData = new ConnectionMetaData<>(getOwnerUuid(), whoCameOnline);
		final ConnectionMetaData<T, U> alreadyOnline = registryMap.putIfAbsent(whoCameOnline.getId(), metaData);
		return alreadyOnline == null;
	}

	private void disconnect(final Member member, final OnlineEntity<T, U> toDisconnect) {
		if (member.localMember()) {
			final DisconnectCommand<T, U> command = new DisconnectCommand<>(keyType, toDisconnect);
			command.setHazelcastInstance(instance);
			command.run();
		} else {
			distributedExecutor.executeOnMember(new DisconnectCommand<>(keyType, toDisconnect), member);
		}
	}

	@Override
	public void forceOnline(final OnlineEntity<T, U> whoCameOnline) {
		final ConnectionMetaData<T, U> metaData = new ConnectionMetaData<>(getOwnerUuid(), whoCameOnline);
		final ConnectionMetaData<T, U> alreadyOnline = registryMap.put(whoCameOnline.getId(), metaData);
		if (alreadyOnline != null) {
			getMemberWithUuid(alreadyOnline.getOwner())
					.ifPresent(member -> disconnect(member, alreadyOnline.getOnlineEntity()));
		}
	}

	Member getLocalMember() {
		return instance.getCluster()
				.getLocalMember();
	}

	Optional<Member> getMemberWithUuid(final String uuid) {
		return instance.getCluster()
				.getMembers()
				.stream()
				.filter(candidate -> uuid.equals(candidate.getUuid()))
				.findFirst();
	}

	String getOwnerUuid() {
		return getLocalMember().getUuid();
	}

	boolean isKeyType(final Class<?> keyType) {
		return this.keyType.equals(keyType);
	}

	@Override
	public boolean isLocallyOnline(final T connectionId) {
		final ConnectionMetaData<T, U> online = registryMap.get(connectionId);
		return online != null && getOwnerUuid().equals(online.getOwner());
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
	public boolean wentOffline(final OnlineEntity<T, U> whoWentOffline) {
		requireNonNull(whoWentOffline, "Online entity which went offline cannot be null!");

		final ConnectionMetaData<T, U> online = registryMap.get(whoWentOffline.getId());
		if (online == null) {
			return false;
		}

		if (getOwnerUuid().equals(online.getOwner()) && whoWentOffline.equals(online.getOnlineEntity())
				&& registryMap.remove(whoWentOffline.getId(), online)) {
			disconnect(getLocalMember(), whoWentOffline);
			return true;
		}

		return false;
	}
}
