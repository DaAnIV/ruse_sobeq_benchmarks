import frangel.FrAngel;
import frangel.FrAngelResult;
import frangel.Settings;
import frangel.SynthesisTask;
import frangel.utils.Utils;
import utils.Utilities;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Objects;

public class Main {
	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			System.err.println("Please provide the path to the synthesis task.");
			System.exit(1);
		}

		Path path = Path.of(args[0]);

		if (Files.notExists(path)) {
			System.err.println("Path does not exist: " + path);
			System.exit(1);
		}

		int timeout = 3600;

		if (args.length > 1) {
			timeout = Integer.parseInt(args[2]);
		}

		// Setup FrAngel
		Settings.USE_ANGELIC_CONDITIONS = false;
		Settings.USE_SIMPLE_NAME = true;

		// Create a new output directory and csv file
		Path outputDir = Path.of(".", (new Date()).getTime() + "_frangel_results");
		while (Files.exists(outputDir)) {
			outputDir = Path.of(".", (new Date()).getTime() + "_frangel_results");
		}
		outputDir = Files.createDirectory(outputDir);
		Path csv = Path.of(outputDir.toAbsolutePath().toString(), "results.csv");
		csv = Files.createFile(csv);

		// First, warm up!
		System.out.print("Warming up... ");
		runBenchmarks(path.toFile(), null, 5, true);
		System.out.println("Done!");

		Utilities.printCsvHeader(csv);
		runBenchmarks(path.toFile(), csv, timeout);
	}

	public static void runBenchmarks(File file, Path csvOutput, int timeout) throws IOException {
		runBenchmarks(file, csvOutput, timeout, false);
	}

	public static void runBenchmarks(File file, Path csvOutput, int timeout, boolean warmup) throws IOException {
		if (file.isDirectory()) {
			for (File subf : Objects.requireNonNull(file.listFiles())) {
				runBenchmarks(subf, csvOutput, timeout, warmup);
			}
		} else if (file.getName().endsWith(".sy")) {
			int count = warmup ? 1 : 5;

			for (int i = 0; i < count; i++) {
				if (!warmup)
					System.out.print("Synthesizing " + file.getName() + ":\t ");

				SynthesisTask task = Utilities.fromFile(file);
				FrAngel synth = new FrAngel(task);
				Settings.VERBOSE = 0;
				FrAngelResult result = synth.run(Utils.getTimeout(timeout));

				if (!warmup)
					System.out.printf("[%.3f] ", result.getTime());

				if (!warmup) {
					if (result.isSuccess()) {
						System.out.println("SUCCESS\n---\n" + result.getProgram() + "\n---\n");
					} else {
						System.out.println("FAILED");
					}
					Utilities.printCsvRow(task.getName(), file.getAbsolutePath(), result, csvOutput);
				}
			}
		} else {
			System.out.println("Not a benchmark file: " + file.getAbsolutePath());
		}
	}
}
