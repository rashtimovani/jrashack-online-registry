package net.rashack.onlineregistry.impl.hazelcast;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import net.rashack.onlineregistry.OnlineRegistry;
import net.rashack.onlineregistry.listener.WentOfflineListener;

public class TestHazelcastCluster {

	private OnlineRegistry<String> local;
	private OnlineRegistry<String> remote;

	private WentOfflineListener<String> localWentOfflineListener;
	private WentOfflineListener<String> remoteWentOfflineListener;

	@SuppressWarnings("unchecked")
	@Before
	public void SetUp() {
		HazelcastIntegrationTestSuite.setUpInstances();

		local = OnlineRegistry.create(HazelcastIntegrationTestSuite.getLocalInstance(), String.class);
		remote = OnlineRegistry.create(HazelcastIntegrationTestSuite.getRemoteInstance(), String.class);

		localWentOfflineListener = spy(WentOfflineListener.class);
		local.addWentOfflineListener(localWentOfflineListener);
		remoteWentOfflineListener = spy(WentOfflineListener.class);
		remote.addWentOfflineListener(remoteWentOfflineListener);
	}

	@Test
	public void testCommingOnline() {
		assertThat(local.comeOnline("connection-01"), equalTo(true));
		assertThat(local.isLocallyOnline("connection-01"), equalTo(true));
		assertThat(remote.isOnline("connection-01"), equalTo(true));
		assertThat(remote.isLocallyOnline("connection-01"), equalTo(false));
	}

	@Test
	public void testCommingOnlineAlreadyPresent() {
		assertThat(local.comeOnline("connection-02"), equalTo(true));
		assertThat(remote.comeOnline("connection-02"), equalTo(false));
	}

	@Test
	public void testCommingOnlineForce() {
		local.comeOnline("connection-03");

		remote.forceOnline("connection-03");

		assertThat(local.isOnline("connection-03"), equalTo(true));
		assertThat(local.isLocallyOnline("connection-03"), equalTo(false));
		verify(localWentOfflineListener, times(1)).wentOffline(eq("connection-03"));
		assertThat(remote.isOnline("connection-03"), equalTo(true));
		assertThat(remote.isLocallyOnline("connection-03"), equalTo(true));
		verify(remoteWentOfflineListener, never()).wentOffline(eq("connection-03"));
	}

	@Test
	public void testCommingOnlineForceNoOne() {
		local.forceOnline("connection-04");

		assertThat(local.isOnline("connection-04"), equalTo(true));
		assertThat(local.isLocallyOnline("connection-04"), equalTo(true));
		verify(localWentOfflineListener, never()).wentOffline(eq("connection-04"));
		assertThat(remote.isOnline("connection-04"), equalTo(true));
		assertThat(remote.isLocallyOnline("connection-04"), equalTo(false));
		verify(remoteWentOfflineListener, never()).wentOffline(eq("connection-04"));
	}
}
