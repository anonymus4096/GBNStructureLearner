package model;

/**
 * Created by Benedek on 3/17/2016.
 */
public class Edge implements Comparable {
    private Node parent;
    private Node child;
    private Network network;

    public Edge(Network network, Node parent, Node child) {
        this.parent = parent;
        this.child = child;
        this.network = network;
    }

    public Network getNetwork() {
        return network;
    }

    public Node getParent() {
        return parent;
    }

    public Node getChild() {
        return child;
    }

    /**
     * Sorts edges first by their parent's name, then their child's name
     * @param o Other edge object
     * @return Return -1 or 1 based on which is "bigger"
     */
    @Override
    public int compareTo(Object o) {
        Edge e = (Edge) o;
        if (parent.getName().compareTo(e.getParent().getName()) > 0){
            return 1;
        } else if (parent.getName().compareTo(e.getParent().getName()) == 0){
            if (child.getName().compareTo(e.getChild().getName()) > 0){
                return 1;
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }
}
