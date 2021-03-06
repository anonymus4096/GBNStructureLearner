import evaluation.Evaluation;
import model.Network;
import model.Node;
import org.apache.commons.cli.*;
import search.HillClimbing;
import search.LocalSearching;
import search.SimulatedAnnealing;
import utils.GraphFunctions;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Created by Benedek on 3/17/2016.
 */
public class BayesNetwork {
    public static Network network;
    public static Network realNetwork;
    private static int numberOfVertices = 100;
    private static int numberOfLinesToUse = 1000000;
    private static int numberOfRandomEdges = 0;

    private static String dataFileName;
    private static String structureFileName;
    private static String savedNetworkFileName = null;
    private static boolean evalOnly = false;
    private static double lambda = 0;


    public static void main(String[] args) {
        network = new Network();
        realNetwork = new Network();

        CommandLineParser parser = new DefaultParser();
        Options options = new Options()
                .addOption("sa", "search-algorithm", true, "choose searching algorithm")
                .addOption("df", "data-filename", true, "specify file containing data")
                .addOption("sf", "structure-filename", true, "specify file containing network structure")
                .addOption("ns", "number-of-steps", true, "specify the number of steps the search algorithm should make")
                .addOption("ld", "load-from-file", true, "if you wish to load the network you saved previously, specify the file")
                .addOption("eo", "evaluation-only", false, "if you only wish to evaluate the network")
                .addOption("lb", "lambda", true, "specify how strong the regularization should be (default=0)")
                .addOption("re", "random-edges", true, "specify how many random edges should the graph contain")
                .addOption("dr", "data-rows", true, "specify the first how many rows should be used for searching");

        String searchAlgorithmParam = "";
        Integer numberOfSteps = 10000;

        try {
            CommandLine line = parser.parse(options, args);
            if (line.hasOption("sa")) {
                searchAlgorithmParam = line.getOptionValue("sa");
            }
            if (line.hasOption("df")) {
                dataFileName = line.getOptionValue("df");
            }
            if (line.hasOption("sf")) {
                structureFileName = line.getOptionValue("sf");
            }
            if (line.hasOption("ns")) {
                numberOfSteps = Integer.valueOf(line.getOptionValue("ns"));
            }
            if (line.hasOption("ld")) {
                savedNetworkFileName = line.getOptionValue("ld");
                File savedNetworkFile = new File(savedNetworkFileName);
                if (!savedNetworkFile.exists()) {
                    System.out.println("The file containing the saved network could not be found! Exiting...");
                    return;
                }
            }

            if (line.hasOption("eo")) {
                evalOnly = true;
            }
            if (line.hasOption("lb")) {
                lambda = Double.parseDouble(line.getOptionValue("lb"));
            }

            if (line.hasOption("re")) {
                numberOfRandomEdges = Integer.valueOf(line.getOptionValue("re"));
            }

            if (line.hasOption("dr")) {
                numberOfLinesToUse = Integer.valueOf(line.getOptionValue("dr"));
            }


        } catch (ParseException exp) {
            System.out.println("ParseException: " + exp.getMessage());
            return;
        }

        if (dataFileName != null) {
            File dataFile = new File(dataFileName);
            if (!dataFile.exists()) {
                System.out.println("The data file that was specified could not be found! Exiting...");
                return;
            }
        } else {
            System.out.println("You did not specify a data file! Exiting...");
            return;
        }


        if (structureFileName != null) {
            File structureFile = new File(structureFileName);
            if (!structureFile.exists()) {
                System.out.println("The structure file that was specified could not be found! Exiting...");
                return;
            }
        } else {
            System.out.println("You did not specify a structure file! Exiting...");
            return;
        }

        if (evalOnly) {
            if (savedNetworkFileName != null) {
                network = new Network(savedNetworkFileName);
            } else {
                System.out.println("You did not specify the file to load saved network from. Exiting...");
                return;
            }

            importNetworkFromCSV(realNetwork, dataFileName, structureFileName);
        } else {
            if (numberOfSteps <= 0) {
                System.out.println("The number of steps specified is invalid. Exiting...");
                return;
            }

            if (savedNetworkFileName != null) {
                network = new Network(savedNetworkFileName);
            } else {
                importEmptyNetworkFromCSV(network, dataFileName);
            }

            importNetworkFromCSV(realNetwork, dataFileName, structureFileName);
            addRandomDAGEdgesToEmptyNetwork(network, numberOfRandomEdges);

            LocalSearching localSearching;
            switch (searchAlgorithmParam) {
                case "hillclimbing":
                case "hc":
                    localSearching = new HillClimbing(network, numberOfLinesToUse, dataFileName, numberOfSteps, lambda);
                    break;
                case "simulatedannealing":
                case "sa":
                default:
                    // default searching algorithm is simulated annealing
                    localSearching = new SimulatedAnnealing(network, numberOfLinesToUse, dataFileName, numberOfSteps, lambda);
                    break;
            }
            localSearching.doSearch();

            LocalDateTime datetime = LocalDateTime.now();
            String timestamp = datetime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            network.saveNetworkToFile("myNetwork_" + timestamp + ".txt");
            System.out.println("myNetwork_" + timestamp + ".txt created.");
        }

        Evaluation evaluation = new Evaluation(realNetwork, network);
        evaluation.convertNetworkToPDag(network);
        evaluation.convertNetworkToPDag(realNetwork);
        evaluation.evaluate();

        realNetwork.printNetwork();
        network.printNetwork();
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
            if (parent != child && !GraphFunctions.containsEdge(network.getEdges(), parent, child) && !network.violatesDAG(parent, child)) {
                network.addNewEdge(parent, child);
            } else {
                i--;
            }
        }
    }

    public static int getNumberOfLinesToUse() {
        return numberOfLinesToUse;
    }
}
