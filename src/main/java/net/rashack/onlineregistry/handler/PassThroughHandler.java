package net.rashack.onlineregistry.handler;

public interface PassThroughHandler<T> {

	void makeOffline(T connectionId);
}
