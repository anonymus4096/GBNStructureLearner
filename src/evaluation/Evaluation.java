package evaluation;

import model.Edge;
import model.Network;

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

    public Evaluation(String realNetworkFileName, String myNetworkFileName) {
        myNetwork = new Network(myNetworkFileName);
        realNetwork = new Network(realNetworkFileName);
        foundEdges = myNetwork.getEdges();
        realEdges = realNetwork.getEdges();
    }


    private Set<Edge> getRealEdgesFromFile(String fileName) {
        realNetwork = new Network(fileName);
        return null;
    }

    public void evaluate() {
        Set<Edge> commonEdges;
        Set<Edge> phantomEdges;
        Set<Edge> undiscoveredEdges;

        commonEdges = new TreeSet<>(realEdges);
        phantomEdges = new TreeSet<>(foundEdges);
        undiscoveredEdges = new TreeSet<>(realEdges);

        commonEdges.retainAll(foundEdges);
        phantomEdges.removeAll(realEdges);
        undiscoveredEdges.removeAll(foundEdges);

        System.out.println("True positives:\t" + commonEdges.size());
        System.out.println("False positives:\t" + phantomEdges.size());
        System.out.println("False negatives:\t" + undiscoveredEdges.size());

        Map<Edge, Double> unsortedEdges = new HashMap<>();
        for (Edge e : realEdges) {
            unsortedEdges.put(e, e.getStrength());
        }
        Map<Edge, Double> sortedEdges = new TreeMap<>(new ValueComparator(unsortedEdges));
        sortedEdges.putAll(unsortedEdges);

        for (Map.Entry e : sortedEdges.entrySet()) {
            boolean isDiscovered = false;
            if (foundEdges.contains(e.getKey())) {
                isDiscovered = true;
            }
            System.out.println(e.getKey() + "\t" + isDiscovered);
        }



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
