package ruse.benchmarks.ruse.bst;

import frangel.Example;
import frangel.SynthesisTask;
import frangel.Tag;
import frangel.benchmarks.BenchmarkUtils;
import frangel.benchmarks.TaskCreator;

import ruse.classes.ruse.BinarySearchTreeNode;

import ruse.benchmarks.RuseBenchmarkGroup;

public enum Ruse_bst_get implements TaskCreator {
    INSTANCE;
    static {
        RuseBenchmarkGroup.FRANGEL.register(INSTANCE);
    }

    @Override
    public SynthesisTask createTask() {
        SynthesisTask task = new SynthesisTask()
                .setName("ruse_bst_get")
                .setInputTypes(BinarySearchTreeNode.class)
                .setInputNames("tree")
                .setOutputType(double.class)
                .addClasses(BinarySearchTreeNode.class)
                .addEqualityTester(Double.class, BenchmarkUtils::equalsDouble)
                .addTags(Tag.SINGLE_LINE); // Easily written in one line

        task.addExample(new Example()
                .setInputs(() -> new Object[] { 
                        new BinarySearchTreeNode(1, null, new BinarySearchTreeNode(28)),
                     
                })
                .setOutput(28.0));

        task.addExample(new Example()
                .setInputs(() -> new Object[] { 
                        new BinarySearchTreeNode(3, null, new BinarySearchTreeNode(59)),
                
                })
                .setOutput(59.0));

        return task;
    }

    public static double solution(BinarySearchTreeNode tree) {
        return tree.getRight().getValue();
    }
}
