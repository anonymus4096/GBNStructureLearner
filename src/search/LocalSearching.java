package search;

import model.Edge;
import model.Network;
import model.Node;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import static utils.GraphFunctions.containsEdge;

/**
 * Created by bfabian on 2016. 10. 03..
 */
public abstract class LocalSearching {
    protected Network network;
    protected Set<Move> possibleMoves;
    protected int maxNumberOfParents = 5;
    protected LinkedList<Move> lastMoves;
    protected int maxSize = 2;
    protected BayesianScoring bayesianScoring;
    protected int maxNumberOfSteps = 1000;
    protected boolean firstStep = true;

    public LocalSearching(Network network, int numberOfLinesToUse) {
        this.network = network;
        bayesianScoring = BayesianScoring.getInstance();
        bayesianScoring.setNumberOfLinesToUse(numberOfLinesToUse);
        bayesianScoring.initializeValues();
        possibleMoves = new HashSet<>();

    }

    public abstract void doSearch();

    protected void deleteInvalidMoves(Set<Move> possibleMoves) {
        Iterator<Move> iterator = possibleMoves.iterator();

        while (iterator.hasNext()) {
            Move move = iterator.next();
            if (move.getType() == MoveType.adding) {
                if (network.violatesDAG(move.getEdge().getParent(), move.getEdge().getChild())
                        || containsEdge(network.getEdges(), move.getEdge().getParent(), move.getEdge().getChild())) {
                    iterator.remove();
                }
            } else if (move.getType() == MoveType.reversing) {
                if (network.reversingViolatesDAG(move.getEdge().getParent(), move.getEdge().getChild())
                        || !containsEdge(network.getEdges(), move.getEdge().getParent(), move.getEdge().getChild())) {
                    iterator.remove();
                }
            } else {
                if (!containsEdge(network.getEdges(), move.getEdge().getParent(), move.getEdge().getChild())) {
                    iterator.remove();
                }
            }
        }
    }


    /**
     * finds all the possible edges, without messing up the DAG property
     *
     * @return set of edges
     */
    protected Set<Edge> calculatePossibleEdges() {
        Set<Edge> possibleEdges = new HashSet<>();
        for (Node parent : network.getNodes()) {
            for (Node child : network.getNodes()) {
                if (parent != child && child.getParents().size() < maxNumberOfParents &&
                        !containsEdge(network.getEdges(), parent, child) && !network.violatesDAG(parent, child)) {
                    possibleEdges.add(new Edge(network, parent, child));
                }
            }
        }
        return possibleEdges;
    }

    /**
     * makes the move given as a parameter - meaning it adds the new edges the move consist of
     *
     * @param bestMove move containing the edges to be added
     */
    protected void makeMove(Move bestMove) {
        if (bestMove.getType() == MoveType.adding) {
            network.addNewEdge(bestMove.getEdge().getParent(), bestMove.getEdge().getChild());
        } else if (bestMove.getType() == MoveType.deleting) {
            network.deleteEdge(bestMove.getEdge().getParent().getName(), bestMove.getEdge().getChild().getName());
        } else {
            network.reverseEdge(bestMove.getEdge().getParent().getName(), bestMove.getEdge().getChild().getName());
        }
    }

    /**
     * finds all possible moves to be made
     *
     * @return set of possible moves
     */
    protected Set<Move> calculatePossibleMoves() {
        Set<Move> moves = new HashSet<>();

        Set<Edge> possibleEdges = calculatePossibleEdges();
        for (Edge e : possibleEdges) {
            moves.add(new Move(network, e, MoveType.adding));
        }
        for (Edge e : network.getEdges()) {
            moves.add(new Move(network, e, MoveType.deleting));
        }

        for (Edge e : network.getEdges()) {
            moves.add(new Move(network, e, MoveType.reversing));
        }

        return moves;
    }

    protected boolean lastMovesContain(LinkedList<Move> lastMoves, Move m) {
        if (lastMoves.contains(m)) return true;
        if (lastMoves.contains(new Move(network, m.getEdge(), MoveType.deleting)) ||
                lastMoves.contains(new Move(network, m.getEdge(), MoveType.adding)) ||
                lastMoves.contains(new Move(network, m.getEdge().getReverse(), MoveType.reversing))) {
            return true;
        }

        return false;
    }

    protected Move findBestMove(Set<Move> possibleMoves, Set<Move> movesToRecalculate) {
        Move bestMove;

        Move bestOldMove = null;
        for (Move m : possibleMoves) {
            if (!lastMovesContain(lastMoves, m)) {
                if (bestOldMove == null) {
                    bestOldMove = m;
                } else {
                    if (m.getScore() > bestOldMove.getScore()) {
                        bestOldMove = m;
                    }
                }
            }
        }

        Move bestNewMove = null;
        for (Move m : movesToRecalculate) {
            if (!lastMovesContain(lastMoves, m)) {
                if (bestNewMove == null) {
                    bestNewMove = m;
                } else {
                    if (m.calculateScore() > bestNewMove.getScore()) {
                        bestNewMove = m;
                    }
                }
            }
        }

        if (bestNewMove == null) {
            bestMove = bestOldMove;
        } else if (bestOldMove == null) {
            bestMove = bestNewMove;
        } else {
            bestMove = bestNewMove.getScore() > bestOldMove.getScore() ? bestNewMove : bestOldMove;
        }

        return bestMove;
    }

}
