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
        Collections.sort(fleet);
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

    public Route merge(Route a, Route b) {
        Route p;

        //decide which vehicle to use
        if(a.getVehicle() == null && b.getVehicle()== null) {
            p = new Route(null);
        } else if (a.getVehicle() == null) {
            p = new Route(b.getVehicle());
        } else if (b.getVehicle() == null) {
            p = new Route(a.getVehicle());
        } else {
            if(a.getVehicle().compareTo(b.getVehicle()) >= 0) {
                p = new Route(a.getVehicle());
            } else {
                p = new Route(b.getVehicle());
            }
        }

        //merge the routes
        p.addNode(a.getNodes().getFirst());
        for (int i = 1; i < a.getNodes().size()-1; i++) {
            p.addNode(a.getNodes().get(i));
        }
        for (int i = 1; i < b.getNodes().size()-1; i++) {
            p.addNode(b.getNodes().get(i));
        }
        p.addNode(a.getNodes().getLast());
        return p;
    }

    public List<Integer> cap_options() {
        List<Integer>  l = new ArrayList<>();
        for(Vehicle v: fleet) {
            if(!l.contains(v.getCapacity())) l.add(v.getCapacity());
        }
        Collections.sort(l);
        return l;
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

        //merge routes with biggest savings -- fucked
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
                        routes.remove(a_route);
                        routes.remove(b_route);
                        routes.add(merge(a_route, b_route));
                    }
                } else {
                    if(a_route.demand() + b_route.demand() <= a_route.getVehicle().getCapacity()) {
                        routes.remove(a_route);
                        routes.remove(b_route);
                        routes.add(merge(a_route, b_route));
                    }
                }
                continue;
            }

            //if neither has a vehicle
            if(cap_options().size() > 1 && a_route.demand() + b_route.demand() <= cap_options().getLast()) {
                // if the demand is bigger than the second biggest capacity
                if(a_route.demand() + b_route.demand() > cap_options().get(cap_options().size()-2)) {
                    Route p = merge(a_route, b_route);
                    p.setVehicle(fleet.getLast());
                    fleet.remove(fleet.getLast());
                    routes.remove(a_route);
                    routes.remove(b_route);
                    routes.add(p);
                } else {
                    routes.remove(a_route);
                    routes.remove(b_route);
                    routes.add(merge(a_route, b_route));
                }
            } else if(cap_options().size() == 1) {
                //if all the vehicles have the same capacity
                if(a_route.demand() + b_route.demand() <= cap_options().getFirst()) {
                    Route p = merge(a_route, b_route);
                    p.setVehicle(fleet.getLast());
                    fleet.remove(fleet.getLast());
                    routes.remove(a_route);
                    routes.remove(b_route);
                    routes.add(p);
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
