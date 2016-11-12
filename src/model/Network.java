package model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static utils.GraphFunctions.*;
import static utils.NameGenerator.generateRandomName;

/**
 * Created by Benedek on 3/17/2016.
 */
public class Network {
    private final String id;
    private Set<Node> nodes;
    private Set<Edge> edges;

    public Network() {
        id = UUID.randomUUID().toString();
        nodes = new TreeSet<>();
        edges = new TreeSet<>();
    }

    public Network(Set<Node> nodes) {
        id = UUID.randomUUID().toString();
        this.nodes = nodes;
        this.edges = new TreeSet<>();
    }

    public Network(String fileName) {
        id = UUID.randomUUID().toString();
        nodes = new TreeSet<>();
        edges = new TreeSet<>();
        loadNetworkFromFile(fileName);
    }

    private void loadNetworkFromFile(String fileName) {
        Scanner scanner;
        String line;
        int numberOfNodes = 0;
        try {
            scanner = new Scanner(new File(fileName));
            numberOfNodes = scanner.nextInt();
            scanner.nextLine();
            for (int i = 0; i < numberOfNodes; i++) {
                addNode(new Node(scanner.nextLine(), this));
            }
            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                String[] elements = line.split("\t");
                addNewEdge(elements[0], elements[1], Double.parseDouble(elements[2]));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void addNode(Node n1) {
        nodes.add(n1);
    }

    public void addNewEdge(String parentName, String childName, Double strength) {
        if (!containsNodeWithName(nodes, parentName)) {
            throw new IllegalArgumentException("Parent does not exist with the name " + parentName + " when trying to add new edges");
        } else if (!containsNodeWithName(nodes, childName)) {
            throw new IllegalArgumentException("Child does not exist with the name " + childName + " when trying to add new edges");
        }

        Node parent = getNodeWithName(getNodes(), parentName);
        Node child = getNodeWithName(getNodes(), childName);
        if (!containsEdge(edges, parent, child)) {
            edges.add(new Edge(this, parent, child, strength));
            parent.addNewChild(child);
            child.addNewParent(parent);
        }
    }

    public Set<Node> getNodes() {
        return nodes;
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
            if (edge.isDirected()) {
                System.out.println(edge.getParent().getName() + " --> " + edge.getChild().getName());
            } else {
                System.out.println(edge.getParent().getName() + " --- " + edge.getChild().getName());
            }
        }
        System.out.println();
    }

    public boolean isDAG() {
        // set that contains all the vertices at first
        Set<String> start = new TreeSet<>(this.getNames());
        // will contain one of the topological orders if exists such a thing
        List<Node> result = new ArrayList<>();
        // the next potential nodes to be considered
        Set<Node> next = new TreeSet<>();

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
                    start.remove(n.getName());
                }
            }
        }

        if (getNodes().size() == result.size()) {
            return true;
        } else {
            return false;
        }
    }

    public List<String> getNames() {
        ArrayList<String> names = new ArrayList<>();
        for (Node n : nodes) {
            names.add(n.getName());
        }
        Collections.sort(names);
        return names;
    }

    public void addNewEdge(String parentName, String childName) {
        if (!containsNodeWithName(nodes, parentName)) {
            throw new IllegalArgumentException("Parent does not exist with the name " + parentName + " when trying to add new edges");
        } else if (!containsNodeWithName(nodes, childName)) {
            throw new IllegalArgumentException("Child does not exist with the name " + childName + " when trying to add new edges");
        }

        Node parent = getNodeWithName(getNodes(), parentName);
        Node child = getNodeWithName(getNodes(), childName);
        if (!containsEdge(edges, parent, child)) {
            edges.add(new Edge(this, parent, child));
            parent.addNewChild(child);
            child.addNewParent(parent);
        }
    }

    public void reverseEdge(Node parent, Node child) {
        reverseEdge(parent.getName(), child.getName());
    }

    public void reverseEdge(String parentName, String childName) {
        if (!containsNodeWithName(nodes, parentName)) {
            throw new IllegalArgumentException("Parent does not exist with the name " + parentName + " when trying to delete the edge.");
        } else if (!containsNodeWithName(nodes, childName)) {
            throw new IllegalArgumentException("Child does not exist with the name " + childName + " when trying to delete the edge.");
        }

        Node parent = getNodeWithName(getNodes(), parentName);
        Node child = getNodeWithName(getNodes(), childName);
        if (containsEdge(edges, parent, child)) {
            // do nothing
        } else if (containsEdge(edges, child, parent)) {
            // the edge is pointing from the child to the parent -> switch the names of the nodes
            Node temp = parent;
            parent = child;
            child = temp;

        } else {
            throw new IllegalArgumentException("There was no edge to reverse!");
        }

        Edge e = getEdge(getEdges(), parent, child);
        Double strength = e.getStrength();

        edges.remove(e);
        parent.removeChild(child);
        child.removeParent(parent);

        edges.add(new Edge(this, child, parent, strength));
        child.addNewChild(parent);
        parent.addNewParent(child);
    }

    public Set<Edge> getEdges() {
        return edges;
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

    public void addRandomNode() {
        Node temp = new Node(generateRandomName(this), this);
        nodes.add(temp);
    }

    public void saveNetworkToFile(String fileName) {
        List<String> lines = new ArrayList<>();
        lines.add(String.valueOf(nodes.size()));
        for (Node n : nodes) {
            lines.add(n.getName());
        }
        for (Edge e : edges) {
            String line = e.toString();
            lines.add(line);
        }
        Path file = Paths.get(fileName);
        try {
            Files.write(file, lines, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean reversingViolatesDAG(Node parent, Node child) {
        boolean violates;
        if (containsEdge(edges, parent, child)) {
            deleteEdge(parent, child);
            violates = violatesDAG(child, parent);
            addNewEdge(parent, child);
        } else if (containsEdge(edges, child, parent)) {
            deleteEdge(child, parent);
            violates = violatesDAG(parent, child);
            addNewEdge(child, parent);
        } else {
            return false;
        }
        return violates;
    }

    public void deleteEdge(Node parent, Node child) {
        deleteEdge(parent.getName(), child.getName());
    }

    /**
     * determines whether adding the edge with the given parameters would mess up the DAG property
     *
     * @param parent start of the edge
     * @param child  end of the edge
     * @return return true if it violates the DAG condition
     */
    public boolean violatesDAG(Node parent, Node child) {
        return hasPath(child, parent);
    }

    public void deleteEdge(String parentName, String childName) {
        if (!containsNodeWithName(nodes, parentName)) {
            throw new IllegalArgumentException("Parent does not exist with the name " + parentName + " when trying to delete the edge.");
        } else if (!containsNodeWithName(nodes, childName)) {
            throw new IllegalArgumentException("Child does not exist with the name " + childName + " when trying to delete the edge.");
        }

        Node parent = getNodeWithName(getNodes(), parentName);
        Node child = getNodeWithName(getNodes(), childName);
        if (containsEdge(edges, parent, child)) {
            edges.remove(getEdge(getEdges(), parent, child));
            parent.removeChild(child);
            child.removeParent(parent);
        }
    }

    private boolean hasPath(Node from, Node to) {
        for (Node n : from.getDescendants()) {
            if (Objects.equals(n.getName(), to.getName())) {
                return true;
            }
        }
        return false;
    }

    public int size() {
        return getNodes().size();
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Network network = (Network) o;

        return id.equals(network.id);

    }
}
