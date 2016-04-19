
package model;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by Benedek on 3/17/2016.
 */
public class BayesNetwork {
    private static final int lambda = 2;
    public static Network network;
    private static int numberOfVertices = 1000;


    public static void main(String[] args) {
        network = new Network();
        //createRandomNetwork(10);
        createRandomDAGNetwork(100);
        //importNetworkFromCSV("res/sample.0.data.csv");
        network.printNetwork();
    }

    private static void createRandomDAGNetwork(int numberOfNodes) {
        numberOfVertices = numberOfNodes;

        for (int i = 0; i < numberOfVertices; i++) {
            network.addNode(new Node("GENE" + i, network));
        }

        for (int i = 0; i < numberOfVertices * lambda; i++) {
            Random random = new Random();
            int index1 = random.nextInt(numberOfVertices);
            int index2 = random.nextInt(numberOfVertices);
            if (index1 != index2) {
                int parentIndex = Math.min(index1, index2);
                int childIndex = Math.max(index1, index2);
                Node parent = network.getNodeWithName("GENE" + parentIndex);
                Node child = network.getNodeWithName("GENE" + childIndex);
                // if the edge was already in the network
                if (child != null && parent != null && !network.containsEdge(parent, child)) {
                    network.addNewEdge(parent, child);
                } else {
                    i--;
                }
            } else {
                i--;
            }
        }
    }

    private static void createRandomNetwork(int numberOfNodes) {
        numberOfVertices = numberOfNodes;

        for (int i = 0; i < numberOfVertices; i++) {
            network.addRandomNode();
        }

        for (int i = 0; i < numberOfVertices * lambda; i++) {
            network.addRandomEdge();
        }

    }

    private static void importNetworkFromCSV(String fileName) {
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
}
