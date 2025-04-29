package grammar;

public class Bool extends SobeqValue<Boolean> implements Comparable<Bool> {
	public Bool(boolean value) {
		super(value);
	}

	// public Bool and(Bool other) {
	// 	return new Bool(this.inner && other.inner);
	// }

	// public Bool or(Bool other) {
	// 	return new Bool(this.inner || other.inner);
	// }

	@Override
	public Bool clone() {
		return new Bool(this.inner);
	}

	@Override
	public int compareTo(Bool o) {
		return this.inner.compareTo(o.inner);
	}
}
