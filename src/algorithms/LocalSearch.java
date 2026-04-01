package algorithms;

import model.Node;
import model.Route;

import java.util.ArrayList;
import java.util.List;

public class LocalSearch {
    private List<Route> best;
    private List<Route> original;

    public LocalSearch(List<Route> routes) {
        this.best = new ArrayList<>();
        this.original = new ArrayList<>();
        for(Route r: routes) {
            this.best.add(new Route(r));
            this.original.add(new Route(r));
        }
        search();
    }

    public List<Route> getBest() {
        return best;
    }

    public double route_cost(List<Route> routes) {
        double cost = 0;
        for(Route r: routes) {
            cost += r.totalCost();
        }
        return cost;
    }

    /*public void swap_all() {
        for(int i = 0; i < original.size(); i++) {
            for (int j = 1; j < original.get(i).getNodes().size()-2; j++) {
                for (int k = j+1; k < original.get(i).getNodes().size()-1; k++) {
                    Route copy = new Route(original.get(i));
                    swap(copy, j, k);

                    if(best.get(i).totalCost() > copy.totalCost()) best.set(i, copy);
                }
            }
        }
    }*/

    public void swap_all() {
        for (int i = 0; i < original.size(); i++) {
            for (int j = 0; j < original.size(); j++) {
                for (int k = 1; k < original.get(i).getNodes().size()-1; k++) {
                    for (int l = 1; l < original.get(j).getNodes().size()-1; l++) {
                        List<Route> copy = new ArrayList<>();
                        for(Route r: original) {
                            copy.add(new Route(r));
                        }

                        if(i == j) {
                            if(l < k+1) continue;
                            Route r = copy.get(i);
                            swap(r, k, l);
                        } else {
                            Route r1 = copy.get(i);
                            Route r2 = copy.get(j);
                            swap_between(r1, r2, k, l);
                        }

                        if(route_cost(copy) < route_cost(best)) best = copy;
                    }
                }
            }
        }
    }

    public void swap(Route r, int n1, int n2) {
        Node n = r.getNodes().get(n1);
        r.getNodes().set(n1, r.getNodes().get(n2));
        r.getNodes().set(n2, n);
    }

    public void swap_between(Route r1, Route r2, int i, int j) {
        Node n1 = r1.getNodes().get(i);
        Node n2 = r2.getNodes().get(j);

        if(r1.getVehicle().getCapacity() >= r1.demand()-n1.getDemand()+n2.getDemand() && r2.getVehicle().getCapacity() >= r2.demand()-n2.getDemand()+n1.getDemand()) {
            r1.getNodes().remove(n1);
            r2.getNodes().remove(n2);
            r1.addNode(n2, i);
            r2.addNode(n1, j);
        }
    }

    public void move_all() {
        for (int i = 0; i < original.size()-1; i++) {
            for (int j = i+1; j < original.size(); j++) {
                for (int k = 1; k < original.get(i).getNodes().size()-1; k++) {

                    List<Route> copy = new ArrayList<>();
                    for(Route r: original) {
                        copy.add(new Route(r));
                    }

                    Route r1 = copy.get(i);
                    Route r2 = copy.get(j);
                    move_between(r1, r2, k);

                    if(route_cost(copy) < route_cost(best)) best = copy;
                }
            }
        }
    }

    public void move_between(Route r1, Route r2, int n) {
        if(r2.getVehicle().getCapacity() >= r2.demand() + r1.getNodes().get(n).getDemand()) {
            r2.addNode(r1.getNodes().remove(n), 1);

            Route best = new Route(r2);
            for (int i = 2; i < r2.getNodes().size()-1; i++) {
                Route copy = new Route(r2);
                swap(copy, 1, i);
                if(copy.totalCost() < best.totalCost()) best = copy;
            }
            r2 = best;
        }
    }

    public void search() {
        double previous = 0;
        double cost = route_cost(original);
        int i = 0;

        while(previous != cost && i < 10) {
            previous = cost;

            swap_all();
            move_all();

            cost = route_cost(best);
            original = best;
            i++;
            System.out.println(i + ". iteration: " + route_cost(best));
        }
    }
}
