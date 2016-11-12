package search;

import model.Edge;
import model.Network;

/**
 * Created by Benedek on 4/23/2016.
 * Encapsulates one move that can be made in the process of building a DAG network
 * Moves can be the following: type, deleting and reversing an edge
 */
public class Move implements Comparable {
    private Edge edge = null;
    private Double score = null;
    private Network network;
    private MoveType type;

    Move(Network myNetwork, Edge edge, MoveType add) {
        network = myNetwork;
        this.edge = edge;
        this.type = add;
    }

    public void setScoreToNull() {
        this.score = null;
    }

    double getScore() {
        if (score == null) {
            score = calculateScore();
        }
        return score;
    }

    /**
     * rates how well the move helps reconstructing the structure based on the data
     *
     * @return score of the move
     */
    double calculateScore() {
        BayesianScoring bayesianScoring = BayesianScoring.getInstance();
        bayesianScoring.setMove(this);
        bayesianScoring.setNetwork(network);


        double calculatedScore = bayesianScoring.calculateScoreOfMove();
        score = calculatedScore;
        return calculatedScore;
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
            if (type == m.getType()) {
                return 0;
            } else if ((type == MoveType.adding && (m.getType() == MoveType.deleting || m.getType() == MoveType.reversing)) ||
                    (type == MoveType.reversing && m.getType() == MoveType.deleting)) {
                // let's say adding > reversing > deleting
                return -1;
            } else {
                return 1;
            }
        }
    }

    Edge getEdge() {
        return edge;
    }

    /**
     * setter function for the edge field
     *
     * @param edge the desired edge
     */
    public void setEdge(Edge edge) {
        this.edge = edge;
    }

    MoveType getType() {
        return type;
    }

    @Override
    public int hashCode() {
        int result = edge != null ? edge.hashCode() : 0;
        result = 31 * result + (network != null ? network.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Move move = (Move) o;

        if (edge != null ? !edge.equals(move.edge) : move.edge != null) return false;
        if (network != null ? !network.equals(move.network) : move.network != null) return false;
        return type == move.type;

    }

    @Override
    public String toString() {
        return edge.getParent().getName() + " --> " + edge.getChild().getName() + ", " + this.getType();
    }


}
