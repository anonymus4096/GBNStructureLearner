package model;

/**
 * Created by Benedek on 3/17/2016.
 */
public class BayesNetwork {
    public static Network network;

    public static void main(String[] args) {
        network = new Network();
        createRandomNetwork(10);

        for (Node node : network.getNodes()) {
            System.out.println(node.getName());
        }
    }

    private static void createRandomNetwork(int numberOfNodes) {
        for (int i = 0; i < numberOfNodes; i++) {
            network.addRandomNode();
        }
    }
}
