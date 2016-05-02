package utils;

import model.Edge;
import model.Node;

import java.util.Set;

/**
 * Created by Benedek on 4/23/2016.
 */
public class GraphFunctions {
    public static Node getNodeWithName(Set<Node> nodes, String name) {
        for (Node n : nodes) {
            if (n.getName().equals(name)) {
                return n;
            }
        }
        return null;
    }

    public static boolean containsNodeWithName(Set<Node> nodes, String name) {
        for (Node n : nodes) {
            if (n.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }


    public static boolean containsEdge(Set<Edge> edges, Node parent, Node child) {
        for (Edge e : edges) {
            if (e.getChild() == child && e.getParent() == parent) {
                return true;
            }
        }
        return false;
    }

    public static Edge getEdge(Set<Edge> edges, Node parent, Node child) {
        for (Edge e : edges) {
            if (e.getChild() == child && e.getParent() == parent) {
                return e;
            }
        }
        return null;
    }
}
