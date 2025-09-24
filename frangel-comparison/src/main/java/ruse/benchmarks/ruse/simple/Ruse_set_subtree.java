package ruse.benchmarks.ruse.simple;

import frangel.Example;
import frangel.SynthesisTask;
import frangel.Tag;
import frangel.benchmarks.TaskCreator;

import ruse.classes.ruse.BinaryTree;

import ruse.benchmarks.RuseBenchmarkGroup;

public enum Ruse_set_subtree implements TaskCreator {
    INSTANCE;
    static {
        RuseBenchmarkGroup.FRANGEL.register(INSTANCE);
    }

    @Override
    public SynthesisTask createTask() {
        SynthesisTask task = new SynthesisTask()
                .setName("ruse_set_subtree")
                .setInputTypes(BinaryTree.class, BinaryTree.class)
                .setInputNames("tree1", "tree2")
                .addClasses(BinaryTree.class)
                .addTags(Tag.SINGLE_LINE); // Easily written in one line

        task.addExample(new Example()
                .setInputs(() -> new Object[] { 
                        new BinaryTree(5, new BinaryTree(2), new BinaryTree(3)),
                        new BinaryTree(50, new BinaryTree(20), new BinaryTree(30))
                     })
                .setModifiedInputChecker(1, (BinaryTree tree1) -> tree1 != null && tree1.getSize() == 5 && tree1.getRight().getValue() == 51));

        return task;
    }

    public static void solution(BinaryTree tree1, BinaryTree tree2) {
        tree1.setRight(tree2);
        tree2.incValue();
    }
}
