package grammar;

public class Int extends SobeqValue<Integer> implements Comparable<Int> {
	final boolean lhs;

	public Int(int value) {
		this(value, true);
	}

	public Int(int value, boolean lhs) {
		super(value);
		this.lhs = lhs;
	}

	public Int add(Int other) {
		return new Int(this.inner + other.inner);
	}

	// public Int multiply(Int other) {
	// 	return new Int(this.inner * other.inner);
	// }

	public Int subtract(Int other) {
		return new Int(this.inner - other.inner);
	}

	// public Int divide(Int other) {
	// 	return new Int(this.inner / other.inner);
	// }

	// public Int modulo(Int other) {
	// 	return new Int(this.inner % other.inner);
	// }

	public Int minus() {
		return new Int(-this.inner);
	}

	public Int inc() {
		if (!this.lhs)
			return null;
		Int rs = new Int(this.inner);
		this.inner++;
		return rs;
	}

	// public Int power(Int other) {
	// 	return new Int((int) Math.pow(this.inner, other.inner));
	// }

	public Bool lessThan(Int other) {
		return new Bool(this.inner < other.inner);
	}

	// public Bool greaterThanEq(Int other) {
	// 	return new Bool(this.inner >= other.inner);
	// }

	@Override
	public Int clone() {
		return new Int(this.inner);
	}

	@Override
	public int compareTo(Int o) {
		return this.inner.compareTo(o.inner);
	}
}
