package grammar;

import java.util.HashSet;

public class StrSet extends Set<Str> {
	public StrSet(java.util.Set<Str> inner) {
		super(inner);
	}

	@Override
	protected StrSet makeNew() {
		return new StrSet(new HashSet<>());
	}

	@Override
	public StrSet clone() {
		return new StrSet(new HashSet<>(this.inner));
	}


	public StrSet add(Str val) {
		return (StrSet) super.add(val);
	}
}
