package algorithms;

import model.Node;
import model.Route;
import model.Vehicle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class NearestNeighbor {
    private List<Vehicle> fleet;
    private List<Node> nodes;
    private Node depot;
    private List<Route> routes;
    private Random random;

    public NearestNeighbor(List<Vehicle> fleet, List<Node> nodes, Node depot) {
        this.fleet = new ArrayList<>(fleet);
        Collections.sort(fleet);
        this.nodes = new ArrayList<>(nodes);
        this.depot = depot;
        this.random = new Random();
        this.routes = new ArrayList<>();
    }

    public List<Route> run() {
        Vehicle v;
        int capacity;
        while(!nodes.isEmpty() && !fleet.isEmpty()) {
            Node current = depot;
            nodes.remove(current);
            v = fleet.getFirst();
            //v = fleet.get(random.nextInt(fleet.size()));
            fleet.remove(v);
            capacity = 0;
            Route route = new Route(v);
            route.addNode(depot);
            while(!nodes.isEmpty()) {
                //Node nearest = nodes.getFirst();
                boolean first = true;
                Node nearest = new Node(0, 0, 0);
                for (Node node: nodes) {
                    if(first || (current.distance(nearest) > current.distance(node) /*&& capacity + node.getDemand() <= v.getCapacity()*/)) {
                        first = false;
                        nearest = node;
                    }
                }
                capacity += nearest.getDemand();
                if(capacity >= v.getCapacity()) {
                    route.addNode(depot);
                    routes.add(route);
                    break;
                }
                route.addNode(nearest);
                current = nearest;
                nodes.remove(current);
            }
        }
        return routes;
    }
}
