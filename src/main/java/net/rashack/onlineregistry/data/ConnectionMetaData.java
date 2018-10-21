package net.rashack.onlineregistry.data;

import java.io.Serializable;

public class ConnectionMetaData implements Serializable {

	private static final long serialVersionUID = -2407456226550897178L;

	private final String owner;

	public ConnectionMetaData(String owner) {
		this.owner = owner;
	}

	public String getOwner() {
		return this.owner;
	}
}
