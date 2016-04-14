package model;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
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
        //createRandomNetwork(numberOfVertices);
        importNetworkFromCSV("res/sample.0.data.csv");

        network.printNetwork();
    }

    private static void createRandomNetwork(int numberOfNodes) {

        for (int i = 0; i < numberOfNodes; i++) {
            network.addRandomNode();
        }

        for (int i = 0; i < numberOfNodes * lambda; i++) {
            network.addRandomEdge();
        }

    }

    private static void importNetworkFromCSV(String fileName){
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

        network.printNetwork();
    }
}
