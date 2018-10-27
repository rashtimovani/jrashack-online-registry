package net.rashack.onlineregistry.impl.hazelcast;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.logging.ILogger;

import net.rashack.onlineregistry.listener.WentOfflineListener;

public abstract class HazelcastCommand<T extends Serializable>
		implements HazelcastInstanceAware, Runnable, Serializable {
	private static final long serialVersionUID = -1555356959538490543L;

	private final String keyType;

	private transient HazelcastInstance instance;
	protected transient Optional<HazelcastOnlineRegistry<T>> registry;

	protected HazelcastCommand(final String keyType) {
		this.keyType = keyType;
	}

	protected final ILogger getLogger() {
		return instance.getLoggingService()
				.getLogger(getClass());
	}

	@Override
	public final void setHazelcastInstance(final HazelcastInstance instance) {
		this.instance = instance;

		registry = new HazelcastContextHandler<T>(instance.getUserContext()).getExisting(keyType);
	}

	protected Collection<WentOfflineListener<T>> wentOfflineListeners(final HazelcastOnlineRegistry<T> registry) {
		return registry.wentOfflineListenersReadOnly;
	}
}
