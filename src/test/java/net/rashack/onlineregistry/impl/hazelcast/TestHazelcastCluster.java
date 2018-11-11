package net.rashack.onlineregistry.impl.hazelcast;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import net.rashack.onlineregistry.OnlineEntity;
import net.rashack.onlineregistry.OnlineRegistry;
import net.rashack.onlineregistry.listener.WentOfflineListener;

public class TestHazelcastCluster {

	private OnlineRegistry<String, String> local;
	private OnlineRegistry<String, String> remote;

	private WentOfflineListener<String, String> localWentOfflineListener;
	private WentOfflineListener<String, String> remoteWentOfflineListener;

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
		final OnlineEntity<String, String> entity = new OnlineEntity<>("connection-01", "cs-01");

		final boolean cameOnline = local.comeOnline(entity);

		assertThat(cameOnline, equalTo(true));
		assertThat(local.isOnline("connection-01"), equalTo(true));
		assertThat(local.isLocallyOnline("connection-01"), equalTo(true));
		assertThat(remote.isOnline("connection-01"), equalTo(true));
		assertThat(remote.isLocallyOnline("connection-01"), equalTo(false));
	}

	@Test
	public void testCommingOnlineAlreadyOnline() {
		final OnlineEntity<String, String> entity = new OnlineEntity<>("connection-02", "cs-01");

		final boolean cameOnline = local.comeOnline(entity);

		assertThat(cameOnline, equalTo(true));
		assertThat(local.isOnline("connection-02"), equalTo(true));
		assertThat(local.isLocallyOnline("connection-02"), equalTo(true));
		assertThat(remote.comeOnline(new OnlineEntity<>("connection-02", "cs-02")), equalTo(false));
		assertThat(remote.isOnline("connection-02"), equalTo(true));
		assertThat(remote.isLocallyOnline("connection-02"), equalTo(false));
	}

	@Test
	public void testCommingOnlineForce() {
		final OnlineEntity<String, String> entity = new OnlineEntity<>("connection-03", "cs-01");
		final OnlineEntity<String, String> forcedEntity = new OnlineEntity<>("connection-03", "cs-02");
		local.comeOnline(entity);

		remote.forceOnline(forcedEntity);

		assertThat(local.isOnline("connection-03"), equalTo(true));
		assertThat(local.isLocallyOnline("connection-03"), equalTo(false));
		verify(localWentOfflineListener, times(1)).wentOffline(eq(entity));
		assertThat(remote.isOnline("connection-03"), equalTo(true));
		assertThat(remote.isLocallyOnline("connection-03"), equalTo(true));
		verify(remoteWentOfflineListener, never()).wentOffline(any());
	}

	@Test
	public void testCommingOnlineForceBySameInstance() {
		final OnlineEntity<String, String> entity = new OnlineEntity<>("connection-04", "cs-01");
		final OnlineEntity<String, String> forcedEntity = new OnlineEntity<>("connection-04", "cs-02");
		local.comeOnline(entity);

		local.forceOnline(forcedEntity);

		assertThat(local.isOnline("connection-04"), equalTo(true));
		assertThat(local.isLocallyOnline("connection-04"), equalTo(true));
		verify(localWentOfflineListener, times(1)).wentOffline(eq(entity));
		assertThat(remote.isOnline("connection-04"), equalTo(true));
		assertThat(remote.isLocallyOnline("connection-04"), equalTo(false));
		verify(remoteWentOfflineListener, never()).wentOffline(any());
	}

	@Test
	public void testCommingOnlineForceNoOnePreviouslyOnline() {
		final OnlineEntity<String, String> entity = new OnlineEntity<>("connection-05", "cs-01");

		local.forceOnline(entity);

		assertThat(local.isOnline("connection-05"), equalTo(true));
		assertThat(local.isLocallyOnline("connection-05"), equalTo(true));
		verify(localWentOfflineListener, never()).wentOffline(any());
		assertThat(remote.isOnline("connection-05"), equalTo(true));
		assertThat(remote.isLocallyOnline("connection-05"), equalTo(false));
		verify(remoteWentOfflineListener, never()).wentOffline(any());
	}

	@Test
	public void testWentOffline() {
		final OnlineEntity<String, String> entity = new OnlineEntity<>("connection-06", "cs-01");
		local.comeOnline(entity);

		final boolean wentOffline = local.wentOffline(entity);

		assertThat(wentOffline, equalTo(true));
		assertThat(local.isOnline("connection-06"), equalTo(false));
		assertThat(local.isLocallyOnline("connection-06"), equalTo(false));
		verify(localWentOfflineListener, times(1)).wentOffline(eq(entity));
		assertThat(remote.isOnline("connection-06"), equalTo(false));
		assertThat(remote.isLocallyOnline("connection-06"), equalTo(false));
		verify(remoteWentOfflineListener, never()).wentOffline(any());
	}

	@Test
	public void testWentOfflineButIsOnlineOnRemote() {
		final OnlineEntity<String, String> entity = new OnlineEntity<>("connection-07", "cs-01");
		remote.comeOnline(entity);

		final boolean wentOffline = local.wentOffline(entity);

		assertThat(wentOffline, equalTo(false));
		assertThat(local.isOnline("connection-07"), equalTo(true));
		assertThat(local.isLocallyOnline("connection-07"), equalTo(false));
		verify(localWentOfflineListener, never()).wentOffline(any());
		assertThat(remote.isOnline("connection-07"), equalTo(true));
		assertThat(remote.isLocallyOnline("connection-07"), equalTo(true));
		verify(remoteWentOfflineListener, never()).wentOffline(any());
	}

	@Test
	public void testWentOfflineButWasNotOnline() {
		final OnlineEntity<String, String> entity = new OnlineEntity<>("connection-08", "cs-01");

		final boolean wentOffline = local.wentOffline(entity);

		assertThat(wentOffline, equalTo(false));
		assertThat(local.isOnline("connection-08"), equalTo(false));
		assertThat(local.isLocallyOnline("connection-08"), equalTo(false));
		verify(localWentOfflineListener, never()).wentOffline(any());
		assertThat(remote.isOnline("connection-08"), equalTo(false));
		assertThat(remote.isLocallyOnline("connection-08"), equalTo(false));
		verify(remoteWentOfflineListener, never()).wentOffline(any());
	}

	@Test
	public void testWentOfflineDifferentSession() {
		final OnlineEntity<String, String> entity = new OnlineEntity<>("connection-09", "cs-02");
		final OnlineEntity<String, String> differentSession = new OnlineEntity<>("connection-09", "cs-01");
		local.comeOnline(entity);

		final boolean wentOffline = local.wentOffline(differentSession);

		assertThat(wentOffline, equalTo(false));
		assertThat(local.isOnline("connection-09"), equalTo(true));
		assertThat(local.isLocallyOnline("connection-09"), equalTo(true));
		verify(localWentOfflineListener, never()).wentOffline(any());
		assertThat(remote.isOnline("connection-09"), equalTo(true));
		assertThat(remote.isLocallyOnline("connection-09"), equalTo(false));
		verify(remoteWentOfflineListener, never()).wentOffline(any());
	}
}
