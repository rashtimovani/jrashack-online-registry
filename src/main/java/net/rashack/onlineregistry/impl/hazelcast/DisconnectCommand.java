package net.rashack.onlineregistry.impl.hazelcast;

import java.io.Serializable;

import net.rashack.onlineregistry.impl.hazelcast.HazelcastCommand;
import net.rashack.onlineregistry.impl.hazelcast.HazelcastOnlineRegistry;
import net.rashack.onlineregistry.listener.WentOfflineListener;

public class DisconnectCommand<T extends Serializable> extends HazelcastCommand<T> {
	private static final long serialVersionUID = 4314643176778598449L;

	private final T connectionId;

	public DisconnectCommand(final Class<T> keyType, final T connectionId) {
		super(keyType.getName());
		this.connectionId = connectionId;
	}

	@Override
	public void run() {
		registry.ifPresent(this::triggerWentOfflineListeners);
	}

	private void triggerWentOfflineListeners(final HazelcastOnlineRegistry<T> registry) {
		for (final WentOfflineListener<T> listener : wentOfflineListeners(registry)) {
			try {
				listener.wentOffline(connectionId);
			} catch (Exception e) {
				getLogger().severe("Failed to execute listener: " + listener, e);
			}
		}
	}
}
