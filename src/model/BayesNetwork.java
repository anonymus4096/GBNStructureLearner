package model;

/**
 * Created by Benedek on 3/17/2016.
 */
public class BayesNetwork {
    private static final int lambda = 2;
    public static Network network;
    private static int numberOfVertices = 10;


    public static void main(String[] args) {


        network = new Network();
        //createRandomNetwork(numberOfVertices);

        for (int i = 0; i < numberOfVertices; i++) {
            network.addNode(new Node("GENE" + i, network));
        }
        for (int i = 0; i < numberOfVertices * lambda; i++) {
            network.addRandomEdge();
        }


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
}
