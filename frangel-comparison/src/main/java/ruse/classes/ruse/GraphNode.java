package ruse.classes.ruse;

import java.util.Vector;
import java.util.Arrays;

public class GraphNode {
    public int value;
    public Vector<GraphNode> neighbors;

    public GraphNode(int value, GraphNode... neighbors) {
        this.value = value;
        this.neighbors = new Vector<GraphNode>(Arrays.asList(neighbors));
    }

    public void incValue(int delta) {
        this.value += delta;
    }
}
