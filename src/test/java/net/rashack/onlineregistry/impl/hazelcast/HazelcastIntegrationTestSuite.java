package net.rashack.onlineregistry.impl.hazelcast;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

@RunWith(Suite.class)
@SuiteClasses({ TestHazelcastCluster.class })
public class HazelcastIntegrationTestSuite {

	private static volatile HazelcastInstance localInstance;
	private static volatile HazelcastInstance remoteInstance;

	public static HazelcastInstance getLocalInstance() {
		return localInstance;
	}

	public static HazelcastInstance getRemoteInstance() {
		return remoteInstance;
	}

	@BeforeClass
	public synchronized static void setUpInstances() {
		if (localInstance == null) {
			localInstance = Hazelcast.newHazelcastInstance();
		}
		if (remoteInstance == null) {
			remoteInstance = Hazelcast.newHazelcastInstance();
		}
	}
}
