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
public class Move implements Comparable {
    private Edge edge = null;
    private Double score = null;
    private Network network;
    private Network realNetwork;
    private boolean adding;
    private String dataFileName = "res/sample.0.data.csv";

    public Move(Network myNetwork, Network actualNetwork, boolean add) {
        network = myNetwork;
        realNetwork = actualNetwork;
        this.adding = add;
    }

    public Move(Network myNetwork, Network actualNetwork, Edge edge, boolean add) {
        network = myNetwork;
        realNetwork = actualNetwork;
        this.edge = edge;
        this.adding = add;
    }

    /**
     * setter function for the edge field
     *
     * @param edge the desired edge
     */
    public void setEdge(Edge edge) {
        this.edge = edge;
    }

    /**
     * rates how well the move helps reconstructing the structure based on the data
     *
     * @return score of the move
     */
    public double calculateScore() {
        //TODO just for debugging purposes
        BayesianScoring bayesianScoring = new BayesianScoring(this, network, dataFileName);


        double calculatedScore = bayesianScoring.calculateScoreOfMove();
        score = calculatedScore;
        return calculatedScore;
    }

    private double getDummyScore() {
        double dummyScore = 0.0;

        Node realChild = GraphFunctions.getNodeWithName(realNetwork.getNodes(), edge.getChild().getName());
        Node newChild = GraphFunctions.getNodeWithName(network.getNodes(), edge.getChild().getName());

        Set<String> realParentNames = new TreeSet<>();
        if (realChild != null) {
            for (Node n : realChild.getParents()) {
                realParentNames.add(n.getName());
            }
        } else {
            throw new IllegalArgumentException("The real network structure and the new network structure has different nodes.");
        }

        Set<String> newParentNames = new TreeSet<>();
        if (newChild != null) {
            for (Node n : newChild.getParents()) {
                newParentNames.add(n.getName());
            }
        } else {
            throw new IllegalArgumentException("The real network structure and the new network structure has different nodes.");
        }
        if (adding) {
            newParentNames.add(edge.getParent().getName());
        } else {
            newParentNames.remove(edge.getParent().getName());
        }

        Set<String> intersectionOfParentNames = new TreeSet<>(realParentNames);
        intersectionOfParentNames.retainAll(newParentNames);
        if (intersectionOfParentNames.size() == 0) {
            dummyScore = 0;
        } else {
            dummyScore = intersectionOfParentNames.size() / (Math.sqrt(newParentNames.size()) * Math.sqrt(realParentNames.size()));
        }


        return dummyScore;
    }

    public Edge getEdge() {
        return edge;
    }

    public double getScore() {
        if (score == null) {
            score = calculateScore();
        }
        return score;
    }

    public boolean isAdding() {
        return adding;
    }

    @Override
    public int compareTo(Object o) {
        Move m = (Move) o;
        Edge e = m.getEdge();
        if (edge.compareTo(e) > 0) {
            return 1;
        } else if (edge.compareTo(e) < 0) {
            return -1;
        } else {
            if (adding == m.isAdding()) {
                return 0;
            } else if (!adding && m.isAdding()) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this.getClass().equals(obj.getClass())) {
            Move m = (Move) obj;
            return edge.compareTo(m.edge) == 0;
        } else {
            return false;
        }
    }
}
