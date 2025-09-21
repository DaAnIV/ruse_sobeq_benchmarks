package ruse.benchmarks.frangel;

import java.util.Map;
import static java.util.Map.entry;

import frangel.Example;
import frangel.SynthesisTask;
import frangel.Tag;
import frangel.benchmarks.TaskCreator;

import ruse.benchmarks.RuseBenchmarkGroup;
import ruse.benchmarks.RuseBenchmarkUtils;
import ruse.classes.polynomial.*;

public enum SyPet_03_findRoots implements TaskCreator {
    INSTANCE;
    static {
        RuseBenchmarkGroup.FRANGEL.register(INSTANCE);
    }

    @Override
    public SynthesisTask createTask() {
        SynthesisTask task = new SynthesisTask()
                .setName("sypet_03_findRoots")
                .setInputTypes(Polynomial.class)
                .setInputNames("func")
                .setOutputType(Complex[].class)
                .addPackages("ruse.classes.polynomial")
                .addTags(Tag.SINGLE_LINE); // Easily written in one line

        task.addExample(new Example()
                .setInputs(() -> new Object[] { new Polynomial(Map.ofEntries(entry(0, 1.0), entry(1, -2.0), entry(2, 1.0))) })
                .setOutputChecker((Complex[] comp) -> RuseBenchmarkUtils.checkComplexArray(comp, new Complex[] { new Complex(1.0, 0.0), new Complex(1.0, 0.0) })));

        task.addExample(new Example()
            .setInputs(() -> new Object[] { new Polynomial(Map.ofEntries(entry(0, 1.0), entry(1, -1.0), entry(2, 1.0))) })
            .setOutputChecker((Complex[] comp) -> RuseBenchmarkUtils.checkComplexArray(comp, new Complex[] { new Complex(0.5, 0.866025), new Complex(0.5, -0.866025) })));

        return task;
    }

    public static Complex[] solution(Polynomial arg0) {
        return FindRoots.findRoots(arg0.realCoeffs());
    }
}
