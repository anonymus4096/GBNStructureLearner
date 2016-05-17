package evaluation;

import model.Edge;
import model.Network;

import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Benedek on 5/16/2016.
 */
public class Evaluation {
    private Set<Edge> realEdges, foundEdges;
    private String fileName;

    public Evaluation(Network realNetwork, Network foundNetwork) {
        this.realEdges = realNetwork.getEdges();
        this.foundEdges = foundNetwork.getEdges();
    }

    public Evaluation(String fileName, Set<Edge> foundEdges) {
        this.foundEdges = foundEdges;
        this.fileName = fileName;

        realEdges = getRealEdgesFromFile(fileName);
    }

    private Set<Edge> getRealEdgesFromFile(String fileName) {
        //TODO

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
    }

    public Set<Edge> getRealEdges() {
        return realEdges;
    }

    public Set<Edge> getFoundEdges() {
        return foundEdges;
    }
}
