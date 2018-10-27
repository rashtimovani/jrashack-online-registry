package net.rashack.onlineregistry;

import java.io.Serializable;

import com.hazelcast.core.HazelcastInstance;

import net.rashack.onlineregistry.impl.hazelcast.HazelcastOnlineRegistry;
import net.rashack.onlineregistry.listener.WentOfflineListener;

public interface OnlineRegistry<T extends Serializable> {

	public static <T extends Serializable> OnlineRegistry<T> create(final HazelcastInstance instance,
			final Class<T> keyType) {
		return HazelcastOnlineRegistry.create(instance, keyType);
	}

	void addWentOfflineListener(WentOfflineListener<T> listener);

	boolean comeOnline(final T connectionId);

	void forceOnline(T connectionId);

	boolean isLocallyOnline(T connectionId);

	boolean isOnline(final T connectionId);

	void wentOffline(T connectionId);
}
