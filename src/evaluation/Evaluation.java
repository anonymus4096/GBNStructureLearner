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
        Map<Edge, Double> StrictTP = new HashMap<>();
        Map<Edge, Double> PDAGTP = new HashMap<>();
        Map<Edge, Double> StructureTP = new HashMap<>();


        Map<Edge, Double> unsortedEdges = new HashMap<>();
        for (Edge e : realEdges) {
            unsortedEdges.put(e, e.getStrength());
        }
        Map<Edge, Double> sortedEdges = new TreeMap<>(new ValueComparator(unsortedEdges));
        sortedEdges.putAll(unsortedEdges);

        for (Map.Entry e : sortedEdges.entrySet()) {
            Edge edge = (Edge) e.getKey();
            String realStatus;
            String foundStatus;

            if (realEdges.contains(edge) && edge.isDirected()) {
                realStatus = "1";
            } else if (realEdges.contains(edge) && !edge.isDirected()) {
                realStatus = "0";
            } else if (realEdges.contains(edge.getReverse()) && edge.isDirected()) {
                realStatus = "-1";
            } else if (realEdges.contains(edge.getReverse()) && !edge.isDirected()) {
                realStatus = "0";
            } else {
                realStatus = "-5";
            }

            Edge foundEdge = GraphFunctions.getEdgeWithName(foundEdges, edge.getParent().getName(), edge.getChild().getName());
            Edge reverseFoundEdge = GraphFunctions.getEdgeWithName(foundEdges, edge.getChild().getName(), edge.getParent().getName());
            if (foundEdge != null && foundEdge.isDirected()) {
                foundStatus = "1";
            } else if (foundEdge != null && !foundEdge.isDirected()) {
                foundStatus = "0";
            } else if (reverseFoundEdge != null && reverseFoundEdge.isDirected()) {
                foundStatus = "-1";
            } else if (reverseFoundEdge != null && !reverseFoundEdge.isDirected()) {
                foundStatus = "0";
            } else {
                foundStatus = "-5";
            }

            if (Objects.equals(realStatus, foundStatus)) {
                StrictTP.put((Edge) e.getKey(), (Double) e.getValue());
            }
            if (!(Objects.equals(foundStatus, "-5") ||
                    (Objects.equals(foundStatus, "-1") && Objects.equals(realStatus, "1")))) {
                PDAGTP.put((Edge) e.getKey(), (Double) e.getValue());
            }
            if (!Objects.equals(foundStatus, "-5")) {
                StructureTP.put((Edge) e.getKey(), (Double) e.getValue());
            }

            System.out.println(e.getKey() + "\t" + realStatus + "\t" + foundStatus);
        }

        System.out.println("TP:\t" + StrictTP.size() + "\t" + PDAGTP.size() + "\t" + StructureTP.size());
        System.out.println("FP:\t" + (foundEdges.size() - StrictTP.size()) + "\t" + (foundEdges.size() - PDAGTP.size()) + "\t" + (foundEdges.size() - StructureTP.size()));
        System.out.println("FN:\t" + (realEdges.size() - StrictTP.size()) + "\t" + (realEdges.size() - PDAGTP.size()) + "\t" + (realEdges.size() - StructureTP.size()));

        double strictSens = (double) StrictTP.size() / realEdges.size();
        double pdagSens = (double) PDAGTP.size() / realEdges.size();
        double structureSens = (double) StructureTP.size() / realEdges.size();
        double strictPrec = (double) StrictTP.size() / foundEdges.size();
        double pdagPrec = (double) PDAGTP.size() / foundEdges.size();
        double structurePrec = (double) StructureTP.size() / foundEdges.size();

        System.out.println("Sensitivity:\t" + strictSens + "\t" + pdagSens + "\t" + structureSens);
        System.out.println("Precision:\t" + strictPrec + "\t" + pdagPrec + "\t" + structurePrec);
        System.out.println("F1:\t" +
                2 * strictPrec * strictSens / (strictPrec + strictSens) + "\t" +
                2 * pdagPrec * pdagSens / (pdagPrec + pdagSens) + "\t" +
                2 * structurePrec * structureSens / (structurePrec + structureSens));

        double strictTPSum = 0;
        double pdagTPSum = 0;
        double structureTPSum = 0;
        double sum = 0;

        for (Map.Entry e : StrictTP.entrySet()) {
            strictTPSum += Math.abs((double) e.getValue());
        }
        for (Map.Entry e : PDAGTP.entrySet()) {
            pdagTPSum += Math.abs((double) e.getValue());
        }
        for (Map.Entry e : StructureTP.entrySet()) {
            structureTPSum += Math.abs((double) e.getValue());
        }
        for (Map.Entry e : sortedEdges.entrySet()) {
            sum += Math.abs((double) e.getValue());
        }

        System.out.println("Weighted Sensitivity:\t" + strictTPSum / sum + "\t" + pdagTPSum / sum + "\t" + structureTPSum / sum);
    }

    public boolean setOfPDAGEdgesContainsEdge(Edge edge, Set<Edge> edges) {
        Edge reversedEdge = GraphFunctions.getEdge(edges, edge.getChild(), edge.getParent());
        return edges.contains(edge) || (reversedEdge != null && !reversedEdge.isDirected());
    }


    public void convertNetworkToPDag(Network network) {
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
            return Double.compare(Math.abs(valueB), Math.abs(valueA));
        }
    }
}
