package search;

import model.Edge;
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

    public Move() {
        edges = new TreeSet<>();
    }

    public Move(Edge firstEdge){
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
        // TODO do some magic
        double calculatedScore = 0;
        score = calculatedScore;
        return calculatedScore;
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
