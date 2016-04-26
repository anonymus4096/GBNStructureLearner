package model;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import static utils.GraphFunctions.containsNodeWithName;

/**
 * Created by Benedek on 3/17/2016.
 */
public class Node implements Comparable {
    private final String name;
    private Set<Node> parents;
    private Set<Node> children;
    private Network network;


    public Node(String s, Network network) {
        name = s;
        network = network;
        parents = new HashSet<>();
        children = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public Set<Node> getChildren() {
        return children;
    }

    public Set<Node> getParents() {
        return parents;
    }

    public Set<Node> getDescendants() {
        Set<Node> descendants = getChildren();
        descendants = getAllDescendants(descendants);
        return descendants;
    }

    private Set<Node> getAllDescendants(Set<Node> currentDescendants) {
        Set<Node> newDescendants = new TreeSet<>(currentDescendants);
        for (Node n : currentDescendants) {
            boolean anyNewNodes = false;
            for (Node child : n.getChildren()) {
                if (!containsNodeWithName(newDescendants, child.getName())) {
                    newDescendants.add(child);
                    anyNewNodes = true;
                }
            }
            if (anyNewNodes){
                newDescendants = getAllDescendants(newDescendants);
            } else {
                break;
            }
        }
        return newDescendants;
    }

    public void addNewChild(Node n) {
        children.add(n);
    }

    public void addNewParent(Node n) {
        parents.add(n);
    }

    @Override
    public int compareTo(Object o) {
        Node n = (Node) o;
        if (getName().compareTo(n.getName()) > 0) {
            return 1;
        } else {
            return -1;
        }
    }
}
