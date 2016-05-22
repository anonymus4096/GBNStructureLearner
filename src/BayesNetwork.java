import evaluation.Evaluation;
import model.Network;
import model.Node;
import search.HillClimbing;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import static utils.GraphFunctions.containsEdge;
import static utils.GraphFunctions.getNodeWithName;

/**
 * Created by Benedek on 3/17/2016.
 */
public class BayesNetwork {
    private static double lambda = 2;
    public static Network network;
    public static Network realNetwork;
    private static int numberOfVertices = 100;
    private static String format = "%03d";

    private static String dataFileName = "res/sample.0.small.data.csv";
    private static String structureFileName = "res/sample.0.small.structure";


    public static void main(String[] args) {
        network = new Network();
        realNetwork = new Network();

        importEmptyNetworkFromCSV(network, dataFileName);
        addRandomDAGEdgesToEmptyNetwork(network, 0);
        importNetworkFromCSV(realNetwork, dataFileName, structureFileName);
        HillClimbing hillClimbing = new HillClimbing(network, realNetwork);
        hillClimbing.climbHill();

        //network = new Network("myNetwork.txt");
        //realNetwork = new Network("realNetwork.txt");
        Evaluation evaluation = new Evaluation(realNetwork, network);
        evaluation.evaluate();

        realNetwork.saveNetworkToFile("realNetwork.txt");
        network.saveNetworkToFile("myNetwork.txt");
        realNetwork.printNetwork();
        network.printNetwork();
    }

    private static void addRandomDAGEdgesToEmptyNetwork(Network network, int numberOfEdges) {
        Random random = new Random();
        List<Node> nodes = new ArrayList<>();
        nodes.addAll(network.getNodes());
        if (numberOfEdges > network.size() * (network.size() - 1) / 2) {
            numberOfEdges = network.size() * (network.size() - 1) / 2;
        }
        for (int i = 0; i < numberOfEdges; i++) {
            Node parent = nodes.get(random.nextInt(nodes.size()));
            Node child = nodes.get(random.nextInt(nodes.size()));
            if (parent != child && !network.violatesDAG(parent, child)) {
                network.addNewEdge(parent, child);
            } else {
                i--;
            }
        }
    }

    private static void createEmptyNetwork(Network network, int numberOfNodes) {
        numberOfVertices = numberOfNodes;
        format = "%0" + String.valueOf(numberOfVertices - 1).length() + "d";

        if (lambda > ((double) numberOfVertices - 1) / 2) {
            lambda = ((double) numberOfVertices - 1) / 2;
        }

        for (int i = 0; i < numberOfVertices; i++) {
            String name = "GENE" + String.format(format, i);
            network.addNode(new Node(name, network));
        }
    }

    private static void createRandomDAGNetwork(Network network, int numberOfNodes) {
        numberOfVertices = numberOfNodes;
        format = "%0" + String.valueOf(numberOfVertices - 1).length() + "d";

        if (lambda > ((double) numberOfVertices - 1) / 2) {
            lambda = ((double) numberOfVertices - 1) / 2;
        }

        for (int i = 0; i < numberOfVertices; i++) {
            String name = "GENE" + String.format(format, i);
            network.addNode(new Node(name, network));
        }

        for (int i = 0; i < numberOfVertices * lambda; i++) {
            Random random = new Random();
            int index1 = random.nextInt(numberOfVertices);
            int index2 = random.nextInt(numberOfVertices);
            if (index1 != index2) {
                int parentIndex = Math.min(index1, index2);
                int childIndex = Math.max(index1, index2);
                Node parent = getNodeWithName(network.getNodes(), "GENE" + String.format(format, parentIndex));
                Node child = getNodeWithName(network.getNodes(), "GENE" + String.format(format, childIndex));
                // if the edge was already in the network
                if (child != null && parent != null && !containsEdge(network.getEdges(), parent, child)) {
                    network.addNewEdge(parent, child);
                } else {
                    i--;
                }
            } else {
                i--;
            }
        }
    }

    private static void createRandomNetwork(Network network, int numberOfNodes) {
        numberOfVertices = numberOfNodes;
        if (lambda > ((double) numberOfVertices - 1) / 2) {
            lambda = ((double) numberOfVertices - 1) / 2;
        }

        for (int i = 0; i < numberOfVertices; i++) {
            network.addRandomNode();
        }

        for (int i = 0; i < numberOfVertices * lambda; i++) {
            network.addRandomEdge();
        }

    }

    private static void importEmptyNetworkFromCSV(Network network, String fileName) {
        try {
            Scanner scanner = new Scanner(new File(fileName));
            scanner.useDelimiter(",");
            String headerLine = scanner.nextLine();
            ArrayList<String> headers = new ArrayList<String>(Arrays.asList(headerLine.split(",")));
            numberOfVertices = headers.size();
            for (int i = 0; i < numberOfVertices; i++) {
                network.addNode(new Node(headers.get(i), network));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void importNetworkFromCSV(Network network, String nodesFileName, String edgesFileName) {
        importEmptyNetworkFromCSV(network, nodesFileName);

        try {
            Scanner scanner = new Scanner(new File(edgesFileName));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (!Objects.equals(line.trim(), "")) {
                    ArrayList<String> elements = new ArrayList<String>(Arrays.asList(line.split("\t")));
                    if (elements.size() > 1 && Objects.equals(elements.get(2), "child")) {
                        network.addNewEdge(elements.get(0), elements.get(1), Double.valueOf(elements.get(3)));
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
