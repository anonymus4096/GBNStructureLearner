package model;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Benedek on 3/17/2016.
 */
public class Node {
    private final String name;
    private Set<Node> parents;
    private Set<Node> children;
    private int id = -1;
    private Network network;


    public Node(String s, Network network1) {
        name = s;
        network = network1;
        parents = new HashSet<>();
        children = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public Set<Node> getChildren() {
        return children;
    }

    public Set<Node> getParents() {
        return parents;
    }

    public Set<Node> getDescendants() {
        Set<Node> descendants = new HashSet<>();
        descendants = getChildren();
        getAllDescendants(descendants);
        return descendants;
    }

    private void getAllDescendants(Set<Node> currentDescendants) {
        for (Node n : currentDescendants) {
            if (currentDescendants.addAll(getChildren())) {
                getAllDescendants(currentDescendants);
            } else {
                return;
            }
        }
    }

    public void addNewChild(Node n) {
        children.add(n);
    }

    public void addNewParent(Node n) {
        parents.add(n);
    }

}
