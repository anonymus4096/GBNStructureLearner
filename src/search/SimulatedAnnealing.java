package search;

import model.Edge;
import model.Network;
import model.Node;
import utils.GraphFunctions;

import java.time.LocalTime;
import java.util.*;

/**
 * Created by bfabian on 2016. 10. 02..
 */
public class SimulatedAnnealing extends LocalSearching {
    Double Tmax = 10000.0;
    Double Tmin = 0.0;
    Random random = new Random();
    int numberOfTries = 0;
    int maxNumberOfTries = (int) Math.pow(network.getNodes().size(), 2);
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
        List<Double> scores = new ArrayList<>();

        Double probAcceptance = 0.5;
        Double percentile = 0.99;

        int i = 0;
        for (Move move : possibleMoves) {
            if (i == 1000) break;

            move.setScoreToNull();
            if (move.getScore() < 0
                    && !Double.isInfinite(move.getScore())) {
                i++;
                scores.add(Math.abs(move.getScore()));
            }
        }
        Collections.sort(scores);
        Double threshold = scores.get((int) (scores.size() * percentile));


        return -1 * threshold / Math.log(probAcceptance);
    }

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
                    move = new Move(network, new Edge(network, parent, child), MoveType.deleting, lambda);
                } else {
                    if (parent.getParents().size() < maxNumberOfParents && !network.reversingViolatesDAG(parent, child)) {
                        move = new Move(network, new Edge(network, parent, child), MoveType.reversing, lambda);
                    }
                }
            } else {
                if (child.getParents().size() < maxNumberOfParents && !network.violatesDAG(parent, child)) {
                    move = new Move(network, new Edge(network, parent, child), MoveType.adding, lambda);
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
