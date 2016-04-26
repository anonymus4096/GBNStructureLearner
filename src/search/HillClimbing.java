package search;

import model.Edge;
import model.Network;
import model.Node;

import java.util.HashSet;
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

    /**
     * constructor
     * @param network network object that contains all the nodes and edges already set
     */
    public HillClimbing(Network network, Network realNetwork) {
        this.network = network;
        this.realNetwork = realNetwork;
    }

    /**
     * looks at all possible moves, finds the best one and makes it
     * @return returns the score of the best move
     */
    public Double stepOne(){
        possibleMoves = calculatePossibleMoves();
        if (possibleMoves.size() == 0){
            return null;
        }

        Move bestMove = null;
        for (Move m : possibleMoves) {
            if (bestMove == null) {
                bestMove = m;
            } else {
                if (m.calculateScore() > bestMove.getScore()) {
                    bestMove = m;
                }
            }
        }
        makeMove(bestMove);

        return bestMove != null ? bestMove.getScore() : null;
    }

    /**
     * climbs hill, by making steps until there is no more steps to improve (when scoreBestMove <= 0)
     * or when there is no more edge to be set
     */
    public void climbHill(){
        Double scoreBestMove = 1.0;
        while (scoreBestMove != null && scoreBestMove > 0.5){
            scoreBestMove = stepOne();
        }
    }



    /**
     * makes the move given as a parameter - meaning it adds the new edges the move consist of
     * @param bestMove move containing the edges to be added
     */
    private void makeMove(Move bestMove) {
        for (Edge e : bestMove.getEdges()){
            network.addNewEdge(e.getParent(), e.getChild());
        }
    }

    /**
     * finds all possible moves to be made
     * @return set of possible moves
     */
    private Set<Move> calculatePossibleMoves(){
        Set<Move> moves = new HashSet<>();

        Set<Edge> possibleEdges = calculatePossibleEdges();
        for (Edge e : possibleEdges) {
            moves.add(new Move(network, realNetwork, e));
        }

//        HashMap<Node, TreeSet<Edge>> edgesConnectedToChildren = separateEdgesByChildren(possibleEdges);

        return moves;
    }

//    private HashMap<Node, TreeSet<Edge>> separateEdgesByChildren(TreeSet<Edge> edges) {
//        HashMap<Node, TreeSet<Edge>> edgesConnectedToChildren = new HashMap<>();
//        for (Edge e : edges){
//            if (edgesConnectedToChildren.get(e.getChild()) == null){
//                TreeSet<Edge> temp = new TreeSet<>();
//                temp.add(e);
//                edgesConnectedToChildren.put(e.getChild(), temp);
//            } else {
//                edgesConnectedToChildren.get(e.getChild()).add(e);
//            }
//        }
//
//        return edgesConnectedToChildren;
//    }

    /**
     * finds all the possible edges, without messing up the DAG property
     * @return set of edges
     */
    private Set<Edge> calculatePossibleEdges() {
        Set<Edge> possibleEdges = new HashSet<>();
        for (Node parent : network.getNodes()){
            for (Node child : network.getNodes()){
                if (parent != child && child.getParents().size() <= maxNumberOfParents && !containsEdge(network.getEdges(), parent, child) && !network.violatesDAG(parent, child)){
                    possibleEdges.add(new Edge(network, parent, child));
                }
            }
        }
        return possibleEdges;
    }

}
