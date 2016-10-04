package search;

import model.Network;

import java.time.LocalTime;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

/**
 * Created by bfabian on 2016. 10. 02..
 */
public class SimulatedAnnealing extends LocalSearching {
    Double Tmax = 100.0;
    Double Tmin = 0.0;
    Random random = new Random();

    public SimulatedAnnealing(Network network, int numberOfLinesToUse) {
        super(network, numberOfLinesToUse);
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
            lastScore = stepOne(T);
            numberOfSteps++;
            T -= Tstep;
        } while (lastScore != null && numberOfSteps < maxNumberOfSteps && T > Tmin);

        long elapsedTime = System.nanoTime() - startTime;
        System.out.println("The algorithm took " + elapsedTime / 1000000000.0 + " seconds to finish, while making " + numberOfSteps + " steps.");

    }

    protected Double stepOne(Double T) {

        possibleMoves = calculatePossibleMoves();
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

            if (nextMove.getScore() > 0 ||
                    (nextMove.getScore() < 0 &&
                            random.nextDouble() < prob)) {
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
                return nextMove.getScore();
            } else {
                possibleMoves.remove(nextMove);
            }
        }
        return null;
    }

    private <T extends Object> T getRandomElementFromSet(Set<T> set) {
        Random rnd = new Random();
        int i = rnd.nextInt(set.size());
        return (T) set.toArray()[i];
    }
}
