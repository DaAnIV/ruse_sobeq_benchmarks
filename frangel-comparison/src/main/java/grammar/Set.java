package grammar;

abstract class Set<T extends Comparable<T>> extends SobeqValue<java.util.Set<T>> {

	protected Set(java.util.Set<T> inner) {
		super(inner);
	}

	protected abstract Set<T> makeNew();

	public abstract Set<T> clone();

	public Int size() {
		return new Int(this.inner.size());
	}

	public Set<T> add(T val) {
		this.inner.add(val);
		return this;
	}

	public Bool delete(T val) {
		return new Bool(this.inner.remove(val));
	}

	public Bool has(T val) {
		return new Bool(this.inner.contains(val));
	}
}
