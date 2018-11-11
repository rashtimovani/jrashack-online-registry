package net.rashack.onlineregistry.impl;

import java.io.Serializable;

import net.rashack.onlineregistry.OnlineEntity;

public class ConnectionMetaData<T extends Serializable, U extends Serializable> implements Serializable {

	private static final long serialVersionUID = -2407456226550897178L;

	private final String owner;
	private final OnlineEntity<T, U> onlineEntity;

	public ConnectionMetaData(final String owner, final OnlineEntity<T, U> onlineEntity) {
		this.owner = owner;
		this.onlineEntity = onlineEntity;
	}

	public OnlineEntity<T, U> getOnlineEntity() {
		return this.onlineEntity;
	}

	public String getOwner() {
		return this.owner;
	}
}
