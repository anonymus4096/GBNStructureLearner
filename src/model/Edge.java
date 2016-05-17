package model;

import java.util.regex.Pattern;

/**
 * Created by Benedek on 3/17/2016.
 */
public class Edge implements Comparable {
    private Node parent;
    private Node child;
    private Network network;
    private Double strength;

    public Edge(Network network, Node parent, Node child) {
        this.parent = parent;
        this.child = child;
        this.network = network;
        this.strength = 0.0;
    }

    public Edge(Network network, Node parent, Node child, Double strength) {
        this.parent = parent;
        this.child = child;
        this.network = network;
        this.strength = strength;
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

    public Double getStrength() {
        return strength;
    }

    public void setStrength(Double strength) {
        this.strength = strength;
    }

    /**
     * Sorts edges first by their parent's name, then their child's name
     *
     * @param o Other edge object
     * @return Return -1 or 1 based on which is "bigger"
     */
    @Override
    public int compareTo(Object o) {
        Edge e = (Edge) o;
        int parentID = 0, childID = 0, otherParentID = 0, otherChildID = 0;
        boolean isGene = true;
        if (Pattern.matches("(G|g)(E|e)(N|n)(E|e)[0-9]+", parent.getName())) {
            parentID = Integer.parseInt(parent.getName().substring(4));
        } else {
            isGene = false;
        }

        if (Pattern.matches("(G|g)(E|e)(N|n)(E|e)[0-9]+", child.getName())) {
            childID = Integer.parseInt(child.getName().substring(4));
        } else {
            isGene = false;
        }

        if (Pattern.matches("(G|g)(E|e)(N|n)(E|e)[0-9]+", e.getParent().getName())) {
            otherParentID = Integer.parseInt(e.getParent().getName().substring(4));
        } else {
            isGene = false;
        }

        if (Pattern.matches("(G|g)(E|e)(N|n)(E|e)[0-9]+", e.getChild().getName())) {
            otherChildID = Integer.parseInt(e.getChild().getName().substring(4));
        } else {
            isGene = false;
        }


        if (isGene) {
            if (parentID > otherParentID) {
                return 1;
            } else if (parentID == otherParentID) {
                if (childID > otherChildID) {
                    return 1;
                } else if (childID < otherChildID) {
                    return -1;
                } else {
                    return 0;
                }
            } else {
                return -1;
            }
        } else {
            if (parent.getName().compareTo(e.getParent().getName()) > 0) {
                return 1;
            } else if (parent.getName().compareTo(e.getParent().getName()) == 0) {
                if (child.getName().compareTo(e.getChild().getName()) > 0) {
                    return 1;
                } else if (child.getName().compareTo(e.getChild().getName()) < 0) {
                    return -1;
                } else {
                    return 0;
                }
            } else {
                return -1;
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        Edge e = (Edge) obj;
        return compareTo(e) == 0;
    }

    @Override
    public String toString() {
        return "" + parent.getName() + "\t" + child.getName() + "\t" + strength.toString();
    }
}
