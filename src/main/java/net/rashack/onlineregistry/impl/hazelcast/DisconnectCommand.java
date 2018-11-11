package net.rashack.onlineregistry.impl.hazelcast;

import java.io.Serializable;

import net.rashack.onlineregistry.OnlineEntity;
import net.rashack.onlineregistry.listener.WentOfflineListener;

public class DisconnectCommand<T extends Serializable, U extends Serializable> extends HazelcastCommand<T, U> {
	private static final long serialVersionUID = 4314643176778598449L;

	private final OnlineEntity<T, U> whoWentOffline;

	public DisconnectCommand(final Class<T> keyType, final OnlineEntity<T, U> whoWentOffline) {
		super(keyType.getName());
		this.whoWentOffline = whoWentOffline;
	}

	@Override
	public void run() {
		registry.ifPresent(this::triggerWentOfflineListeners);
	}

	private void triggerWentOfflineListeners(final HazelcastOnlineRegistry<T, U> registry) {
		for (final WentOfflineListener<T, U> listener : wentOfflineListeners(registry)) {
			try {
				listener.wentOffline(whoWentOffline);
			} catch (Exception e) {
				getLogger().severe("Failed to execute listener: " + listener, e);
			}
		}
	}
}
