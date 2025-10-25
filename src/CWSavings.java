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
        for(Saving saving: savings) {
            Node a, b;
            Path a_path = null, b_path = null;
            a = saving.getA();
            b = saving.getB();

            //find the path of the nodes
            for(Path p: paths) {
                if(p.getNodes().contains(a)) a_path = p;
                if(p.getNodes().contains(b)) b_path = p;
            }

            //if on same route or interior, skip
            if(a_path == b_path || interior(a_path, a) || interior(b_path, b)) continue;

            //if demand of route < capacity, merge
            if(a_path.demand() + b_path.demand() <= Integer.max(a_path.getVehicle().getCapacity(), b_path.getVehicle().getCapacity())) {
                paths.remove(a_path);
                paths.remove(b_path);
                paths.add(a_path.merge(b_path));
                continue;
            }

            //if demand> capacity, try to upgrade vehicle
            int v = -1;
            for (int i = 0; i < fleet.size(); i++) {
                if(fleet.get(i).getCapacity() > a_path.demand() + b_path.demand()) {
                    v = i;
                    a_path.setVehicle(fleet.get(i));
                    break;
                }
            }
            if(v == -1) {
                continue;
            } else {
                fleet.remove(v);
                paths.remove(a_path);
                paths.remove(b_path);
                paths.add(a_path.merge(b_path));
            }
        }
    }
}
