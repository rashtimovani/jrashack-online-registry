package net.rashack.onlineregistry.listener;

import java.io.Serializable;

import net.rashack.onlineregistry.OnlineEntity;

public interface WentOfflineListener<T extends Serializable, U extends Serializable> {

	void wentOffline(OnlineEntity<T, U> whoWentOffline);
}
