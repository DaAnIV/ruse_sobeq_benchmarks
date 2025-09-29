package ruse.benchmarks.ruse.simple;

import frangel.Example;
import frangel.SynthesisTask;
import frangel.Tag;
import frangel.benchmarks.TaskCreator;

import ruse.classes.ruse.GraphNode;

import ruse.benchmarks.RuseBenchmarkGroup;

public enum Ruse_mut_graph_one_way_connected implements TaskCreator {
    INSTANCE;
    static {
        RuseBenchmarkGroup.FRANGEL.register(INSTANCE);
    }

    @Override
    public SynthesisTask createTask() {
        SynthesisTask task = new SynthesisTask()
                .setName("ruse_mut_graph_one_way_connected")
                .setInputTypes(GraphNode.class, GraphNode.class, GraphNode.class)
                .setInputNames("graph1", "graph2", "graph3")
                .addClasses(GraphNode.class)
                .addTags(Tag.SINGLE_LINE); // Easily written in one line

        task.addExample(new Example()
                .setInputs(() -> {
                    GraphNode graph3 = new GraphNode(7, new GraphNode(5, new GraphNode(6, new GraphNode(7))));
                    GraphNode graph1 = graph3.neighbors.get(0);
                    GraphNode graph2 = graph1.neighbors.get(0);
                    return new Object[] { 
                        graph1,
                        graph2,
                        graph3
                    };
                })
                .setModifiedInputChecker(1, (GraphNode graph1) -> graph1 != null && graph1.value == 6)
                .setModifiedInputChecker(2, (GraphNode graph2) -> graph2 != null && graph2.value == 7)
                .setModifiedInputChecker(3, (GraphNode graph3) -> graph3 != null && graph3.value == 8));

        return task;
    }

    public static void solution(GraphNode graph1, GraphNode graph2, GraphNode graph3) {
        graph1.incValue();
        graph2.incValue();
        graph3.incValue();
    }
}
