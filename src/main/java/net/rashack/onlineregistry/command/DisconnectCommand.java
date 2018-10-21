package net.rashack.onlineregistry.command;

import java.io.Serializable;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;

public class DisconnectCommand implements HazelcastInstanceAware, Runnable, Serializable {

	private static final long serialVersionUID = 4314643176778598449L;
	
	private final String key;
	
	public DisconnectCommand(final String key) {
		this.key = key;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
		// TODO Auto-generated method stub
		
	}
}
