package search;

import model.Edge;
import model.Network;
import model.Node;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import static utils.GraphFunctions.containsEdge;

/**
 * Created by Benedek on 4/23/2016.
 */
public class HillClimbing {
    private Network network;
    private Network realNetwork;
    private Set<Move> possibleMoves;
    private int maxNumberOfParents = 5;
    private LinkedList<Move> lastMoves;
    private int maxSize = 5;
    private BayesianScoring bayesianScoring;

    /**
     * constructor
     *
     * @param network network object that contains all the nodes and edges already set
     */
    public HillClimbing(Network network, Network realNetwork) {
        this.network = network;
        this.realNetwork = realNetwork;
        bayesianScoring = BayesianScoring.getInstance();
    }

    /**
     * climbs hill, by making steps until there is no more steps to improve (when scoreBestMove <= 0)
     * or when there is no more edge to be set
     */
    public void climbHill() {
        Double scoreBestMove = 0.0;

        lastMoves = new LinkedList<>();

        int numberOfSteps = 0;
        do {
            scoreBestMove = stepOne();
            numberOfSteps++;
        } while (scoreBestMove != null && scoreBestMove > 0 && numberOfSteps < 1000);
    }

    /**
     * looks at all possible moves, finds the best one and makes it
     *
     * @return returns the score of the best move
     */
    public Double stepOne() {
        possibleMoves = calculatePossibleMoves();
        if (possibleMoves.size() == 0) {
            return null;
        }

        Move bestMove = null;
        for (Move m : possibleMoves) {
            if (!lastMoves.contains(m)) {
                if (bestMove == null) {
                    bestMove = m;
                } else {
                    if (m.calculateScore() > bestMove.getScore()) {
                        bestMove = m;
                    }
                }
            }
        }

        if (bestMove == null) {
            return 0.0;
        }

        // if the FIFO is full, delete the first element
        if (lastMoves.size() == maxSize) {
            lastMoves.remove();
        }
        // then append the last move to the end of the queue
        lastMoves.add(bestMove);

        boolean before = network.isDAG();
        makeMove(bestMove);
        if (bestMove.isAdding()) {
            System.out.println("Added edge: " + bestMove.getEdge().getParent().getName() + " --> " + bestMove.getEdge().getChild().getName() + " : \t" + bestMove.getScore());
        } else {
            System.out.println("Deleting edge: " + bestMove.getEdge().getParent().getName() + " --> " + bestMove.getEdge().getChild().getName() + " : \t" + bestMove.getScore());
        }
        boolean after = network.isDAG();


        if (before != after) {
            if (before) {
                System.out.println("Last move messed up the DAG property.");
            } else {
                System.out.println("Last move restored DAG property.");
            }
        }


        return bestMove != null ? bestMove.getScore() : null;
    }


    /**
     * makes the move given as a parameter - meaning it adds the new edges the move consist of
     *
     * @param bestMove move containing the edges to be added
     */
    private void makeMove(Move bestMove) {
        if (bestMove.isAdding()) {
            network.addNewEdge(bestMove.getEdge().getParent(), bestMove.getEdge().getChild());
        } else {
            network.deleteEdge(bestMove.getEdge().getParent().getName(), bestMove.getEdge().getChild().getName());
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
            moves.add(new Move(network, realNetwork, e, true));
        }
        for (Edge e : network.getEdges()) {
            moves.add(new Move(network, realNetwork, e, false));
        }

        return moves;
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
                if (parent != child && child.getParents().size() <= maxNumberOfParents &&
                        !containsEdge(network.getEdges(), parent, child) && !network.violatesDAG(parent, child)) {
                    possibleEdges.add(new Edge(network, parent, child));
                }
            }
        }
        return possibleEdges;
    }

}
