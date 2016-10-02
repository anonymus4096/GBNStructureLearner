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
 * Created by Benedek on 4/23/2016.
 */
public class HillClimbing {
    private Network network;
    private Set<Move> possibleMoves;
    private int maxNumberOfParents = 5;
    private LinkedList<Move> lastMoves;
    private int maxSize = 2;
    private BayesianScoring bayesianScoring;
    private int maxNumberOfSteps = 1000;
    private boolean firstStep = true;

    /**
     * constructor
     *
     * @param network network object that contains all the nodes and edges already set
     */
    public HillClimbing(Network network, int numberOfLinesToUse) {
        this.network = network;
        bayesianScoring = BayesianScoring.getInstance();
        bayesianScoring.setNumberOfLinesToUse(numberOfLinesToUse);
        bayesianScoring.initializeValues();
        possibleMoves = new HashSet<>();
    }

    /**
     * climbs hill, by making steps until there is no more steps to improve (when scoreBestMove <= 0)
     * or when there is no more edge to be set
     */
    public void climbHill() {
        long startTime = System.nanoTime();
        Double scoreBestMove;

        lastMoves = new LinkedList<>();

        int numberOfSteps = 0;
        do {
            scoreBestMove = stepOne();
            numberOfSteps++;
        } while (scoreBestMove != null && scoreBestMove > 0 && numberOfSteps < maxNumberOfSteps);

        long elapsedTime = System.nanoTime() - startTime;
        System.out.println("The algorithm took " + elapsedTime / 1000000000.0 + " seconds to finish, while making " + numberOfSteps + " steps.");
    }

    /**
     * looks at all possible moves, finds the best one and makes it
     *
     * @return returns the score of the best move
     */
    public Double stepOne() {
        Move bestMove = null;
        if (firstStep) {
            // if it's the first step, collect all possible moves
            firstStep = false;
            possibleMoves = calculatePossibleMoves();
            System.out.println("Number of possible moves to make: " + possibleMoves.size());
            if (possibleMoves.size() == 0) {
                return null;
            }

            for (Move m : possibleMoves) {
                if (bestMove == null) {
                    bestMove = m;
                } else {
                    if (m.calculateScore() > bestMove.getScore()) {
                        bestMove = m;
                    }
                }
            }
        } else {
            // if it's not the first step, try to minimize the amount of calculation
            Set<Move> lastPossibleMoves = new HashSet<>(possibleMoves);
            Set<Move> movesToRecalculate = new HashSet<>();
            Move lastMove = lastMoves.getLast();
            if (lastMove.getType() == MoveType.deleting) {
                //ADDING MOVES


                // if we deleted the last time, add the moves that are possible again
                Set<Node> descendants = lastMove.getEdge().getChild().getDescendants();
                Set<Node> ancestors = lastMove.getEdge().getParent().getAncestors();

                descendants.add(lastMove.getEdge().getChild());
                ancestors.add(lastMove.getEdge().getParent());

                // for all of the edges, where the child is the same as the child of the last edge that was deleted,
                // recalculate their score
                for (Move m : lastPossibleMoves) {
                    if (m.getEdge().getChild() == lastMove.getEdge().getChild()) {
                        movesToRecalculate.add(m);
                    }
                }

                // add every move, that does not violate the DAG condition anymore because of the deleting
                for (Node parent : ancestors) {
                    for (Node child : descendants) {
                        if (parent != child && !network.violatesDAG(child, parent)) {
                            movesToRecalculate.add(new Move(network, new Edge(network, child, parent), MoveType.adding));
                        }
                    }
                }

                // REMOVING MOVES
                deleteInvalidMoves(lastPossibleMoves);
                deleteInvalidMoves(movesToRecalculate);

                // CALCULATE WHAT WE NEED, AND FIND THE BEST MOVE
                lastPossibleMoves.removeAll(movesToRecalculate);
                movesToRecalculate.remove(lastMove);
                possibleMoves = lastPossibleMoves;

                // find the best move from the last turn
                Move bestOldMove = null;
                for (Move m : possibleMoves) {
                    // TODO add condition to not allow trying for last moves even with different move types
                    if (!lastMoves.contains(m) || !lastMoves.contains(new Move(network, m.getEdge().getReverse(), MoveType.reversing))) {
                        if (bestOldMove == null) {
                            bestOldMove = m;
                        } else {
                            if (m.getScore() > bestOldMove.getScore()) {
                                bestOldMove = m;
                            }
                        }
                    }
                }

                // find the new best move, that was not possible last turn
                Move bestNewMove = null;
                for (Move m : movesToRecalculate) {
                    if (!lastMoves.contains(m)) {
                        if (bestNewMove == null) {
                            bestNewMove = m;
                        } else {
                            if (m.calculateScore() > bestNewMove.getScore()) {
                                bestNewMove = m;
                            }
                        }
                    }
                }

                possibleMoves.addAll(movesToRecalculate);

                // find the move with the highest score
                if (bestNewMove == null) {
                    bestMove = bestOldMove;
                } else if (bestOldMove == null) {
                    bestMove = bestNewMove;
                } else {
                    bestMove = bestNewMove.getScore() > bestOldMove.getScore() ? bestNewMove : bestOldMove;
                }

            } else if (lastMove.getType() == MoveType.adding) {
                // DELETING MOVES
                deleteInvalidMoves(lastPossibleMoves);

                // ADDING MOVES

                for (Move move : lastPossibleMoves) {
                    if (move.getType() == MoveType.reversing &&
                            (move.getEdge().getChild() == lastMove.getEdge().getChild()
                                    || move.getEdge().getParent() == lastMove.getEdge().getChild())) {
                        movesToRecalculate.add(move);
                    }
                }
                if (!network.reversingViolatesDAG(lastMove.getEdge().getParent(), lastMove.getEdge().getChild())) {
                    movesToRecalculate.add(new Move(network, lastMove.getEdge(), MoveType.reversing));
                }
                movesToRecalculate.add(new Move(network, lastMove.getEdge(), MoveType.deleting));

                for (Move m : lastPossibleMoves) {
                    if (m.getEdge().getChild() == lastMove.getEdge().getChild()) {
                        movesToRecalculate.add(m);
                    }
                }
                lastPossibleMoves.removeAll(movesToRecalculate);
                possibleMoves = lastPossibleMoves;

                //for (Move move : lastPossibleMoves) System.out.println(move.toString());
                //System.out.println();
                //for (Move move : movesToRecalculate) System.out.println(move.toString());

                // CALCULATE WHAT WE NEED, AND FIND THE BEST MOVE
                Move bestOldMove = null;
                for (Move m : possibleMoves) {
                    if (!lastMoves.contains(m)) {
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
                    if (!lastMoves.contains(m)) {
                        if (bestNewMove == null) {
                            bestNewMove = m;
                        } else {
                            if (m.calculateScore() > bestNewMove.getScore()) {
                                bestNewMove = m;
                            }
                        }
                    }
                }

                possibleMoves.addAll(movesToRecalculate);
                if (bestNewMove == null) {
                    bestMove = bestOldMove;
                } else if (bestOldMove == null) {
                    bestMove = bestNewMove;
                } else {
                    bestMove = bestNewMove.getScore() > bestOldMove.getScore() ? bestNewMove : bestOldMove;
                }
            } else {
                // if the last move reversed the edge

                // DELETING MOVES
                deleteInvalidMoves(lastPossibleMoves);

                // ADDING MOVES
                if (!network.reversingViolatesDAG(lastMove.getEdge().getReverse().getParent(), lastMove.getEdge().getReverse().getChild())) {
                    movesToRecalculate.add(new Move(network, lastMove.getEdge().getReverse(), MoveType.reversing));
                }
                movesToRecalculate.add(new Move(network, lastMove.getEdge().getReverse(), MoveType.deleting));

                for (Move m : lastPossibleMoves) {
                    if (m.getEdge().getChild() == lastMove.getEdge().getReverse().getChild()) {
                        movesToRecalculate.add(m);
                    }
                }
                lastPossibleMoves.removeAll(movesToRecalculate);
                possibleMoves = lastPossibleMoves;

                // CALCULATE WHAT WE NEED, AND FIND THE BEST MOVE
                Move bestOldMove = null;
                for (Move m : possibleMoves) {
                    if (!lastMoves.contains(m)) {
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
                    if (!lastMoves.contains(m)) {
                        if (bestNewMove == null) {
                            bestNewMove = m;
                        } else {
                            if (m.calculateScore() > bestNewMove.getScore()) {
                                bestNewMove = m;
                            }
                        }
                    }
                }

                possibleMoves.addAll(movesToRecalculate);
                if (bestNewMove == null) {
                    bestMove = bestOldMove;
                } else if (bestOldMove == null) {
                    bestMove = bestNewMove;
                } else {
                    bestMove = bestNewMove.getScore() > bestOldMove.getScore() ? bestNewMove : bestOldMove;
                }
            }
        }

        if (bestMove == null || bestMove.getScore() < 0.0) {
            return 0.0;
        }

        // if the FIFO is full, delete the first element
        if (lastMoves.size() == maxSize) {
            lastMoves.remove();
        }
        // then append the last move to the end of the queue
        lastMoves.add(bestMove);

        makeMove(bestMove);
        if (bestMove.getType() == MoveType.adding) {
            System.out.println("Added edge: " + bestMove.getEdge().getParent().getName() + " --> " + bestMove.getEdge().getChild().getName() + " : \t" + bestMove.getScore());
        } else if (bestMove.getType() == MoveType.deleting) {
            System.out.println("Deleting edge: " + bestMove.getEdge().getParent().getName() + " --> " + bestMove.getEdge().getChild().getName() + " : \t" + bestMove.getScore());
        } else {
            System.out.println("Reversing edge: " + bestMove.getEdge().getParent().getName() + " --> " + bestMove.getEdge().getChild().getName() + " : \t" + bestMove.getScore());
        }

        return bestMove.getScore();
    }

    private void deleteInvalidMoves(Set<Move> possibleMoves) {
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
     * finds all possible moves to be made
     *
     * @return set of possible moves
     */
    private Set<Move> calculatePossibleMoves() {
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

    /**
     * makes the move given as a parameter - meaning it adds the new edges the move consist of
     *
     * @param bestMove move containing the edges to be added
     */
    private void makeMove(Move bestMove) {
        if (bestMove.getType() == MoveType.adding) {
            network.addNewEdge(bestMove.getEdge().getParent(), bestMove.getEdge().getChild());
        } else if (bestMove.getType() == MoveType.deleting) {
            network.deleteEdge(bestMove.getEdge().getParent().getName(), bestMove.getEdge().getChild().getName());
        } else {
            network.reverseEdge(bestMove.getEdge().getParent().getName(), bestMove.getEdge().getChild().getName());
        }
    }

    /**
     * finds all the possible edges, without messing up the DAG property
     *
     * @return set of edges
     */
    private Set<Edge> calculatePossibleEdges() {
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

}
