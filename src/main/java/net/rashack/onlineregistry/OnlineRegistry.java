package net.rashack.onlineregistry;

import java.io.Serializable;

import com.hazelcast.core.HazelcastInstance;

import net.rashack.onlineregistry.impl.hazelcast.HazelcastOnlineRegistry;
import net.rashack.onlineregistry.listener.WentOfflineListener;

public interface OnlineRegistry<T extends Serializable, U extends Serializable> {

	public static <T extends Serializable, U extends Serializable> OnlineRegistry<T, U> create(final HazelcastInstance instance,
			final Class<T> keyType) {
		return HazelcastOnlineRegistry.create(instance, keyType);
	}

	void addWentOfflineListener(WentOfflineListener<T, U> listener);

	boolean comeOnline(OnlineEntity<T, U> whoCameOnline);

	void forceOnline(OnlineEntity<T, U> whoCameOnline);

	boolean isLocallyOnline(T whoCameOnline);

	boolean isOnline(final T onlineEntityId);

	boolean wentOffline(OnlineEntity<T, U> whoWentOffline);
}
