package search;

import model.Network;

import java.time.LocalTime;
import java.util.LinkedList;

/**
 * Created by Benedek on 4/23/2016.
 */
public class HillClimbing extends LocalSearching {

    /**
     * constructor
     *
     * @param network network object that contains all the nodes and edges already set
     */
    public HillClimbing(Network network, int numberOfLinesToUse, String fileName, int numberOfSteps, Double lambda) {
        super(network, numberOfLinesToUse, fileName, numberOfSteps, lambda);
    }

    /**
     * climbs hill, by making steps until there is no more steps to improve (when scoreBestMove <= 0)
     * or when there is no more edge to be set
     */
    public void doSearch() {
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


        } else {
            maintainingPossibleMoves(lastMoves.getLast(), possibleMoves, true);
        }
        if (possibleMoves.size() == 0) {
            return null;
        }


        for (Move m : possibleMoves) {
            if (!lastMovesContain(lastMoves, m) && !moveWouldCauseMoreParents(m, maxNumberOfParents)) {
                if (bestMove == null) {
                    bestMove = m;
                } else {
                    if (m.getScore() > bestMove.getScore()) {
                        bestMove = m;
                    }
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
            System.out.println(LocalTime.now() + ": Adding edge: " + bestMove.getEdge().getParent().getName() + " --> " + bestMove.getEdge().getChild().getName() + " : \t" + bestMove.getScore());
        } else if (bestMove.getType() == MoveType.deleting) {
            System.out.println(LocalTime.now() + ": Deleting edge: " + bestMove.getEdge().getParent().getName() + " --> " + bestMove.getEdge().getChild().getName() + " : \t" + bestMove.getScore());
        } else {
            System.out.println(LocalTime.now() + ": Reversing edge: " + bestMove.getEdge().getParent().getName() + " --> " + bestMove.getEdge().getChild().getName() + " : \t" + bestMove.getScore());
        }

        return bestMove.getScore();
    }


}
