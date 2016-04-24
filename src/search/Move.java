package search;

import model.Edge;
import model.Network;
import model.Node;

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

    public Move(Network network) {
        edges = new TreeSet<>();
        this.network = network;
    }

    public Move(Network network, Edge firstEdge){
        this.network = network;
        edges = new TreeSet<Edge>();
        edges.add(firstEdge);
        commonChild = firstEdge.getChild();
    }

    /**
     * adding the edge to the set of edges
     * throws IllegalArgumentException if the new edge has a different child than the others
     * @param newEdge the new edge to be added
     */
    public void addEdge(Edge newEdge){
        if (edges.size() == 0){
            edges.add(newEdge);
        } else {
            if (newEdge.getChild() != commonChild){
                throw new IllegalArgumentException("A move cannot have edges with different children.");
            } else {
                edges.add(newEdge);
            }
        }
    }

    /**
     * rates how well the move helps reconstructing the structure based on the data
     * @return score of the move
     */
    public double calculateScore(){
        double calculatedScore = getDummyScore();
        score = calculatedScore;
        return calculatedScore;
    }

    private double getDummyScore() {
        double dummyScore = 0.0;
        // TODO
        return dummyScore;
    }

    public Set<Edge> getEdges() {
        return edges;
    }

    public Node getCommonChild() {
        return commonChild;
    }

    public double getScore() {
        if (score == null){
            score = calculateScore();
        }
        return score;
    }
}
