package algorithms;

import model.Node;
import model.Route;
import model.Vehicle;
import utils.Statistics;

import java.util.*;

public class Genetic {
    private int population_size;
    private List<Vehicle> fleet;
    private List<Node> nodes;
    private Node depot;
    private Random rand;
    private List<Individual> population;
    private int generations;

    public Genetic(List<Vehicle> fleet, List<Node> nodes, Node depot, int population_size, int generations) {
        this.population_size = population_size;
        this.fleet = new ArrayList<>(fleet);
        nodes.remove(depot);
        this.nodes = new ArrayList<>(nodes);
        this.depot = depot;
        this.rand = new Random();
        this.population = new ArrayList<>();
        this.generations = generations;
    }

    //TODO: doesn't go to all nodes, except for the big graphs?
    public List<Route> random_sol() {
        List<Route> sol = new ArrayList<>();
        Collections.shuffle(fleet);
        List<Node> nodes = new ArrayList<>(this.nodes);
        for(Vehicle v: fleet) {
            Route r = new Route(v);
            r.addNode(depot);
            if(!nodes.isEmpty()) {
                Node n = nodes.get(rand.nextInt(nodes.size()));
                while(r.demand() + n.getDemand() <= v.getCapacity()) {
                    nodes.remove(n);
                    r.addNode(n);
                    if(!nodes.isEmpty()) n = nodes.get(rand.nextInt(nodes.size()));
                    else break;
                }
            }
            r.addNode(depot);
            sol.add(r);
        }
        //TODO: maybe see if remaining nodes fit anywhere?
        System.out.println(nodes.size());
        return sol;
    }

    public Individual code(List<Route> sol) {
        Individual i = new Individual();
        i.addr(1);
        for(Route r: sol) {
            for (int j = 1; j < r.getNodes().size(); j++) {
                i.addr(r.getNodes().get(j).getIndex());
            }
        }
        i.setFitness(fitness(sol));
        return i;
    }

    public List<Route> decode(Individual sol) {
        List<Route> res = new ArrayList<>();
        List<Integer> routes = new ArrayList<>(sol.routes);

        //construct routes from array
        routes.removeFirst();
        res.add(new Route(null));
        res.getFirst().addNode(depot);

        while(!routes.isEmpty()) {
            int i = routes.removeFirst();
            if(i == depot.getIndex()) {
                res.getLast().addNode(depot);
                if(!routes.isEmpty()) {
                    res.add(new Route(null));
                    res.getLast().addNode(depot);
                }
            } else {
                //TODO: better way to find the node
                Node node = null;
                for(Node n: nodes) {
                    if(n.getIndex() == i) {
                        node = n;
                        break;
                    }
                }
                res.getLast().addNode(node);
            }
        }

        //pair vehicles to routes
        List<Vehicle> temp_fleet = new ArrayList<>(fleet);
        Collections.sort(temp_fleet);
        for(Route r: res) {
            for(Vehicle v: temp_fleet) {
                if(v.getCapacity() > r.demand()) {
                    r.setVehicle(v);
                    temp_fleet.remove(v);
                    break;
                }
            }
        }

        return res;
    }

    public void population_init() {
        for (int i = 0; i < population_size; i++) {
            population.add(code(random_sol()));
        }
    }

    public int fitness(List<Route> routes) {
        Statistics stats = new Statistics(routes);
        return stats.getCost() + (nodes.size() - stats.getNode_num()) * 10000;
    }

    //TODO: I'm maximizing fitnes waaaaaaaaaa
    public List<Individual> roulette_selection() {
        Collections.sort(population);
        int sum = 0;
        int next = 0;
        List<Individual> parents = new ArrayList<>();
        for(Individual i: population) {
            sum += i.fitness;
        }

        for (int i = 0; i < population_size; i++) {
            next = rand.nextInt(sum);
            int before = 0;
            for(Individual ind: population) {
                before += ind.fitness;
                if(before >= next) {
                    parents.add(ind);
                    break;
                }
            }
        }
        return parents;
    }

    public List<Integer[]> pmx_crossover(Individual parent1, Individual parent2) {
        List<Integer[]> children = new ArrayList<>();

        Integer[] p1 = parent1.routes.toArray(new Integer[0]);
        Integer[] p2 = parent2.routes.toArray(new Integer[0]);
        if(p1.length != p2.length) System.out.println("NOT SAME LENGTH!!!!!!!!!!!");

        //random crossover points
        int start = rand.nextInt(p1.length);
        int end = rand.nextInt(start, p1.length);

        //initialising children
        Integer[] c1 = new Integer[p1.length];
        Integer[] c2 = new Integer[p2.length];
        Arrays.fill(c1, -1);
        Arrays.fill(c2, -1);

        //copying the genes between the crossover points
        for (int i = start; i <= end; i++) {
            c1[i] = p1[i];
            c2[i] = p2[i];
        }

        //checking for genes that haven't been copied
        for (int i = start; i <= end; i++) {
            if(!Arrays.asList(c1).contains(p2[i])) {
                int new_i = Arrays.asList(p2).indexOf(c1[i]);
                while(c1[new_i] != -1) {
                    new_i = Arrays.asList(p2).indexOf(c1[new_i]);
                }
                c1[new_i] = p2[i];
            }

            if(!Arrays.asList(c2).contains(p1[i])) {
                int new_i = Arrays.asList(p1).indexOf(c2[i]);
                while(c2[new_i] != -1) {
                    new_i = Arrays.asList(p1).indexOf(c2[new_i]);
                }
                c2[new_i] = p1[i];
            }
        }

        //copying the rest from one parent
        for (int i = 0; i < c1.length; i++) {
            if(c1[i] == -1) {
                c1[i] = p2[i];
            }

            if(c2[i] == -1) {
                c2[i] = p1[i];
            }
        }

        children.add(c1);
        children.add(c2);
        return children;
    }

    public void swap_mutation(Individual i) {
        int first = rand.nextInt(i.routes.size());
        int second = rand.nextInt(i.routes.size());
        int num = i.routes.get(first);
        i.routes.set(first, i.routes.get(second));
        i.routes.set(second, num);
    }

    public void run() {
        List<Individual> parents = new ArrayList<>();
        List<Individual> children = new ArrayList<>();
        List<Integer[]> temp = new ArrayList<>();

        population_init();
        for (int i = 0; i < generations; i++) {
            parents = roulette_selection();
            while(!parents.isEmpty()) {
                temp.addAll(pmx_crossover(parents.removeFirst(), parents.removeFirst()));
                children.add(new Individual(Arrays.asList(temp.removeFirst())));
                children.add(new Individual(Arrays.asList(temp.removeFirst())));
            }
        }
    }
}
