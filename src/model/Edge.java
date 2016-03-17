package model;

/**
 * Created by Benedek on 3/17/2016.
 */
public class Edge {
    private Node parent;
    private Node child;

    public Edge(Node parent, Node child) {
        this.parent = parent;
        this.child = child;
    }

    public Node getParent() {
        return parent;
    }

    public Node getChild() {
        return child;
    }
}
