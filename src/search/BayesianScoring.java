package search;

import model.Network;
import model.Node;
import org.jblas.DoubleMatrix;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Set;

/**
 * Created by Benedek on 5/5/2016.
 */
public class BayesianScoring {
    private Move move;
    private Network network;
    String fileName = "res/sample.0.data.csv";
    private Double v, alpha, n;
    private DoubleMatrix mean;

    public BayesianScoring(Move m, Network n, String fileName) {
        move = m;
        network = n;
        this.fileName = fileName;
        initializeValues();
    }

    private void initializeValues() {
        mean = getMeansOfData(fileName);
    }

    private DoubleMatrix getMeansOfData(String fileName) {
        double[] sumOfColumns = new double[0];
        try {
            Scanner scanner = new Scanner(new File(fileName));
            scanner.useDelimiter(",");
            String headerLine = scanner.nextLine();
            int numberOfColumns = headerLine.split(",").length;
            sumOfColumns = new double[numberOfColumns];
            String[] values;
            int dataLines = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                values = line.split(",");
                for (int i = 0; i < numberOfColumns; i++) {
                    sumOfColumns[i] = Double.valueOf(values[i]);
                }
                dataLines++;
            }
            for (int i = 0; i < numberOfColumns; i++) {
                sumOfColumns[i] = sumOfColumns[i] / dataLines;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return sumOfColumns.length == 0 ? null : new DoubleMatrix(sumOfColumns);
    }

    public Double calculateScoreOfMove() {
        Double score = 0.0;
        Node parent = move.getEdge().getParent();
        Node child = move.getEdge().getChild();

        Double scoreBefore = calculateScore();
        network.addNewEdge(parent, child);
        Double scoreAfter = calculateScore();
        network.deleteEdge(parent, child);

        score = scoreAfter / scoreBefore;
        return score;
    }

    private Double calculateScore() {
        Set<Node> parents = move.getEdge().getChild().getParents();
        Node child = move.getEdge().getChild();

        Double denominator = empiricalProbability(parents);
        parents.add(child);
        Double numerator = empiricalProbability(parents);

        return numerator / denominator;
    }

    private Double empiricalProbability(Set<Node> parents) {
        return 0.0;
    }

    private DoubleMatrix beta() {
        DoubleMatrix beta = new DoubleMatrix(network.size(), network.size());
        DoubleMatrix T = DoubleMatrix.eye(network.size());

        v = 3.0;
        alpha = v - 1;


        beta = T.mul(v * (alpha - n + 1) / (v + 1));
        return beta;
    }


}
