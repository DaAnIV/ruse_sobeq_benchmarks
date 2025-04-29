package grammar;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

public class StrArray extends Array<Str> {

	public StrArray(List<Str> values) {
		super(values);
	}

	public StrArray(Str value) {
		super(new ArrayList<>());
		this.inner.add(value);
	}

	@Override
	protected StrArray makeNew() {
		return new StrArray(new ArrayList<>());
	}

	@Override
	public StrArray clone() {
		return new StrArray(new ArrayList<>(this.inner));
	}

	@Override
	public StrArray slice(Int start) {
		return (StrArray) super.slice(start);
	}

	@Override
	public StrArray slice(Int start, Int end) {
		return (StrArray) super.slice(start, end);
	}

	@Override
	public StrArray sort() {
		return (StrArray) super.sort();
	}

	@Override
	public StrArray reverse() {
		return (StrArray) super.reverse();
	}

	@Override
	public StrArray splice(Int start, Int end) {
		return (StrArray) super.splice(start, end);
	}

	@Override
	public StrArray splice(Int start) {
		return (StrArray) super.splice(start);
	}

	public StrArray concat(StrArray other) {
		return (StrArray) super.concat(other);
	}

	@Override
	public Str join(final Str s) {
		return new Str(this.inner
			.stream()
			.map(item -> item.inner)
			.collect(Collectors.joining(s.inner)));
	}
}
