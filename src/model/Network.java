package model;

import java.util.*;

import static utils.GraphFunctions.*;
import static utils.NameGenerator.generateRandomName;

/**
 * Created by Benedek on 3/17/2016.
 */
public class Network {
    private Set<Node> nodes;
    private Set<Edge> edges;

    public Network() {
        nodes = new TreeSet<>();
        edges = new TreeSet<>();
    }

    public Network(Set<Node> nodes) {
        this.nodes = nodes;
        this.edges = new HashSet<>();
    }

    public Set<Node> getNodes() {
        return nodes;
    }

    public Set<Edge> getEdges() {
        return edges;
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
        Node temp = new Node(generateRandomName(this), this);
        nodes.add(temp);
    }

    public void printNetwork() {
        System.out.println("NETWORK CONTAINS " + nodes.size() + " NODES AND " + edges.size() + " EDGES");
        if (isDAG()) {
            System.out.println("NETWORK IS ACYCLIC");
        } else {
            System.out.println("NETWORK IS NOT ACYCLIC");
        }
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
        if (!containsNodeWithName(nodes, parent.getName())) {
            throw new IllegalArgumentException("Parent does not exist with the name " + parent.getName() + " when trying to add new edges");
        } else if (!containsNodeWithName(nodes, child.getName())) {
            throw new IllegalArgumentException("Child does not exist with the name " + child.getName() + " when trying to add new edges");
        }
        if (!containsEdge(edges, parent, child)) {
            edges.add(new Edge(this, parent, child));
            parent.addNewChild(child);
            child.addNewParent(parent);
        }
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

    public boolean isDAG() {
        // set that contains all the vertices at first
        Set<String> start = new HashSet<>(this.getNames());
        // will contain one of the topological orders if exists such a thing
        List<Node> result = new ArrayList<>();
        // the next potential nodes to be considered
        Set<Node> next = new HashSet<>();

        // move the root vertices from the start set to the next set
        Iterator<String> iter = start.iterator();
        while (iter.hasNext()) {
            Node n = getNodeWithName(nodes, iter.next());
            if (n != null) {
                if (n.getParents() == null || n.getParents().size() == 0) {
                    next.add(n);
                }
            }
        }

        while (next.size() > 0) {
            Node current = next.iterator().next();
            Set<Node> potentialNext = current.getChildren();
            next.remove(current);
            result.add(current);

            for (Node n : potentialNext) {
                if (!next.contains(n) && result.containsAll(n.getParents())) {
                    next.add(n);
                    start.remove(n);
                }
            }
        }

        if (getNodes().size() == result.size()) {
            return true;
        } else {
            return false;
        }


    }


    /**
     * determines whether adding the edge with the given parameters would mess up the DAG property
     * @param parent start of the edge
     * @param child end of the edge
     * @return return true if it violates the DAG condition
     */
    public boolean violatesDAG(Node parent, Node child) {
        return hasPath(child, parent);
    }

    private boolean hasPath(Node from, Node to) {
        return from.getDescendants().contains(to);
    }

}
