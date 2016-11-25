package search;

import model.Network;
import model.Node;
import org.apache.commons.math3.special.Gamma;
import org.jblas.Decompose;
import org.jblas.DoubleMatrix;
import org.jblas.Solve;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;


/**
 * Created by Benedek on 5/5/2016.
 */
public class BayesianScoring {
    private static BayesianScoring ourInstance = new BayesianScoring();
    private Move move;
    private Network network;
    private String fileName;
    private Double v, alpha;
    private int n;
    private DoubleMatrix mean;
    private DoubleMatrix variance;
    private DoubleMatrix beta;
    private List<String> namesOfNodes;
    private Set<Integer> indexes;
    private int dataLength;
    private DoubleMatrix betaStar;
    private DoubleMatrix mu;
    private int numberOfLinesToUse;
    private double lambda = 0.2;

    private BayesianScoring() {
    }

    public static BayesianScoring getInstance() {
        if (ourInstance == null) {
            ourInstance = new BayesianScoring();
        }
        return ourInstance;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public void setMove(Move move) {
        this.move = move;
    }

    public void setLambda(Double lambda) {
        this.lambda = lambda;
    }

    public void initializeValues() {
        mean = getMeansOfData(fileName, numberOfLinesToUse);
        variance = getVarianceOfData(fileName, numberOfLinesToUse);
        mu = DoubleMatrix.zeros(n, 1);
    }

    private DoubleMatrix getMeansOfData(String fileName, int numberOfLinesToUse) {
        double[] sumOfColumns = new double[0];
        try {
            Scanner scanner = new Scanner(new File(fileName));
            scanner.useDelimiter(",");
            String headerLine = scanner.nextLine();
            namesOfNodes = Arrays.asList(headerLine.split(","));
            int numberOfColumns = headerLine.split(",").length;
            sumOfColumns = new double[numberOfColumns];
            String[] values;
            int dataLines = 0;
            while (scanner.hasNextLine() && dataLines < numberOfLinesToUse) {
                String line = scanner.nextLine();
                values = line.split(",");
                for (int i = 0; i < numberOfColumns; i++) {
                    sumOfColumns[i] += Double.valueOf(values[i]);
                }
                dataLines++;
            }
            dataLength = dataLines;
            for (int i = 0; i < numberOfColumns; i++) {
                sumOfColumns[i] = sumOfColumns[i] / dataLines;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return sumOfColumns.length == 0 ? null : new DoubleMatrix(sumOfColumns);
    }

    private DoubleMatrix getVarianceOfData(String fileName, int numberOfLinesToUse) {
        DoubleMatrix sumOfVariances = null;
        try {
            Scanner scanner = new Scanner(new File(fileName));
            scanner.useDelimiter(",");
            String headerLine = scanner.nextLine();
            n = headerLine.split(",").length;
            sumOfVariances = new DoubleMatrix(n, n);
            String[] stringValues;
            double[] realValues;
            int dataLines = 0;
            while (scanner.hasNextLine() && dataLines < numberOfLinesToUse) {
                String line = scanner.nextLine();
                stringValues = line.split(",");
                realValues = new double[n];
                int columnIndex = 0;
                for (String s : stringValues) {
                    realValues[columnIndex] = Double.valueOf(stringValues[columnIndex]);
                    columnIndex++;
                }
                dataLines++;

                DoubleMatrix deviation = new DoubleMatrix(realValues);
                deviation = deviation.sub(mean);
                DoubleMatrix variance = deviation.mmul(deviation.transpose());
                sumOfVariances = sumOfVariances.add(variance);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return sumOfVariances;
    }

    public void setNumberOfLinesToUse(int numberOfLinesToUse) {
        this.numberOfLinesToUse = numberOfLinesToUse;
    }

    public Double calculateScoreOfMove() {
        Double score;
        Node parent = move.getEdge().getParent();
        Node child = move.getEdge().getChild();


        Double scoreBefore, scoreAfter;
        if (move.getType() == MoveType.adding) {
            scoreBefore = calculateScore();
            network.addNewEdge(parent, child);
            scoreAfter = calculateScore();
            network.deleteEdge(parent, child);
        } else if (move.getType() == MoveType.deleting) {
            scoreBefore = calculateScore();
            network.deleteEdge(parent, child);
            scoreAfter = calculateScore();
            network.addNewEdge(parent, child);
        } else {
            scoreBefore = calculateScore();
            network.reverseEdge(parent, child);
            scoreAfter = calculateScore();
            network.reverseEdge(child, parent);
        }

        // it has to be subtraction because we calculate the logarithms of the scores
        score = scoreAfter - scoreBefore;
        return score;
    }

    private Double calculateScore() {
        Set<Node> parents = new HashSet<>(move.getEdge().getChild().getParents());
        Node child = move.getEdge().getChild();

        Double denominator = empiricalProbability(parents);
        parents.add(child);
        Double numerator = empiricalProbability(parents);

        return numerator - denominator - lambda * network.getEdges().size() * network.getEdges().size();
    }

    private Double empiricalProbability(Set<Node> nodes) {
        if (nodes.size() == 0) {
            return 0.0;
        }
        int lw = nodes.size();

        DoubleMatrix betaW = getBetaW(nodes);
        DoubleMatrix betaStarW = getBetaStarW(betaW);
        Double alphaw = alpha - n + lw;
        int M = dataLength;

        /*double ans = Math.pow((1 / (2 * Math.PI)), M * lw / 2) *
                Math.pow((v / (v + M)), lw / 2) *
                (c(lw, alphaw) / c(lw, alphaw + M)) *
                (Math.pow(getDeterminant(betaW), alphaw / 2) / Math.pow(getDeterminant(betaStarW), (alphaw + M) / 2));*/
        Double ans = (M * lw / 2) * Math.log(1 / (2 * Math.PI)) +
                (lw / 2) * Math.log(v / (v + M)) +
                (logc(lw, alphaw) - logc(lw, alphaw + M)) +
                ((alphaw / 2) * Math.log(getDeterminant(betaW)) - ((alphaw + M) / 2) * Math.log(getDeterminant(betaStarW)));

        return ans;
    }

    private DoubleMatrix getBetaW(Set<Node> nodes) {
        DoubleMatrix inverseBeta = Solve.pinv(getBeta());
        indexes = new TreeSet<>();
        for (Node n : nodes) {
            indexes.add(namesOfNodes.indexOf(n.getName()));
        }
        DoubleMatrix inverseBetaW = getSubMatrix(inverseBeta, indexes);
        return Solve.pinv(inverseBetaW);
    }

    public DoubleMatrix getBetaStarW(DoubleMatrix betaW) {
        int M = dataLength;
        DoubleMatrix meanW = getSubVector(mean, indexes);
        DoubleMatrix muW = getSubVector(mu, indexes);
        return betaW.add(getSubMatrix(variance, indexes)).add(meanW.sub(muW).mmul(meanW.sub(muW).transpose())).mul(((v * M) / (v + M)));
    }

    private double logc(int nPar, Double alphaPar) {
        double sum = 0;
        for (int i = 1; i <= nPar; i++) {
            sum += Gamma.logGamma((alphaPar + 1 - i) / 2);
        }
        return -1 * ((alphaPar * nPar / 2) * Math.log(2) + (n * (n - 1) / 4) * Math.log(Math.PI) + sum);
    }

    public double getDeterminant(DoubleMatrix matrix) {
        if (matrix.rows != matrix.columns) {
            throw new IllegalArgumentException("The matrix is not a square matrix.");
        }
        Decompose.LUDecomposition<DoubleMatrix> LUDecomposition = Decompose.lu(matrix);

        Double determinant = 1.0;
        for (int i = 0; i < LUDecomposition.l.columns; i++) {
            determinant *= LUDecomposition.l.get(i, i);
            determinant *= LUDecomposition.u.get(i, i);
        }
        determinant *= getSignOfPermutation(LUDecomposition.p);

        return determinant;
    }

    public DoubleMatrix getBeta() {
        if (beta == null) {
            calculateBeta();
        }
        return beta;
    }

    private DoubleMatrix getSubMatrix(DoubleMatrix matrix, Set<Integer> indexes) {
        DoubleMatrix temp = new DoubleMatrix(indexes.size(), matrix.getColumns());
        int newIndexRow = 0;
        for (Integer row : indexes) {
            for (int col = 0; col < matrix.columns; col++) {
                temp.put(newIndexRow, col, matrix.get(row, col));
            }
            newIndexRow++;
        }

        DoubleMatrix subMatrix = new DoubleMatrix(indexes.size(), indexes.size());
        int newIndexCol = 0;
        for (Integer col : indexes) {
            for (int row = 0; row < temp.rows; row++) {
                subMatrix.put(row, newIndexCol, temp.get(row, col));
            }
            newIndexCol++;
        }

        return subMatrix;
    }

    private DoubleMatrix getSubVector(DoubleMatrix vector, Set<Integer> indexes) {
        if (vector.rows != 1 && vector.columns != 1) {
            throw new IllegalArgumentException("This is not a vector!");
        }
        boolean transposed = false;
        if (vector.rows == 1) {
            vector = vector.transpose();
            transposed = true;
        }

        DoubleMatrix subVector = new DoubleMatrix(indexes.size(), 1);
        int newIndexRow = 0;
        for (Integer row : indexes) {
            subVector.put(newIndexRow, 0, vector.get(row, 0));
            newIndexRow++;
        }

        if (transposed) {
            subVector = subVector.transpose();
        }

        return subVector;
    }

    private Integer getSignOfPermutation(DoubleMatrix p) {
        List<Integer> inversions = new ArrayList<>(Collections.nCopies(p.rows, 0));

        List<Integer> permutation = new ArrayList<>(Collections.nCopies(p.rows, 0));
        for (int i = 0; i < p.rows; i++) {
            for (int j = 0; j < p.columns; j++) {
                if (p.get(i, j) > 0) {
                    permutation.set(i, j);
                    break;
                }
            }
        }

        for (int i = 0; i < permutation.size(); i++) {
            int numberOfInversions = 0;
            for (int j = i + 1; j < permutation.size(); j++) {
                if (permutation.get(i) > permutation.get(j)) {
                    numberOfInversions++;
                }
            }
            inversions.set(i, numberOfInversions);
        }

        int sum = 0;
        for (Integer i : inversions) {
            if (i > 0) {
                sum += i;
            }
        }

        return sum % 2 == 0 ? 1 : -1;
    }

    private DoubleMatrix calculateBeta() {
        DoubleMatrix T = DoubleMatrix.eye(network.size());

        v = (double) (n + 1);
        alpha = (double) n;

        beta = T.mul(v * (alpha - n + 1) / (v + 1));
        return beta;
    }

    private double c(int nPar, Double alphaPar) {
        double product = 1;
        for (int i = 1; i <= nPar; i++) {
            product *= Gamma.gamma((alphaPar + 1 - i) / 2);
        }
        return 1 / (Math.pow(2, alphaPar * nPar / 2) * Math.pow(Math.PI, nPar * (nPar - 1) / 4) * product);
    }

    public DoubleMatrix getBetaStar() {
        if (betaStar == null) {
            calculateBetaStar();
        }
        return betaStar;
    }

    private void calculateBetaStar() {
        int M = dataLength;
        betaStar = getBeta().add(variance).add(mean.sub(mu).mmul(mean.sub(mu).transpose())).mul(((v * M) / (v + M)));
    }


}
