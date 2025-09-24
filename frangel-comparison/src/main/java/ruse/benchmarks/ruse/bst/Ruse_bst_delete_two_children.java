package ruse.benchmarks.ruse.bst;

import frangel.Example;
import frangel.SynthesisTask;
import frangel.Tag;
import frangel.benchmarks.TaskCreator;

import ruse.classes.ruse.BinarySearchTreeNode;

import ruse.benchmarks.RuseBenchmarkGroup;

public enum Ruse_bst_delete_two_children implements TaskCreator {
    INSTANCE;
    static {
        RuseBenchmarkGroup.FRANGEL.register(INSTANCE);
    }

    @Override
    public SynthesisTask createTask() {
        SynthesisTask task = new SynthesisTask()
                .setName("ruse_bst_delete_two_children")
                .setInputTypes(BinarySearchTreeNode.class, BinarySearchTreeNode.class)
                .setInputNames("tree", "node_to_delete")
                .addClasses(BinarySearchTreeNode.class)
                .addTags(Tag.SINGLE_LINE); // Easily written in one line

        task.addExample(new Example()
                .setInputs(() -> { 
                    BinarySearchTreeNode tree = new BinarySearchTreeNode(5, new BinarySearchTreeNode(2, new BinarySearchTreeNode(1), new BinarySearchTreeNode(3)), new BinarySearchTreeNode(10, new BinarySearchTreeNode(6), new BinarySearchTreeNode(11)));
                    BinarySearchTreeNode node_to_delete = tree.getRight();            
                    return new Object[] { 
                        tree,
                        node_to_delete
                    };
                })
                .setModifiedInputChecker(1, (BinarySearchTreeNode tree) -> tree != null && tree.getSize() == 6 && tree.valid() && !tree.contains(10)));

        task.addExample(new Example()
                .setInputs(() -> { 
                    BinarySearchTreeNode tree = new BinarySearchTreeNode(5, new BinarySearchTreeNode(2, new BinarySearchTreeNode(1), new BinarySearchTreeNode(3)), new BinarySearchTreeNode(10, new BinarySearchTreeNode(6), new BinarySearchTreeNode(11)));
                    BinarySearchTreeNode node_to_delete = tree.getLeft();            
                    return new Object[] { 
                        tree,
                        node_to_delete
                    };}
                )
                .setModifiedInputChecker(1, (BinarySearchTreeNode tree) -> tree != null && tree.getSize() == 6 && tree.valid() && !tree.contains(2)));
        
        task.addExample(new Example()
                .setInputs(() -> { 
                    BinarySearchTreeNode tree = new BinarySearchTreeNode(5, new BinarySearchTreeNode(2, new BinarySearchTreeNode(1), new BinarySearchTreeNode(3)), new BinarySearchTreeNode(10, new BinarySearchTreeNode(6), new BinarySearchTreeNode(11)));
                    BinarySearchTreeNode node_to_delete = tree;            
                    return new Object[] { 
                        tree,
                        node_to_delete
                    };
                    }
                )
                .setModifiedInputChecker(1, (BinarySearchTreeNode tree) -> tree != null && tree.getSize() == 6 && tree.valid() && !tree.contains(5)));

        return task;
    }

    public static void solution(BinarySearchTreeNode tree, BinarySearchTreeNode node_to_delete) {
        node_to_delete.getRight().minNode().swap(node_to_delete).unlinkLeaf();
    }
}
