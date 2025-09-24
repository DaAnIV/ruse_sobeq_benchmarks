package ruse.benchmarks.ruse.simple;

import frangel.Example;
import frangel.SynthesisTask;
import frangel.Tag;
import frangel.benchmarks.TaskCreator;

import ruse.classes.ruse.BinaryTree;

import ruse.benchmarks.RuseBenchmarkGroup;

public enum Ruse_inc_tree_values implements TaskCreator {
    INSTANCE;
    static {
        RuseBenchmarkGroup.FRANGEL.register(INSTANCE);
    }

    @Override
    public SynthesisTask createTask() {
        SynthesisTask task = new SynthesisTask()
                .setName("ruse_inc_tree_values")
                .setInputTypes(BinaryTree.class, BinaryTree.class, BinaryTree.class)
                .setInputNames("tree1", "tree2", "tree3")
                .addClasses(BinaryTree.class)
                .addTags(Tag.SINGLE_LINE); // Easily written in one line

        task.addExample(new Example()
                .setInputs(() -> new Object[] { 
                        new BinaryTree(5),
                        new BinaryTree(6),
                        new BinaryTree(7, new BinaryTree(5), new BinaryTree(6))
                     })
                .setModifiedInputChecker(1, (BinaryTree tree1) -> tree1 != null && tree1.getValue() == 6)
                .setModifiedInputChecker(2, (BinaryTree tree2) -> tree2 != null && tree2.getValue() == 7)
                .setModifiedInputChecker(3, (BinaryTree tree3) -> tree3 != null && tree3.getValue() == 8));

        return task;
    }

    public static void solution(BinaryTree tree1, BinaryTree tree2, BinaryTree tree3) {
        tree1.incValue();
        tree2.incValue();
        tree3.incValue();
    }
}
