package ruse.benchmarks.frangel;

import frangel.Example;
import frangel.SynthesisTask;
import frangel.Tag;
import frangel.benchmarks.TaskCreator;

import ruse.benchmarks.RuseBenchmarkGroup;
import ruse.classes.linear.Matrix;

public enum SyPet_02_getInnerProduct implements TaskCreator {
    INSTANCE;
    static {
        RuseBenchmarkGroup.FRANGEL.register(INSTANCE);
    }

    @Override
    public SynthesisTask createTask() {
        SynthesisTask task = new SynthesisTask()
                .setName("sypet_02_getInnerProduct")
                .setInputTypes(Matrix.class, Matrix.class)
                .setInputNames("vec1", "vec2")
                .setOutputType(double.class)
                .addPackages("ruse.classes.linear")
                .addTags(Tag.SINGLE_LINE); // Easily written in one line

        task.addExample(new Example()
                .setInputs(() -> new Object[] { new Matrix(new double[][]{{1,1,1}}), new Matrix(new double[][]{{1,2,3}}) })
                .setOutput(6.0));

        return task;
    }

    public static double solution(Matrix vec1, Matrix vec2) {
        return vec1.dot(vec2);
    }
}
