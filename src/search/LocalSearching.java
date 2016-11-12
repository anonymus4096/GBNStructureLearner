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
    protected int maxNumberOfParents = 100;
    protected LinkedList<Move> lastMoves;
    protected int maxSize = 50;
    protected BayesianScoring bayesianScoring;
    protected int maxNumberOfSteps = 100000;
    protected boolean firstStep = true;

    public LocalSearching(Network network, int numberOfLinesToUse, String fileName, int numberOfSteps, Double lambda) {
        this.network = network;
        bayesianScoring = BayesianScoring.getInstance();
        bayesianScoring.setFileName(fileName);
        bayesianScoring.setNumberOfLinesToUse(numberOfLinesToUse);
        bayesianScoring.initializeValues();
        bayesianScoring.setLambda(lambda);
        maxNumberOfSteps = numberOfSteps;
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

        // to dodge ConcurrentModificationExceptions
        Set<Edge> tempEdges = new HashSet<>(network.getEdges());
        for (Edge e : tempEdges) {
            if (!network.reversingViolatesDAG(e.getParent(), e.getChild())) {
                moves.add(new Move(network, e, MoveType.reversing));
            }
        }

        return moves;
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
                    if (m.getScore() > bestNewMove.getScore()) {
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

    protected boolean lastMovesContain(LinkedList<Move> lastMoves, Move m) {
        if (lastMoves.contains(m)) return true;
        if (lastMoves.contains(new Move(network, m.getEdge(), MoveType.deleting)) ||
                lastMoves.contains(new Move(network, m.getEdge(), MoveType.adding)) ||
                lastMoves.contains(new Move(network, m.getEdge().getReverse(), MoveType.reversing))) {
            return true;
        }

        return false;
    }

    protected void maintainingPossibleMoves(Move lastMove, Set<Move> possibleMoves, boolean needToRecalculate) {
        Set<Move> newPossibleMoves = new HashSet<>();
        if (lastMove.getType() == MoveType.adding) {
            // deleting
            possibleMoves.remove(lastMove);

            Set<Node> descendants = lastMove.getEdge().getChild().getDescendants();
            Set<Node> ancestors = lastMove.getEdge().getParent().getAncestors();
            descendants.add(lastMove.getEdge().getChild());
            ancestors.add(lastMove.getEdge().getParent());

            for (Node parent : ancestors) {
                for (Node child : descendants) {
                    if (network.violatesDAG(child, parent)) {
                        possibleMoves.remove(new Move(network, new Edge(network, child, parent), MoveType.adding));
                    }
                    if (network.reversingViolatesDAG(parent, child)) {
                        possibleMoves.remove(new Move(network, new Edge(network, parent, child), MoveType.reversing));
                    }
                }
            }

            // adding
            newPossibleMoves.add(new Move(network,
                    new Edge(network, lastMove.getEdge().getParent(), lastMove.getEdge().getChild()),
                    MoveType.deleting));
            if (!network.reversingViolatesDAG(lastMove.getEdge().getParent(), lastMove.getEdge().getChild())) {
                newPossibleMoves.add(new Move(network,
                        new Edge(network, lastMove.getEdge().getParent(), lastMove.getEdge().getChild()),
                        MoveType.reversing));
            }
            // finding moves to be calculated
            for (Move move : possibleMoves) {
                if (move.getType() == MoveType.reversing) {
                    if (move.getEdge().getParent() == lastMove.getEdge().getChild()) {
                        newPossibleMoves.add(move);
                    }
                } else {
                    if (move.getEdge().getChild() == lastMove.getEdge().getChild()) {
                        newPossibleMoves.add(move);
                    }
                }
            }

        } else if (lastMove.getType() == MoveType.deleting) {
            // deleting
            possibleMoves.remove(lastMove);
            possibleMoves.remove(new Move(network, lastMove.getEdge(), MoveType.reversing));

            // adding
            Set<Node> descendants = lastMove.getEdge().getChild().getDescendants();
            Set<Node> ancestors = lastMove.getEdge().getParent().getAncestors();
            descendants.add(lastMove.getEdge().getChild());
            ancestors.add(lastMove.getEdge().getParent());

            for (Node parent : ancestors) {
                for (Node child : descendants) {
                    if (parent != child && !network.violatesDAG(child, parent)) {
                        newPossibleMoves.add(new Move(network, new Edge(network, child, parent), MoveType.adding));
                    }

                    if (containsEdge(network.getEdges(), parent, child) && !network.reversingViolatesDAG(parent, child)) {
                        newPossibleMoves.add(new Move(network, new Edge(network, parent, child), MoveType.reversing));
                    }
                }
            }

            newPossibleMoves.add(new Move(network, new Edge(network, lastMove.getEdge().getParent(), lastMove.getEdge().getChild()), MoveType.adding));

            // finding moves to be calculated
            for (Move move : possibleMoves) {
                if (move.getType() == MoveType.reversing) {
                    if (move.getEdge().getParent() == lastMove.getEdge().getChild()) {
                        newPossibleMoves.add(move);
                    }
                } else {
                    if (move.getEdge().getChild() == lastMove.getEdge().getChild()) {
                        newPossibleMoves.add(move);
                    }
                }
            }

        } else {
            // deleting
            possibleMoves.remove(lastMove);
            possibleMoves.remove(new Move(network, lastMove.getEdge(), MoveType.deleting));

            // here the child and the parent roles are reversed
            Set<Node> newDescendants = lastMove.getEdge().getParent().getDescendants();
            Set<Node> newAncestors = lastMove.getEdge().getChild().getAncestors();
            newDescendants.add(lastMove.getEdge().getParent());
            newAncestors.add(lastMove.getEdge().getChild());

            for (Node parent : newAncestors) {
                for (Node child : newDescendants) {
                    possibleMoves.remove(new Move(network, new Edge(network, child, parent), MoveType.adding));
                    if (network.reversingViolatesDAG(parent, child)) {
                        possibleMoves.remove(new Move(network, new Edge(network, parent, child), MoveType.reversing));
                    }
                }
            }

            // adding
            newPossibleMoves.add(new Move(network, lastMove.getEdge().getReverse(), MoveType.deleting));

            Set<Node> descendants = lastMove.getEdge().getChild().getDescendants();
            Set<Node> ancestors = lastMove.getEdge().getParent().getAncestors();
            descendants.add(lastMove.getEdge().getChild());
            ancestors.add(lastMove.getEdge().getParent());

            for (Node parent : ancestors) {
                for (Node child : descendants) {
                    if (parent != child && !containsEdge(network.getEdges(), child, parent) && !network.violatesDAG(child, parent)) {
                        newPossibleMoves.add(new Move(network, new Edge(network, child, parent), MoveType.adding));
                    }

                    //if (containsEdge(network.getEdges(), child, parent) && !network.reversingViolatesDAG(child, parent)) {
                    //    newPossibleMoves.add(new Move(network, new Edge(network, child, parent), MoveType.reversing));
                    //}
                    if (containsEdge(network.getEdges(), parent, child) && !network.reversingViolatesDAG(parent, child)) {
                        newPossibleMoves.add(new Move(network, new Edge(network, parent, child), MoveType.reversing));
                    }

                }
            }

            Set<Edge> tempEdges = new HashSet<>(network.getEdges());
            for (Edge edge : tempEdges) {
                if (edge.getChild() == lastMove.getEdge().getChild()
                        || edge.getParent() == lastMove.getEdge().getParent()) {
                    if (!network.reversingViolatesDAG(edge.getParent(), edge.getChild())) {
                        newPossibleMoves.add(new Move(network, edge, MoveType.reversing));
                    }
                }
            }

            // finding moves to be calculated
            for (Move move : possibleMoves) {
                if (move.getType() == MoveType.reversing) {
                    if (move.getEdge().getParent() == lastMove.getEdge().getChild()
                            || move.getEdge().getParent() == lastMove.getEdge().getParent()) {
                        newPossibleMoves.add(move);
                    }
                } else {
                    if (move.getEdge().getChild() == lastMove.getEdge().getChild()
                            || move.getEdge().getChild() == lastMove.getEdge().getParent()) {
                        newPossibleMoves.add(move);
                    }
                }
            }

        }

        if (needToRecalculate) {
            for (Move move : newPossibleMoves) {
                move.setScoreToNull();
            }
            possibleMoves.removeAll(newPossibleMoves);
            possibleMoves.addAll(newPossibleMoves);
        } else {
            possibleMoves.removeAll(newPossibleMoves);
            possibleMoves.addAll(newPossibleMoves);
        }
    }

    protected boolean moveWouldCauseMoreParents(Move nextMove, int maxNumberOfParents) {
        switch (nextMove.getType()) {
            case adding:
                if (nextMove.getEdge().getChild().getParents().size() >= maxNumberOfParents) {
                    return true;
                }
                break;
            case reversing:
                if (nextMove.getEdge().getParent().getParents().size() >= maxNumberOfParents) {
                    return true;
                }
                break;
            default:
                return false;
        }
        return false;
    }
}
