package search;

import model.Edge;
import model.Network;
import model.Node;
import utils.GraphFunctions;

import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Benedek on 4/23/2016.
 * Encapsulates one move that can be made in the process of building a DAG network (adding a set of edges)
 * The class is equipped to represent adding multiple edges, though we will only be using this with a single edge
 */
public class Move {
    private Set<Edge> edges;
    private Node commonChild;
    private Double score = null;
    private Network network;
    private Network realNetwork;

    public Move(Network myNetwork, Network actualNetwork) {
        edges = new TreeSet<>();
        network = myNetwork;
        realNetwork = actualNetwork;
    }

    public Move(Network myNetwork, Network actualNetwork, Edge firstEdge) {
        network = myNetwork;
        realNetwork = actualNetwork;
        edges = new TreeSet<Edge>();
        edges.add(firstEdge);
        commonChild = firstEdge.getChild();
    }

    /**
     * adding the edge to the set of edges
     * throws IllegalArgumentException if the new edge has a different child than the others
     *
     * @param newEdge the new edge to be added
     */
    public void addEdge(Edge newEdge) {
        if (edges.size() == 0) {
            edges.add(newEdge);
        } else {
            if (newEdge.getChild() != commonChild) {
                throw new IllegalArgumentException("A move cannot have edges with different children.");
            } else {
                edges.add(newEdge);
            }
        }
    }

    /**
     * rates how well the move helps reconstructing the structure based on the data
     *
     * @return score of the move
     */
    public double calculateScore() {
        double calculatedScore = getDummyScore();
        score = calculatedScore;
        return calculatedScore;
    }

    private double getDummyScore() {
        double dummyScore = 0.0;

        Node realChild = GraphFunctions.getNodeWithName(realNetwork.getNodes(), edges.iterator().next().getChild().getName());
        Node newChild = GraphFunctions.getNodeWithName(network.getNodes(), edges.iterator().next().getChild().getName());

        Set<String> realParentNames = new TreeSet<>();
        if (realChild != null) {
            for (Node n : realChild.getParents()) {
                realParentNames.add(n.getName());
            }
        } else {
            throw new IllegalArgumentException("The real network structure and the new network structure has different nodes.");
        }

        Set<String> newParentNames = new TreeSet<>();
        newParentNames.add(edges.iterator().next().getParent().getName());
        if (newChild != null) {
            for (Node n : newChild.getParents()) {
                newParentNames.add(n.getName());
            }
        } else {
            throw new IllegalArgumentException("The real network structure and the new network structure has different nodes.");
        }

        Set<String> intersectionOfParentNames = new TreeSet<>(realParentNames);
        intersectionOfParentNames.retainAll(newParentNames);
        if (intersectionOfParentNames.size() == 0) {
            dummyScore = 0;
        }
        else {
            dummyScore = intersectionOfParentNames.size() / (Math.sqrt(newParentNames.size()) * Math.sqrt(realParentNames.size()));
        }


        return dummyScore;
    }

    public Set<Edge> getEdges() {
        return edges;
    }

    public Node getCommonChild() {
        return commonChild;
    }

    public double getScore() {
        if (score == null) {
            score = calculateScore();
        }
        return score;
    }
}
