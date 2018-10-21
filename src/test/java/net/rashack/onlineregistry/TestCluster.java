package net.rashack.onlineregistry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class TestCluster {

	private static HazelcastInstance firstInstance;
	private static HazelcastInstance secondInstance;

	@BeforeClass
	public static void CreateCluster() {
		firstInstance = Hazelcast.newHazelcastInstance();
		secondInstance = Hazelcast.newHazelcastInstance();
	}

	private OnlineRegistry<String> first;
	private OnlineRegistry<String> second;

	@Before
	public void SetUp() {
		first = OnlineRegistryCreator.onInstanceForType(firstInstance, String.class);
		second = OnlineRegistryCreator.onInstanceForType(secondInstance, String.class);
	}

	@Test
	public void testCommingOnline() {
		assertThat(first.tryToComeOnline("connection-01"), equalTo(true));
		assertThat(second.isOnline("connection-01"), equalTo(true));
	}
	
	@Test
	public void testCommingOnlineAlreadyPresent() {
		assertThat(first.tryToComeOnline("connection-02"), equalTo(true));
		assertThat(second.tryToComeOnline("connection-02"), equalTo(false));
	}
}
