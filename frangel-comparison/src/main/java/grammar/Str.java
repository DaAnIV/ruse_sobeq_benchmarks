package grammar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class Str extends SobeqValue<String> implements Comparable<Str> {

	public Str(String str) {
		super(str);
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj || (obj instanceof Str && this.inner.equals(((Str) obj).inner));
	}

	@Override
	public String toString() {
		return "Str(\"" + this.inner + "\")";
	}

	@Override
	public Str clone() {
		return new Str(this.inner);
	}

	public String deref(Int i) {
		return String.valueOf(this.inner.charAt(i.inner));
	}
	
	public Int length() {
		return new Int(this.inner.length());
	}

	public Bool isEqualTo(Str other) {
		return new Bool(this.inner.equals(other.inner));
	}

	public Str toUpper() {
		return new Str(this.inner.toUpperCase());
	}

	// public Str toLower() {
	// 	return new Str(this.inner.toLowerCase());
	// }

	public Str slice(Int start) {
		return new Str(this.inner.substring(start.inner));
	}

	public Str slice(Int start, Int end) {
		return new Str(this.inner.substring(start.inner, end.inner));
	}

	public Str concat(Str other) {
		return new Str(this.inner + other.inner);
	}

	// public Bool includes(Str other) {
	// 	return new Bool(this.inner.contains(other.inner));
	// }


	public StrArray split(Str other) {
		if (other.inner.equals("")) {
			char[] chars = this.inner.toCharArray();
			List<Str> rs = new ArrayList<>(chars.length);
			for (char c : chars) {
				rs.add(new Str(String.valueOf(c)));
			}
			return new StrArray(rs);
		} else {
			final String delim = other.inner;
			List<Str> rs = new ArrayList<>();
			String buf = this.inner;
			while (buf != null) {
				int idx = buf.indexOf(delim);
				if (idx == -1) {
					rs.add(new Str(buf));
					buf = null;
				} else {
					rs.add(new Str(buf.substring(0, idx)));
					buf = buf.substring(idx + delim.length());
				}
			}
			//Collections.reverse(rs);
			return new StrArray(rs);
		}
	}

	public Str trim() {
		return new Str(this.inner.trim());
	}

	public Str replace(Str ths, Str that) {
		return new Str(this.inner.replaceFirst(Pattern.quote(ths.inner), that.inner));
	}

	public Str replaceAll(Str ths, Str that) {
		return new Str(this.inner.replace(ths.inner, that.inner));
	}

	public Int indexOf(Str s) {
		return new Int(this.inner.indexOf(s.inner));
	}

	public Int lastIndexOf(Str s) {
		return new Int(this.inner.lastIndexOf(s.inner));
	}

	@Override
	public int compareTo(Str o) {
		return this.inner.compareTo(o.inner);
	}
}
