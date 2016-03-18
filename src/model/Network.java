package model;

import java.util.*;

import static utils.NameGenerator.generateRandomName;

/**
 * Created by Benedek on 3/17/2016.
 */
public class Network {
    private Set<Node> nodes;
    private Set<Edge> edges;

    public Network() {
        nodes = new HashSet<>();
        edges = new HashSet<>();
    }

    public Network(Set<Node> nodes) {
        this.nodes = nodes;
        this.edges = new HashSet<>();
    }

    public Set<Node> getNodes() {
        return nodes;
    }

    public List<String> getNames() {
        ArrayList<String> names = new ArrayList<>();
        for (Node n : nodes) {
            names.add(n.getName());
        }
        Collections.sort(names);
        return names;
    }

    public void addRandomNode() {
        Node temp = new Node(generateRandomName(nodes), this);
        nodes.add(temp);
    }

    public void printNetwork() {
        System.out.println("NETWORK CONTAINS " + nodes.size() + " NODES AND " + edges.size() + " EDGES");
        System.out.println("--------------------------------------------------------------------------------");
        getNames().forEach(System.out::println);
        System.out.println();
        System.out.println("--------------------------------------------------------------------------------");
        for (Edge edge : edges) {
            System.out.println(edge.getParent().getName() + " --> " + edge.getChild().getName());
        }
        System.out.println();


    }

    public void addNewEdge(Node parent, Node child) {
        edges.add(new Edge(parent, child));
        parent.addNewChild(child);
        child.addNewParent(parent);
    }

    public void addRandomEdge() {
        Node parent, child;
        parent = getRandomNode();
        child = getRandomNode();

        addNewEdge(parent, child);
    }

    public Node getRandomNode() {
        if (nodes.size() == 0) {
            addRandomNode();
        }
        int item = new Random().nextInt(nodes.size());
        int i = 0;
        for (Node node : nodes) {
            if (i == item)
                return node;
            i = i + 1;
        }
        // will never get here
        return null;
    }

    public void addNode(Node n1) {
        nodes.add(n1);
    }
}
