package grammar;

import java.util.ArrayList;

public abstract class SobeqValue<T> {
	protected T inner;
	protected boolean any = false;

	public static SobeqValue<?> any(Class<?> cls) {
		final SobeqValue<?> rs;
		if (cls == Bool.class) {
			rs = new Bool(false);
		} else if (cls == Int.class) {
			rs = new Int(-1);
		} else if (cls == Str.class) {
			rs = new Str("");
		} else if (cls == IntArray.class) {
			rs = new IntArray(new ArrayList<>());
		} else if (cls == StrArray.class) {
			rs = new StrArray(new ArrayList<>());
		} else {
			throw new IllegalArgumentException("Class not recognized: " + cls);
		}
		rs.any = true;
		return rs;
	}

	SobeqValue(T inner) {
		if (inner == null) {
			throw new IllegalArgumentException();
		}
		this.inner = inner;
	}

	public Bool eq(SobeqValue<T> other) {
		return new Bool(
			this == other ||
			(other != null && this.inner.equals(other.inner)));
	}

	@Override
	public int hashCode() {
		return this.inner.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof SobeqValue<?>)) return false;
		if (!this.getClass().equals(obj.getClass())) return false;
		SobeqValue<?> that = (SobeqValue<?>) obj;
		return that.any || this.inner.equals(that.inner);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + this.inner.toString() + ")";
	}

	public abstract SobeqValue<T> clone();
}
