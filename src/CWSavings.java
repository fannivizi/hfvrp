import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class CWSavings {
    private List<Vehicle> fleet;
    private List<Node> nodes;
    private Node depot;
    private List<Path> paths;

    public CWSavings(List<Vehicle> fleet, List<Node> nodes, Node depot) {
        this.fleet = new ArrayList<>(fleet);
        this.nodes = new ArrayList<>(nodes);
        this.depot = depot;
        this.paths = new ArrayList<>();
    }

    public List<Path> getPaths() {
        return paths;
    }

    public boolean interior(Path p, Node n) {
        List<Node> nodes = p.getNodes();
        if(nodes.get(1) == n || nodes.get(nodes.size()-2) == n) return false;
        return true;
    }

    public void run() {
        nodes.remove(depot);
        //create path depot -> node -> depot
        for(Node node: nodes) {
            paths.add(new Path(fleet.getFirst(), List.of(depot, node, depot)));
        }

        //compute savings
        List<Saving> savings = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i+1; j < nodes.size(); j++) {
                savings.add(new Saving(nodes.get(i), nodes.get(j), nodes.get(i).distance(depot) + depot.distance(nodes.get(j)) - nodes.get(i).distance(nodes.get(j))));
            }
        }

        //sort savings
        savings.sort(Collections.reverseOrder());

        //merge routes with biggest savings -- fucked

    }
}
