package grammar;

import java.util.HashSet;

public class IntSet extends Set<Int> {
	public IntSet(java.util.Set<Int> inner) {
		super(inner);
	}

	@Override
	protected IntSet makeNew() {
		return new IntSet(new HashSet<>());
	}

	@Override
	public IntSet clone() {
		return new IntSet(new HashSet<>(this.inner));
	}


	public IntSet add(Int val) {
		return (IntSet) super.add(val);
	}
}
