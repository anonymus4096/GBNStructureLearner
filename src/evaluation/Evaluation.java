package evaluation;

import model.Edge;
import model.Network;
import model.Node;
import utils.GraphFunctions;

import java.util.*;

/**
 * Created by Benedek on 5/16/2016.
 */
public class Evaluation {
    private Set<Edge> realEdges, foundEdges;
    private Network realNetwork, myNetwork;
    private String fileName;

    public Evaluation(Network realNetwork, Network foundNetwork) {
        this.realEdges = realNetwork.getEdges();
        this.foundEdges = foundNetwork.getEdges();
        this.realNetwork = realNetwork;
        this.myNetwork = foundNetwork;
    }

    public Evaluation(String fileName, Set<Edge> foundEdges) {
        this.foundEdges = foundEdges;
        this.fileName = fileName;


        realEdges = getRealEdgesFromFile(fileName);
    }

    private Set<Edge> getRealEdgesFromFile(String fileName) {
        realNetwork = new Network(fileName);
        return null;
    }


    public Evaluation(String realNetworkFileName, String myNetworkFileName) {
        myNetwork = new Network(myNetworkFileName);
        realNetwork = new Network(realNetworkFileName);
        foundEdges = myNetwork.getEdges();
        realEdges = realNetwork.getEdges();
    }

    public void evaluate() {
        Set<Edge> truePosEdges = new TreeSet<>();
        Set<Edge> falsePosEdges = new TreeSet<>();
        Set<Edge> falseNegEdges = new TreeSet<>();

        for (Edge edge : foundEdges) {
            if (setOfPDAGEdgesContainsEdge(edge, realEdges)) {
                truePosEdges.add(edge);
            } else {
                falsePosEdges.add(edge);
            }
        }

        for (Edge edge : realEdges) {
            if (edge.isDirected()) {
                if (!foundEdges.contains(edge)) falseNegEdges.add(edge);
            } else {
                if (!foundEdges.contains(edge) && !foundEdges.contains(edge.getReverse())) falseNegEdges.add(edge);
            }
        }

        System.out.println("True positives:\t" + truePosEdges.size());
        System.out.println("False positives:\t" + falsePosEdges.size());
        System.out.println("False negatives:\t" + falseNegEdges.size());

        Map<Edge, Double> unsortedEdges = new HashMap<>();
        for (Edge e : realEdges) {
            unsortedEdges.put(e, e.getStrength());
        }
        Map<Edge, Double> sortedEdges = new TreeMap<>(new ValueComparator(unsortedEdges));
        sortedEdges.putAll(unsortedEdges);

        for (Map.Entry e : sortedEdges.entrySet()) {

            String isDiscovered = "not found";
            Edge edge = (Edge) e.getKey();
            if (foundEdges.contains(edge) || (!edge.isDirected() && foundEdges.contains(edge.getReverse()))) {
                isDiscovered = "found";
            } else if (foundEdges.contains(edge.getReverse())) {
                isDiscovered = "reversed";
            }

            String realStatus;
            String foundStatus;


            if (realEdges.contains(edge) && edge.isDirected()) {
                realStatus = "-->";
            } else if (realEdges.contains(edge) && !edge.isDirected()) {
                realStatus = "---";
            } else if (realEdges.contains(edge.getReverse()) && edge.isDirected()) {
                realStatus = "<--";
            } else if (realEdges.contains(edge.getReverse()) && !edge.isDirected()) {
                realStatus = "---";
            } else {
                realStatus = "-X-";
            }

            Edge foundEdge = GraphFunctions.getEdgeWithName(foundEdges, edge.getParent().getName(), edge.getChild().getName());
            Edge reverseFoundEdge = GraphFunctions.getEdgeWithName(foundEdges, edge.getChild().getName(), edge.getParent().getName());
            if (foundEdge != null && foundEdge.isDirected()) {
                foundStatus = "-->";
            } else if (foundEdge != null && !foundEdge.isDirected()) {
                foundStatus = "---";
            } else if (reverseFoundEdge != null && reverseFoundEdge.isDirected()) {
                foundStatus = "<--";
            } else if (reverseFoundEdge != null && !reverseFoundEdge.isDirected()) {
                foundStatus = "---";
            } else {
                foundStatus = "-X-";
            }

            System.out.println(e.getKey() + "\t" + realStatus + "\t" + foundStatus);
        }

    }

    public boolean setOfPDAGEdgesContainsEdge(Edge edge, Set<Edge> edges) {
        Edge reversedEdge = GraphFunctions.getEdge(edges, edge.getChild(), edge.getParent());
        return edges.contains(edge) || (reversedEdge != null && !reversedEdge.isDirected());
    }


    public void convertNetworkToPDag(Network network) {
//        for (Edge e : network.getEdges()) {
//            for (Edge e2 : network.getEdges()){
//                if (!e.isDirected()) break;
//                if (e == e2) continue;
//
//                if (e.getChild().getParents().size() == 1 ||
//                        e.getChild() == e2.getChild() &&
//                                (GraphFunctions.containsEdge(network.getEdges(), e.getParent(), e2.getParent()) || GraphFunctions.containsEdge(network.getEdges(), e2.getParent(), e.getParent()))) {
//                    e.setDirected(false);
//                    break;
//                }
//            }
//        }

        for (Edge e : network.getEdges()) {
            e.setDirected(false);
        }

        for (Node child : network.getNodes()) {
            for (Node parent : child.getParents()) {
                for (Node otherParent : child.getParents()) {
                    if (parent == otherParent) {
                        continue;
                    }
                    Edge e1 = GraphFunctions.getEdge(network.getEdges(), parent, child);
                    Edge e2 = GraphFunctions.getEdge(network.getEdges(), otherParent, child);
                    Edge parentEdge = GraphFunctions.getEdge(network.getEdges(), parent, otherParent);
                    if (parentEdge == null)
                        parentEdge = GraphFunctions.getEdge(network.getEdges(), otherParent, parent);
                    if (parentEdge == null) {
                        // there is no edge in any direction
                        e1.setDirected(true);
                        e2.setDirected(true);
                    }
                }
            }
        }

        boolean changed = true;
        while (changed) {
            changed = false;
            for (Edge edgeExamined : network.getEdges()) {
                if (!edgeExamined.isDirected()) {
                    Edge parentEdge;
                    for (Node parent : edgeExamined.getParent().getParents()) {
                        parentEdge = GraphFunctions.getEdge(network.getEdges(), parent, edgeExamined.getParent());
                        if (parentEdge != null && parentEdge.isDirected()) {
                            edgeExamined.setDirected(true);
                            changed = true;
                            break;
                        }
                    }
                }
            }
        }
        // amíg nincs változás
    }

    public Set<Edge> getRealEdges() {
        return realEdges;
    }

    public Set<Edge> getFoundEdges() {
        return foundEdges;
    }

    private class ValueComparator implements Comparator {
        Map map;

        public ValueComparator(Map map) {
            this.map = map;
        }

        public int compare(Object keyA, Object keyB) {
            Double valueA = (Double) map.get(keyA);
            Double valueB = (Double) map.get(keyB);
            if (Math.abs(valueB) > Math.abs(valueA)) {
                return 1;
            } else if (Math.abs(valueB) == Math.abs(valueA)) {
                return 0;
            } else {
                return -1;
            }

        }
    }
}
