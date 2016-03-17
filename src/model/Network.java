package model;

import java.util.HashSet;
import java.util.Set;

import static utils.NameGenerator.generateRandomName;

/**
 * Created by Benedek on 3/17/2016.
 */
public class Network {
    private Set<Node> nodes;

    public Network() {
        nodes = new HashSet<>();
    }

    public Network(Set<Node> nodes) {
        this.nodes = nodes;
    }

    public Set<Node> getNodes() {
        return nodes;
    }

    public void addRandomNode() {
        Node temp = new Node(generateRandomName(nodes), this);
        nodes.add(temp);
    }
}
