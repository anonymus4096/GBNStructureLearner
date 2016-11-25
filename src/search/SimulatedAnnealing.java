package search;

import model.Edge;
import model.Network;
import model.Node;
import utils.GraphFunctions;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

/**
 * Created by bfabian on 2016. 10. 02..
 */
public class SimulatedAnnealing extends LocalSearching {
    Double Tmax = 10000.0;
    Double Tmin = 0.0;
    Random random = new Random();
    int numberOfTries = 0;
    int maxNumberOfTries = (int) Math.pow(network.getNodes().size(), 2.5);
    Set<Move> tempMoves = new HashSet<>();

    public SimulatedAnnealing(Network network, int numberOfLinesToUse, String fileName, int numberOfSteps, Double lambda) {
        super(network, numberOfLinesToUse, fileName, numberOfSteps, lambda);
    }

    @Override
    public void doSearch() {
        long startTime = System.nanoTime();
        Double lastScore;
        Double T = Tmax;
        Double Tstep = (Tmax - Tmin) / (double) maxNumberOfSteps;

        lastMoves = new LinkedList<>();

        int numberOfSteps = 0;
        do {
            if (numberOfSteps == maxNumberOfSteps / 100) {
                possibleMoves = calculatePossibleMoves();
                Tmax = calculateIdealTemperature(possibleMoves);
                T = Tmax;
                Tstep = (Tmax - Tmin) / (double) (maxNumberOfSteps - numberOfSteps);
                System.out.println("The new max T is: " + Tmax);
            }
            if (numberOfSteps % 100 == 0) {
                System.out.println("We have completed " + numberOfSteps + " steps. There are " + (maxNumberOfSteps - numberOfSteps) + " steps left.");
            }

            lastScore = stepOne(T);
            numberOfSteps++;
            T -= Tstep;
        } while (lastScore != null && numberOfSteps < maxNumberOfSteps && T > Tmin);

        long elapsedTime = System.nanoTime() - startTime;
        System.out.println("The algorithm took " + elapsedTime / 1000000000.0 + " seconds to finish, while making " + numberOfSteps + " steps.");

    }

    private Double calculateIdealTemperature(Set<Move> possibleMoves) {
        double acc = 0;
        int i = 0;
        for (Move move : possibleMoves) {
            if (i == 1000) break;

            move.setScoreToNull();
            if (move.getScore() < 0
                    && !Double.isInfinite(move.getScore())) {
                i++;
                acc += Math.pow(move.getScore(), 2);
            }
        }
        acc /= Math.min(possibleMoves.size(), i);
        acc = Math.sqrt(acc);

        return acc;
    }

/*    protected Double stepOne(Double T) {
        if (possibleMoves == null || possibleMoves.size() == 0) {
            possibleMoves = calculatePossibleMoves();
        } else {
            maintainingPossibleMoves(lastMoves.getLast(), possibleMoves, false);

//            Set<Move> temp1 = calculatePossibleMoves();
//            Set<Move> temp3 = new HashSet<>(temp1);
//            temp1.removeAll(possibleMoves);
//            Set<Move> temp2 = new HashSet<>(possibleMoves);
//            temp2.removeAll(temp3);
//            System.out.println("Should have found: " + temp1);
//            System.out.println("Shouldn't have found: " + temp2);
        }
        if (possibleMoves.size() == 0) {
            return null;
        }

        Move nextMove;
        int size = possibleMoves.size();

        for (int i = 0; i < size; i++) {

            nextMove = getRandomElementFromSet(possibleMoves);

            if (nextMove == null) {
                return 0.0;
            }
            Double prob = Math.exp(nextMove.calculateScore() / T);

            if ((nextMove.getScore() > 0 ||
                    (nextMove.getScore() < 0 &&
                            random.nextDouble() < prob))
                    && !moveWouldCauseMoreParents(nextMove, maxNumberOfParents)){
                // if the FIFO is full, delete the first element
                if (lastMoves.size() == maxSize) {
                    lastMoves.remove();
                }
                // then append the last move to the end of the queue
                lastMoves.add(nextMove);

                makeMove(nextMove);
                if (nextMove.getType() == MoveType.adding) {
                    System.out.println(LocalTime.now() + ": Added edge: " + nextMove.getEdge().getParent().getName() + " --> " + nextMove.getEdge().getChild().getName() + " : \t" + nextMove.getScore());
                } else if (nextMove.getType() == MoveType.deleting) {
                    System.out.println(LocalTime.now() + ": Deleting edge: " + nextMove.getEdge().getParent().getName() + " --> " + nextMove.getEdge().getChild().getName() + " : \t" + nextMove.getScore());
                } else {
                    System.out.println(LocalTime.now() + ": Reversing edge: " + nextMove.getEdge().getParent().getName() + " --> " + nextMove.getEdge().getChild().getName() + " : \t" + nextMove.getScore());
                }

                //System.out.println(network.isDAG());

                possibleMoves.addAll(tempMoves);
                tempMoves.clear();
                return nextMove.getScore();
            } else{
                tempMoves.add(nextMove);
                possibleMoves.remove(nextMove);
            }
        }
        return null;
    }
*/

    protected Double stepOne(Double T) {
        Move nextMove;
        while (numberOfTries < maxNumberOfTries) {
            nextMove = getRandomMove();

            nextMove.setScoreToNull();
            Double prob = Math.exp(nextMove.getScore() / T);

            if ((nextMove.getScore() > 0 ||
                    (nextMove.getScore() < 0 && random.nextDouble() < prob))) {
                // if the FIFO is full, delete the first element
                if (lastMoves.size() == maxSize) {
                    lastMoves.remove();
                }
                // then append the last move to the end of the queue
                lastMoves.add(nextMove);

                makeMove(nextMove);
                if (nextMove.getType() == MoveType.adding) {
                    System.out.println(LocalTime.now() + ": Adding edge: " + nextMove.getEdge().getParent().getName() + " --> " + nextMove.getEdge().getChild().getName() + " : \t" + nextMove.getScore());
                } else if (nextMove.getType() == MoveType.deleting) {
                    System.out.println(LocalTime.now() + ": Deleting edge: " + nextMove.getEdge().getParent().getName() + " --> " + nextMove.getEdge().getChild().getName() + " : \t" + nextMove.getScore());
                } else {
                    System.out.println(LocalTime.now() + ": Reversing edge: " + nextMove.getEdge().getParent().getName() + " --> " + nextMove.getEdge().getChild().getName() + " : \t" + nextMove.getScore());
                }

                numberOfTries = 0;
                return nextMove.getScore();
            }
            numberOfTries++;
        }
        return null;
    }

    private Move getRandomMove() {
        Move move = null;
        Node parent, child;
        while (move == null) {
            parent = getRandomElementFromSet(network.getNodes());
            child = getRandomElementFromSet(network.getNodes());
            if (parent == child) continue;

            if (GraphFunctions.containsEdge(network.getEdges(), parent, child)) {
                if (random.nextDouble() > 0.5) {
                    move = new Move(network, new Edge(network, parent, child), MoveType.deleting);
                } else {
                    if (parent.getParents().size() < maxNumberOfParents && !network.reversingViolatesDAG(parent, child)) {
                        move = new Move(network, new Edge(network, parent, child), MoveType.reversing);
                    }
                }
            } else {
                if (child.getParents().size() < maxNumberOfParents && !network.violatesDAG(parent, child)) {
                    move = new Move(network, new Edge(network, parent, child), MoveType.adding);
                }
            }

            if (move != null && lastMovesContain(lastMoves, move)) {
                move = null;
                continue;
            }
        }
        return move;
    }

    private <T extends Object> T getRandomElementFromSet(Set<T> set) {
        Random rnd = new Random();
        int i = rnd.nextInt(set.size());
        return (T) set.toArray()[i];
    }
}
