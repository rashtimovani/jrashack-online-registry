package net.rashack.onlineregistry.handler;

public interface MakeOfflineHandler<T> {

	void makeOffline(T connectionId);
}
