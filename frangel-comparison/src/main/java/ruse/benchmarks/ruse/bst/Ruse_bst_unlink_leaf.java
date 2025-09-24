package ruse.benchmarks.ruse.bst;

import frangel.Example;
import frangel.SynthesisTask;
import frangel.Tag;
import frangel.benchmarks.TaskCreator;

import ruse.classes.ruse.BinarySearchTreeNode;

import ruse.benchmarks.RuseBenchmarkGroup;

public enum Ruse_bst_unlink_leaf implements TaskCreator {
    INSTANCE;
    static {
        RuseBenchmarkGroup.FRANGEL.register(INSTANCE);
    }

    @Override
    public SynthesisTask createTask() {
        SynthesisTask task = new SynthesisTask()
                .setName("ruse_bst_unlink_leaf")
                .setInputTypes(BinarySearchTreeNode.class)
                .setInputNames("tree")
                .addClasses(BinarySearchTreeNode.class)
                .addTags(Tag.SINGLE_LINE); // Easily written in one line

        task.addExample(new Example()
                .setInputs(() -> new Object[] { 
                        new BinarySearchTreeNode(1, null, new BinarySearchTreeNode(28)),
                    })
                .setModifiedInputChecker(1, (BinarySearchTreeNode tree) -> tree != null && tree.getSize() == 1));

        return task;
    }

    public static void solution(BinarySearchTreeNode tree) {
        tree.getRight().unlinkLeaf();
    }
}
