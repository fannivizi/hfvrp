package algorithms;

import model.Node;
import model.Route;
import model.Vehicle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CWSavings {
    private List<Vehicle> fleet;
    private List<Node> nodes;
    private Node depot;
    private List<Route> routes;

    public CWSavings(List<Vehicle> fleet, List<Node> nodes, Node depot) {
        this.fleet = new ArrayList<>(fleet);
        this.nodes = new ArrayList<>(nodes);
        this.depot = depot;
        this.routes = new ArrayList<>();
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public boolean interior(Route p, Node n) {
        List<Node> nodes = p.getNodes();
        if(nodes.get(1) == n || nodes.get(nodes.size()-2) == n) return false;
        return true;
    }

    public void merge(Route a, Route b, Node node_a, Node node_b, Vehicle v) {
        Route p;

        //remove routes from the routes list
        routes.remove(a);
        routes.remove(b);

        p = new Route(v);

        //see where the nodes are in the routes
        boolean a_first = false, b_first = false;
        if(a.getNodes().get(1) == node_a) a_first = true;
        if(b.getNodes().get(1) == node_b) b_first = true;

        if(a_first) a.reverse();
        if(!b_first) b.reverse();

        //merge the routes
        p.addNode(a.getNodes().getFirst());
        for (int i = 1; i < a.getNodes().size()-1; i++) {
            p.addNode(a.getNodes().get(i));
        }
        for (int i = 1; i < b.getNodes().size()-1; i++) {
            p.addNode(b.getNodes().get(i));
        }
        p.addNode(a.getNodes().getLast());

        //add new route to the list
        routes.add(p);
    }

    public void run() {
        nodes.remove(depot);

        //create route depot -> node -> depot
        for(Node node: nodes) {
            routes.add(new Route(null, List.of(depot, node, depot)));
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

        //merge routes with biggest savings
        for(Saving saving: savings) {
            Node a, b;
            Route a_route = null, b_route = null;
            a = saving.getA();
            b = saving.getB();

            //find the route of the nodes
            for(Route p: routes) {
                if(p.getNodes().contains(a)) a_route = p;
                if(p.getNodes().contains(b)) b_route = p;
            }

            //if on same route or interior, skip
            if(a_route == b_route || interior(a_route, a) || interior(b_route, b)) continue;

            //if both routes have a vehicle
            if(a_route.getVehicle() != null && b_route.getVehicle() != null) continue;

            //if one route has a vehicle
            if(a_route.getVehicle() != null || b_route.getVehicle() != null) {
                if(a_route.getVehicle() == null) {
                    if(a_route.demand() + b_route.demand() <= b_route.getVehicle().getCapacity()) {
                        merge(a_route, b_route, a, b, b_route.getVehicle());
                    }
                } else {
                    if(a_route.demand() + b_route.demand() <= a_route.getVehicle().getCapacity()) {
                        merge(a_route, b_route, a, b, a_route.getVehicle());
                    }
                }
                continue;
            }

            //if neither has a vehicle
            for (int i = 0; i < fleet.size(); i++) {
                Vehicle v = fleet.get(i);
                if(a_route.demand() + b_route.demand() <= v.getCapacity()) {
                    fleet.remove(v);
                    merge(a_route, b_route, a, b, v);
                    break;
                }
            }
        }
        //if there are empty vehicles, use them
        Collections.reverse(routes);
        for(Vehicle v: fleet) {
            for(Route p: routes) {
                if(p.getVehicle() == null && p.demand() <= v.getCapacity())  {
                    p.setVehicle(v);
                    break;
                }
            }
        }
    }
}
