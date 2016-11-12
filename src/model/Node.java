package model;

import java.util.Set;
import java.util.TreeSet;

import static utils.GraphFunctions.containsNodeWithName;
import static utils.GraphFunctions.getNodeWithName;

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
        this.network = network;
        parents = new TreeSet<>();
        children = new TreeSet<>();
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

    public Set<Node> getAncestors() {
        Set<Node> ancestors = getParents();
        ancestors = getAllAncestors(ancestors);
        return ancestors;
    }

    private Set<Node> getAllAncestors(Set<Node> currentAncestors) {
        Set<Node> newAncestors = new TreeSet<>(currentAncestors);
        for (Node n : currentAncestors) {
            boolean anyNewNodes = false;
            for (Node parent : n.getParents()) {
                if (!containsNodeWithName(newAncestors, parent.getName())) {
                    newAncestors.add(parent);
                    anyNewNodes = true;
                }
            }
            if (anyNewNodes) {
                newAncestors = getAllAncestors(newAncestors);
            }
        }
        return newAncestors;
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

//    @Override
//    public int compareTo(Object o) {
//        Node n = (Node) o;
//        int ID = 0, otherID = 0;
//        boolean isGene = true;
//
//        if (Pattern.matches("(G|g)(E|e)(N|n)(E|e)[0-9]+", name)) {
//            ID = Integer.parseInt(name.substring(4));
//        } else {
//            isGene = false;
//        }
//
//        if (Pattern.matches("(G|g)(E|e)(N|n)(E|e)[0-9]+", n.getName())) {
//            otherID = Integer.parseInt(n.getName().substring(4));
//        } else {
//            isGene = false;
//        }
//
//
//        if (isGene) {
//            if (ID > otherID) {
//                return 1;
//            } else if (ID < otherID) {
//                return -1;
//            } else {
//                return 0;
//            }
//        } else {
//            if (getName().compareTo(n.getName()) > 0) {
//                return 1;
//            } else if (getName().compareTo(n.getName()) < 0) {
//                return -1;
//            } else {
//                return 0;
//            }
//        }
//    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        if (!name.equals(node.name)) return false;
        return network.equals(node.network);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + network.hashCode();
        return result;
    }

    @Override
    public int compareTo(Object o) {
        Node n = (Node) o;
        return getName().compareTo(n.getName());
    }

    public void removeParent(Node parent) {
        parents.remove(getNodeWithName(parents, parent.getName()));
    }

    public void removeChild(Node child) {
        children.remove(getNodeWithName(children, child.getName()));
    }

    @Override
    public String toString() {
        return name;
    }
}
