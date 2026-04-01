package algorithms;

import model.Node;
import model.Route;
import model.Vehicle;
import utils.Statistics;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class GeneticSplit extends Genetic{
    public GeneticSplit(List<Vehicle> fleet, List<Node> nodes, Node depot, int population_size, int generations, int seed) {
        super(fleet, nodes, depot, population_size, generations, seed);
    }

    @Override
    public Individual code(List<Route> sol) {
        Individual i = new Individual();

        for(Route r: sol) {
            for (int j = 1; j < r.getNodes().size(); j++) {
                //if we aren't at depot, write the node index
                if(r.getNodes().get(j).getIndex() != depot.getIndex()) {
                    i.addr(r.getNodes().get(j).getIndex());
                }
            }
        }

        //set the fitness value
        i.setFitness(fitness(sol));

        return i;
    }

    public List<Node> decoder(Individual sol) {
        List<Node> copy = new ArrayList<>(nodes);
        List<Node> re = new ArrayList<>();

        for(int i: sol.routes) {
            for(Node n: copy) {
                if(i == n.getIndex()) {
                    re.add(n);
                    copy.remove(n);
                    break;
                }
            }
        }

        return re;
    }

    public double cost(List<Node> nodes, Vehicle v) {
        double cost = v.getFixed_cost() + nodes.getFirst().distance(depot)*v.getUnit_distance_cost() + nodes.getLast().distance(depot)*v.getUnit_distance_cost();
        for (int i = 0; i < nodes.size()-1; i++) {
            cost += nodes.get(i).distance(nodes.get(i+1)) * v.getUnit_distance_cost();
        }
        return cost;
    }

    public int demand(List<Node> nodes) {
        int demand = 0;
        for (Node n: nodes) {
            demand += n.getDemand();
        }
        return demand;
    }

    public List<Route> split(List<Node> nodes) {
        Collections.sort(fleet);
        double[] dp = new double[nodes.size()+1];
        int[] prev = new int[nodes.size()+1];
        int[] vehicles = new int[nodes.size()+1];
        List<Integer> prev_vehicles = new ArrayList<>();
        List<Route> sol = new ArrayList<>();

        int demand = 0;

        for (int j = 1; j <= nodes.size(); j++) {
            dp[j] = Double.MAX_VALUE;
            for (int i = 0; i < j; i++) {
                demand = demand(nodes.subList(i, j));

                int k = i;
                prev_vehicles.clear();
                while (k != 0) {
                    prev_vehicles.add(vehicles[k]);
                    k = prev[k];
                }
                for(Vehicle v: fleet) {
                    if(demand <= v.getCapacity() && !prev_vehicles.contains(v.getIndex())) {
                        double cost = dp[i] + cost(nodes.subList(i, j), v);
                        if(cost < dp[j]) {
                            dp[j] = cost;
                            prev[j] = i;
                            vehicles[j] = v.getIndex();
                        }
                        break;
                    }
                }
            }
        }

        List<Node> rogue = new ArrayList<>();
        int last = nodes.size();
        while(vehicles[last] == 0) {
            rogue.add(nodes.get(last-1));
            last--;
        }

        List<Integer> path = new ArrayList<>();
        List<Integer> v = new ArrayList<>();
        int i = last;
        while(i != 0) {
            v.add(vehicles[i]);
            path.add(i);
            i = prev[i];
        }
        path.add(0);
        Collections.reverse(path);
        Collections.reverse(v);

        for (int j = 1; j < path.size(); j++) {
            for(Vehicle vehicle: fleet) {
                if(v.get(j-1) == vehicle.getIndex()) {
                    sol.add(new Route(vehicle, nodes.subList(path.get(j-1), path.get(j))));
                    break;
                }
            }
            sol.getLast().addNode(depot);
            sol.getLast().addNode(depot, 0);
        }

        correct(rogue, sol);

        return sol;
    }

    @Override
    public Individual evaluate(Individual ind) {
        List<Route> decoded = new ArrayList<>();
        decoded = decode(ind);

        ind = code(decoded);
        ind.setFitness(fitness(decoded));
        return ind;
    }

    @Override
    public List<Route> decode(Individual sol) {
        List<Node> nodes = decoder(sol);
        return split(nodes);
    }
}

//why so slow and sucky?
