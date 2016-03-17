package model;

/**
 * Created by Benedek on 3/17/2016.
 */
public class BayesNetwork {
    public static Network network;

    public static void main(String[] args) {
        network = new Network();
        createRandomNetwork(100);

        network.printNetwork();
    }

    private static void createRandomNetwork(int numberOfNodes) {
        final int lambda = 2;

        for (int i = 0; i < numberOfNodes; i++) {
            network.addRandomNode();
        }

        for (int i = 0; i < numberOfNodes * lambda; i++) {
            network.addRandomEdge();
        }


    }
}
