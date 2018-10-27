package net.rashack.onlineregistry.listener;

import java.io.Serializable;

public interface WentOfflineListener<T extends Serializable> {

	void wentOffline(T connectionId);
}
