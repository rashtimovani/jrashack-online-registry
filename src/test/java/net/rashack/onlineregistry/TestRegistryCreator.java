package net.rashack.onlineregistry;

import static net.rashack.onlineregistry.OnlineRegistry.toKey;
import static net.rashack.onlineregistry.OnlineRegistryCreator.onInstanceForType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class TestRegistryCreator {

	private static HazelcastInstance instance;

	@BeforeClass
	public static void CreateCluster() {
		instance = Hazelcast.newHazelcastInstance();
	}

	private OnlineRegistry<String> registry;

	@Before
	public void SetUp() {
		registry = onInstanceForType(instance, String.class);
	}

	@Test
	public void testNoDuplicateInstances() {
		assertThat(onInstanceForType(instance, String.class), equalTo(registry));
	}

	@Test(expected = IllegalStateException.class)
	public void testWrongRegisty() {
		instance.getUserContext()
				.put(toKey(Long.class), "wrong object");

		onInstanceForType(instance, Long.class);
	}

	@Test(expected = IllegalStateException.class)
	public void testWrongKey() {
		instance.getUserContext()
				.put(toKey(Integer.class), new OnlineRegistry<>(instance, String.class));

		onInstanceForType(instance, Integer.class);
	}
}
