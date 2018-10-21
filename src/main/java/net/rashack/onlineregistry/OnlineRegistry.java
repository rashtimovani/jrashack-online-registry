package net.rashack.onlineregistry;

import java.io.Serializable;
import java.util.Optional;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;

import net.rashack.onlineregistry.command.DisconnectCommand;
import net.rashack.onlineregistry.data.ConnectionMetaData;

public class OnlineRegistry<T extends Serializable> {
	private static final String KEY_PREFIX = "net.rashack.onlineregistry.map_";
	private static final ConnectionMetaData NOT_ONLINE = new ConnectionMetaData("");

	static String toKey(final Class<?> keyType) {
		return KEY_PREFIX + keyType.getName();
	}

	private final HazelcastInstance instance;
	private final Class<T> keyType;

	private final IMap<T, ConnectionMetaData> registryMap;
	private final IExecutorService distributedExecutor;

	OnlineRegistry(final HazelcastInstance instance, final Class<T> keyType) {
		this.instance = instance;
		this.keyType = keyType;

		registryMap = instance.getMap(toKey(this.keyType));
		distributedExecutor = instance.getExecutorService(KEY_PREFIX + "/maintainer");
	}

	private void disconnect(final Member member) {
		if (member.localMember()) {
			final DisconnectCommand command = new DisconnectCommand(toKey(keyType));
			command.setHazelcastInstance(instance);
			command.run();
		} else {
			distributedExecutor.executeOnMember(new DisconnectCommand(toKey(keyType)), member);
		}
	}

	public void forceOnline(final T connectionId) {
		final ConnectionMetaData alreadyOnline = registryMap.put(connectionId, new ConnectionMetaData(getOwnerUuid()));
		if (alreadyOnline != null) {
			getMemberWithUuid(alreadyOnline.getOwner()).ifPresent(this::disconnect);
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

	public boolean isOnline(final T connectionId) {
		return registryMap.containsKey(connectionId);
	}

	public boolean isOnlineHere(final T connectionId) {
		return registryMap.getOrDefault(connectionId, NOT_ONLINE)
				.getOwner()
				.equals(getOwnerUuid());
	}

	@Override
	public String toString() {
		return "Registry for " + keyType.getSimpleName() + " on key " + toKey(keyType);
	}

	public boolean tryToComeOnline(final T connectionId) {
		final ConnectionMetaData metaData = new ConnectionMetaData(getOwnerUuid());
		final ConnectionMetaData alreadyOnline = registryMap.putIfAbsent(connectionId, metaData);
		return alreadyOnline == null;
	}
}
