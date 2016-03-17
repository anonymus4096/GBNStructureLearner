package model;

import java.util.List;

/**
 * Created by Benedek on 3/17/2016.
 */
public class Node {
    private final String name;
    private List<Node> parents;
    private List<Node> children;
    private int id = -1;


    public Node(String s) {
        name = s;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public List<Node> getChildren() {
        return children;
    }

    public List<Node> getParents() {
        return parents;
    }
}
