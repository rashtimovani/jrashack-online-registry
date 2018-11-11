package net.rashack.onlineregistry;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;

public class OnlineEntity<T extends Serializable, U extends Serializable> implements Serializable {
	private static final long serialVersionUID = -1995616297920922684L;

	private final T id;
	private final U currentSession;

	public OnlineEntity(final T id, final U currentSession) {
		this.id = requireNonNull(id,
				"Online entity id must not be null, it is used throughout the system to identify single entity that came online");
		this.currentSession = requireNonNull(currentSession,
				"Current session identifier of online entity must not be null, it identifies current connection entity holds to system");
	}

	@Override
	public boolean equals(final Object that) {
		if (this == that) {
			return true;
		}
		if (that == null || !getClass().equals(that.getClass())) {
			return false;
		}
		final OnlineEntity<?, ?> other = (OnlineEntity<?, ?>) that;
		return id.equals(other.id) && currentSession.equals(other.currentSession);
	}

	public U getCurrentSession() {
		return this.currentSession;
	}

	public T getId() {
		return this.id;
	}

	@Override
	public int hashCode() {
		return id.hashCode() ^ currentSession.hashCode();
	}

	@Override
	public String toString() {
		return "OnlineEntity [id=" + id + ", currentSession=" + currentSession + "]";
	}
}
