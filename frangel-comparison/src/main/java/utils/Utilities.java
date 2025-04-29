package utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import frangel.Example;
import frangel.FrAngelResult;
import frangel.SynthesisTask;
import grammar.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Utilities {
	public static Object clone(Object obj) {
		if (obj instanceof SobeqValue<?>) {
			return ((SobeqValue<?>) obj).clone();
		} else {
			throw new IllegalArgumentException("Can't clone object of type " + obj.getClass() + ": " + obj);
		}
	}

	public static Class<?> toType(final String typ) {
		if (typ.equals("String")) {
			return Str.class;
		} else if (typ.equals("Int")) {
			return Int.class;
		} else if (typ.equals("Bool")) {
			return Bool.class;
		} else if (typ.equals("[String]")) {
			return StrArray.class;
		} else if (typ.equals("[Int]")) {
			return IntArray.class;
		} else if (typ.equals("{String}")) {
			return StrSet.class;
		} else if (typ.equals("{Int}")) {
			return IntSet.class;
		} else {
			throw new IllegalArgumentException("Type not recognized: " + typ);
		}
	}

	public static List<?> toArray(final Class<?> elemTyp, final String array) {
		JsonParser parser = new JsonParser();
		JsonArray arr = parser.parse(array).getAsJsonArray();
		List<Object> rs = new ArrayList<>();
		for (JsonElement elem : arr) {
			rs.add(fromJson(elemTyp, elem));
		}
		return rs;
	}

	public static java.util.Set<?> toSet(final Class<?> elemTyp, String array) {
		assert (array.startsWith("{") && array.endsWith("}"));
		array = "[" + array.substring(1, array.length() - 1) + "]";
		JsonParser parser = new JsonParser();
		JsonElement elements = parser.parse(array);
		JsonArray arr = elements.getAsJsonArray();
		HashSet<Object> rs = new HashSet<>();
		for (JsonElement elem : arr) {
			rs.add(fromJson(elemTyp, elem));
		}
		return rs;
	}

	public static Object fromJson(final Class<?> typ, final JsonElement elem) {
		Object rs;
		if (typ == Str.class) {
			String str = elem.getAsString();
			if (str.startsWith("'") && str.endsWith("'")) {
				rs = new Str(str.substring(1, str.length() - 1));
			} else {
				rs = new Str(str);
			}
		} else if (typ == Int.class) {
			rs = new Int(elem.getAsInt());
		} else if (typ == Bool.class) {
			rs = new Bool(elem.getAsBoolean());
		} else if (typ == IntArray.class) {
			rs = new IntArray((List<Int>) toArray(Int.class, elem.getAsString()));
		} else if (typ == StrArray.class) {
			rs = new StrArray((List<Str>) toArray(Str.class, elem.getAsString()));
		} else if (typ == IntSet.class) {
			rs = new IntSet((java.util.Set<Int>) toSet(Int.class, elem.getAsString()));
		} else if (typ == StrSet.class) {
			rs = new StrSet((java.util.Set<Str>) toSet(Str.class, elem.getAsString()));
		} else {
			// TODO Implement the rest!
			throw new IllegalArgumentException("Type not recognized: " + typ.toString());
		}

		// System.out.println(rs);
		return rs;
	}

	public static SynthesisTask fromFile(File file) throws IOException {
		try (Reader reader = new FileReader(file);
				Reader bufReader = new BufferedReader(reader)) {

			// Start the task!
			SynthesisTask task = new SynthesisTask()
					.addPackages("grammar") // Add the custom SObEq grammar
					.setName(
							"benchmark_" + file.getName()
									.replace(".sy", "")
									.replace(".json", "")
									.replace("-", "_"));
			task.clearLiterals(); // We'll add all of SObEq's literals later.

			JsonParser parser = new JsonParser();
			JsonObject json = parser.parse(bufReader).getAsJsonObject();

			// First, the inputs!
			// FrAngel uses indices for the inputs, so we need to keep track here.
			JsonObject vars = json.getAsJsonObject("variables");
			List<String> inputs = new ArrayList<>(vars.keySet());
			task.setInputNames(inputs.toArray(String[]::new));

			Class<?>[] types = inputs.stream().map(key -> toType(vars.get(key).getAsString())).toArray(Class<?>[]::new);
			task.setInputTypes(types);

			// Add the output type!
			Class<?> returnType = void.class;
			if (json.has("returnType")) {
				returnType = toType(json.get("returnType").getAsString());
			}
			task.setOutputType(returnType);

			// TODO FrAngel only allows primitive and String literals.
			// Is this enough for it to use the constructors and turn
			// them into our custom types? -> Yes!
			List<Int> intLiterals = new ArrayList<>();
			intLiterals.add(new Int(0, false));
			intLiterals.add(new Int(1, false));
			if (json.has("intLiterals")) {
				for (JsonElement l : json.getAsJsonArray("intLiterals")) {
					int lit = l.getAsInt();
					if (!intLiterals.contains(lit)) {
						intLiterals.add(new Int(lit, false));
					}
				}
			}
			task.addLiterals(Int.class, intLiterals.toArray());

			List<Str> strLiterals = new ArrayList<>();
			strLiterals.add(new Str(""));
			strLiterals.add(new Str(" "));
			if (json.has("stringLiterals")) {
				for (JsonElement l : json.getAsJsonArray("stringLiterals")) {
					String str = l.getAsString();
					if (!strLiterals.contains(str)) {
						strLiterals.add(new Str(str));
					}
				}
			}
			task.addLiterals(Str.class, strLiterals.toArray());

			final Set<String> immutables = new HashSet<>();
			if (json.has("immutables")) {
				// FrAngel's way of specifying mutability is weird.
				// There is a blanket "immutable" option, which can be overwritten
				// on a per-example per-variable basis.
				task.makeInputsMutable(false);

				// Get a set of our immutable variables.
				// Below, we use a special `any` value to allow inputs *not* in this list to be
				// mutable.
				json.getAsJsonArray("immutables").forEach(elem -> immutables.add(elem.getAsString()));
			}

			JsonArray examples = json.getAsJsonArray("examples");
			for (JsonElement ex : examples) {
				JsonObject obj = ex.getAsJsonObject();
				Example example = new Example();

				// Inputs!
				JsonObject exInputs = obj.getAsJsonObject("input");
				Object[] inputVals = new Object[inputs.size()];
				for (int i = 0; i < inputs.size(); i++) {
					inputVals[i] = Utilities.fromJson(types[i], exInputs.get(inputs.get(i)));
				}
				example.setInputs(() -> {
					Object[] copy = new Object[inputVals.length];
					for (int i = 0; i < inputVals.length; i++) {
						copy[i] = Utilities.clone(inputVals[i]);
					}
					return copy;
				});

				// For all variables *not* in immutables, set their
				// expected output value to be ANY. Anything that the
				// spec specifies will be overwritten below.
				// TODO Need to test that this actually works.
				if (!task.inputsMutable()) {
					for (int i = 0; i < inputs.size(); i++) {
						final String varName = inputs.get(i);
						if (!immutables.contains(varName)) {
							example.setModifiedInput(
									i + 1, // They're 1-indexed, because of course.
									SobeqValue.any(types[i]));
						}
					}
				}

				if (obj.has("state")) {
					JsonObject state = obj.getAsJsonObject("state");
					for (Map.Entry<String, JsonElement> entry : state.entrySet()) {
						int idx = inputs.indexOf(entry.getKey());
						Object val = Utilities.fromJson(types[idx], entry.getValue());
						example.setModifiedInput(idx + 1, val); // They're 1-indexed because of course.
					}
				}

				if (obj.has("output")) {
					Object out = Utilities.fromJson(returnType, obj.get("output"));
					example.setOutput(out);
				}

				task.addExample(example);
			}

			task.finalizeSetup();
			return task;
		}
	}

	public static void printCsvHeader(final Path csvFile) throws IOException {
		try (final FileWriter fw = new FileWriter(csvFile.toFile());
				final BufferedWriter writer = new BufferedWriter(fw)) {
			writer.write("name,time (ms),programs enumerated,solution,size,size (raw)" + System.lineSeparator());
		}
	}

	public static void printCsvRow(
			String name,
			final String path,
			final FrAngelResult result,
			final Path csvFile) throws IOException {
		final String time = String.format("%.0f", result.getTime() * 1000);
		final int programsEnumerated = result.getNumProgramsGen();

		String solution = "Timeout";
		String size = ""; String rawSize = "";
		if (result.isSuccess()) {
			// write the solution to a file and link it here.
			solution = name + "_0.java";
			size = String.valueOf(result.getProgramSize());
			rawSize = String.valueOf(result.getUnCleanedProgramSize());

			int counter = 0;
			while (new File(csvFile.getParent().toFile(), solution).exists()) {
				counter += 1;
				solution = name + "_" + counter + ".java";
			}

			try (final FileWriter fw = new FileWriter(new File(csvFile.getParent().toFile(), solution));
					final BufferedWriter writer = new BufferedWriter(fw)) {
				writer.write(result.getProgram());
			}
		}

		final String marker = "/src/test/benchmarks/";
		if (path.contains(marker)) {
			name = path.substring(path.indexOf(marker) + marker.length());
		}
		try (final FileWriter fw = new FileWriter(csvFile.toFile(), true);
				final BufferedWriter writer = new BufferedWriter(fw)) {
			writer.write(
					String.format("%s,%s,%d,%s,%s,%s" + System.lineSeparator(), name, time, programsEnumerated, solution,size,rawSize));
		}
	}
}
