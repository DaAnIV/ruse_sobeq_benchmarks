package ruse.benchmarks.ruse;

import frangel.Example;
import frangel.SynthesisTask;
import frangel.Tag;
import frangel.benchmarks.TaskCreator;

import ruse.classes.ruse.BinarySearchTreeNode;

import ruse.benchmarks.RuseBenchmarkGroup;

public enum Ruse_bst_height implements TaskCreator {
    INSTANCE;
    static {
        RuseBenchmarkGroup.FRANGEL.register(INSTANCE);
    }

    @Override
    public SynthesisTask createTask() {
        SynthesisTask task = new SynthesisTask()
                .setName("ruse_bst_height")
                .setInputTypes(BinarySearchTreeNode.class)
                .setInputNames("tree")
                .setOutputType(int.class)
                .addPackages("ruse.classes.ruse")
                .addTags(Tag.SINGLE_LINE); // Easily written in one line

        task.addExample(new Example()
                .setInputs(() -> new Object[] { 
                        new BinarySearchTreeNode(1, null, new BinarySearchTreeNode(28)),
                     
                })
                .setOutput(2));

        task.addExample(new Example()
                .setInputs(() -> new Object[] { 
                        new BinarySearchTreeNode(3, null, new BinarySearchTreeNode(59, null, new BinarySearchTreeNode(59))),
                     
                })
                .setOutput(3));

        return task;
    }

    public static int solution(BinarySearchTreeNode tree) {
        return tree.getHeight();
    }
}
