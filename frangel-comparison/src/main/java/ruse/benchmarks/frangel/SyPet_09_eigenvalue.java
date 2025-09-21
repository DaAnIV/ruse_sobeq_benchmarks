package ruse.benchmarks.frangel;

import java.util.Arrays;

import frangel.Example;
import frangel.SynthesisTask;
import frangel.benchmarks.TaskCreator;

import ruse.classes.linear.EigenvalueDecomposition;
import ruse.classes.linear.Matrix;
import ruse.benchmarks.RuseBenchmarkGroup;
import ruse.benchmarks.RuseBenchmarkUtils;

public enum SyPet_09_eigenvalue implements TaskCreator {
    INSTANCE;
    static {
        RuseBenchmarkGroup.FRANGEL.register(INSTANCE);
    }

    @Override
    public SynthesisTask createTask() {
        SynthesisTask task = new SynthesisTask()
                .setName("sypet_09_eigenvalue")
                .setInputTypes(Matrix.class, int.class)
                .setInputNames("mat", "index")
                .setOutputType(double[].class)
                .addPackages("ruse.classes.linear")
                .addEqualityTester(Double[].class, RuseBenchmarkUtils::equalsDoubleArray)
                .addTags(/* none applicable */); // Would have to create EigenDecomposition twice for single-line solution

        task.addExample(new Example()
                .setInputs(() -> new Object[] { new Matrix(new double[][] { { 0, -20 }, { 10, 10 } }), 0 })
                .setOutput(new double[]{5, 5*Math.sqrt(7)}));

        task.addExample(new Example()
                .setInputs(() -> new Object[] { new Matrix(new double[][] { { 0, 2 }, { 2, 0 } }), 1 })
                .setOutput(new double[]{2, 0}));

        return task;
    }

    public static double[] solution(Matrix arg0, int arg1) {
        EigenvalueDecomposition v1 = new EigenvalueDecomposition(arg0);
        double[] v2 = v1.getEigenvalue(arg1);
        return v2;
    }
}
