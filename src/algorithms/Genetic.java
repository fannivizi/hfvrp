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
        this.rand = new Random(32);
        this.population = new ArrayList<>();
        this.generations = generations;
    }

    public Individual correct(List<Route> routes) {
        List<Node> nodes = new ArrayList<>();
        List<Route> sol = new ArrayList<>();

        for(Route r: routes) {
            if(r.getVehicle() == null) {
                nodes.addAll(r.getNodes());
                nodes.remove(depot);
            } else {
                sol.add(r);
                while(r.demand() > r.getVehicle().getCapacity()) {
                    nodes.add(r.getNodes().remove(r.getNodes().size()-2));
                }
            }
        }
        correct(nodes, sol);
        if(!nodes.isEmpty()) {
            sol.add(new Route(null, new ArrayList<>(nodes)));
        }

        return code(sol);
    }

    //TODO: implement to correct paths after crossover
    public void correct(List<Node> nodes, List<Route> sol) {
        Iterator<Node> it = nodes.iterator();
        while (it.hasNext()) {
            Node n = it.next();

            for(Route r: sol) {
                if(r.demand() + n.getDemand() <= r.getVehicle().getCapacity()) {
                    r.addNode(n, 1);
                    it.remove();
                    break;
                }
            }
        }
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
        //see if remaining nodes fit anywhere

        /*Iterator<Node> it = nodes.iterator();
        while (it.hasNext()) {
            Node n = it.next();

            for(Route r: sol) {
                if(r.demand() + n.getDemand() <= r.getVehicle().getCapacity()) {
                    r.addNode(n, 1);
                    it.remove();
                    break;
                }
            }
        }*/
        correct(nodes, sol);

        //put rest of the nodes in a separate route, so they're the same length
        if(!nodes.isEmpty()) {
            Route r = new Route(null);
            r.addNode(depot);
            while(!nodes.isEmpty()) {
                r.addNode(nodes.removeFirst());
            }
            sol.add(r);
        }

        return sol;
    }

    //use the negative numbers to indicate the id of the route's vehicle (i.e -3 for vehicle3)
    public Individual code(List<Route> sol) {
        List<Vehicle> fleet = new ArrayList<>(this.fleet);
        Individual i = new Individual();
        for(Route r: sol) {
            for (int j = 1; j < r.getNodes().size(); j++) {
                if(r.getNodes().get(j).getIndex() == depot.getIndex()) {
                    if(r.getVehicle() == null) continue;
                    i.addr(-r.getVehicle().getIndex());
                    fleet.remove(r.getVehicle());
                } else {
                    i.addr(r.getNodes().get(j).getIndex());
                }
            }
        }
        for(Vehicle v: fleet) {
            i.addr(-v.getIndex());
        }
        i.setFitness(fitness(sol));
        return i;
    }

    //TODO: the routes have vehicles when the vehicle is over capacity too
    /*public List<Route> decode(Individual sol) {
        List<Route> res = new ArrayList<>();
        List<Integer> routes = new ArrayList<>(sol.routes);
        List<Node> nodes = new ArrayList<>(this.nodes);

        res.add(new Route(null));
        res.getLast().addNode(depot);

        //construct routes from array
        while(!routes.isEmpty()) {
            int i = routes.removeFirst();
            if(i < 0) {
                res.getLast().addNode(depot);
                for(Vehicle v: fleet) {
                    if(v.getIndex() == -i) {
                        if(res.getLast().demand() > v.getCapacity()) {
                            res.getLast().setVehicle(null);
                        } else {
                            res.getLast().setVehicle(v);
                        }
                        break;
                    }
                }
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
                        nodes.remove(n);
                        break;
                    }
                }
                res.getLast().addNode(node);
            }
        }

        return res;
    }*/
    public List<Route> decode(Individual sol) {
        List<Route> res = new ArrayList<>();
        List<Integer> routes = new ArrayList<>(sol.routes);

        //construct routes from array
        res.add(new Route(null));
        res.getFirst().addNode(depot);

        while(!routes.isEmpty()) {
            int i = routes.removeFirst();
            if(i < 0) {
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
        CWSavings cw = new CWSavings(fleet, nodes, depot);
        cw.run();
        for (int i = 0; i < population_size; i++) {
            if(rand.nextDouble() < 0.02) {
                population.add(code(cw.getRoutes()));
            } else {
                population.add(code(random_sol()));
            }
        }
    }

    //TODO: better fitness?
    public double fitness(List<Route> routes) {
        Statistics stats = new Statistics(routes);
        double num = stats.getCost() + (nodes.size()+1 - stats.getNode_num()) * 1000000;
        return num;
    }

    //TODO: minimize
    public List<Individual> roulette_selection() {
        Collections.sort(population);
        double sum = 0;
        double next = 0;
        List<Individual> parents = new ArrayList<>();
        for(Individual i: population) {
            sum += i.fitness;
        }

        for (int i = 0; i < population_size; i++) {
            next = rand.nextDouble(sum);
            double before = 0;
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

    public List<Individual> tournament_selection(int num) {
        List<Individual> parents = new ArrayList<>();
        for (int i = 0; i < population.size(); i++) {
            double min_f = 0;
            int min_i = -1;
            for (int j = 0; j < num; j++) {
                if(min_i == -1) {
                    min_i = rand.nextInt(population_size);
                    min_f = population.get(min_i).fitness;
                } else {
                    int next = rand.nextInt(population_size);
                    if(min_f > population.get(next).fitness) {
                        min_i = next;
                        min_f = population.get(next).fitness;
                    }
                }
            }
            parents.add(population.get(min_i));
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
        Arrays.fill(c1, 0);
        Arrays.fill(c2, 0);
        c1[0] = p1[0];
        c2[0] = p2[0];

        //copying the genes between the crossover points
        for (int i = start; i <= end; i++) {
            c1[i] = p1[i];
            c2[i] = p2[i];
        }

        //checking for genes that haven't been copied
        for (int i = start; i <= end; i++) {
            if(!Arrays.asList(c1).contains(p2[i])) {
                int new_i = Arrays.asList(p2).indexOf(c1[i]);
                while(c1[new_i] != 0) {
                    new_i = Arrays.asList(p2).indexOf(c1[new_i]);
                }
                c1[new_i] = p2[i];
            }

            if(!Arrays.asList(c2).contains(p1[i])) {
                int new_i = Arrays.asList(p1).indexOf(c2[i]);
                while(c2[new_i] != 0) {
                    new_i = Arrays.asList(p1).indexOf(c2[new_i]);
                }
                c2[new_i] = p1[i];
            }
        }

        //copying the rest from one parent
        for (int i = 0; i < c1.length; i++) {
            if(c1[i] == 0) {
                c1[i] = p2[i];
            }

            if(c2[i] == 0) {
                c2[i] = p1[i];
            }
        }

        children.add(c1);
        children.add(c2);
        return children;
    }

    //TODO: which one do we remove when depot?
    public List<Integer[]> ox_crossover(Individual parent1, Individual parent2) {
        List<Integer[]> children = new ArrayList<>();

        List<Integer> p1 = new ArrayList<>(parent1.routes);
        List<Integer> p2 = new ArrayList<>(parent2.routes);
        if(p1.size() != p2.size()) System.out.println("NOT SAME LENGTH!!!!!!!!!!!");

        //random crossover points
        int start = rand.nextInt(p1.size());
        int end = rand.nextInt(start, p1.size());

        //initialising children
        Integer[] c1 = new Integer[p1.size()];
        Integer[] c2 = new Integer[p2.size()];
        Arrays.fill(c1, 0);
        Arrays.fill(c2, 0);

        //copying the genes between the crossover points
        for (int i = start; i <= end; i++) {
            c1[i] = p1.get(i);
            c2[i] = p2.get(i);
        }

        for (int i = start; i <= end; i++) {
            p1.remove(c2[i]);
            p2.remove(c1[i]);
        }

        for (int i = 0; i < c1.length; i++) {
            if(c1[i] == 0) c1[i] = p2.removeFirst();
            if(c2[i] == 0) c2[i] = p1.removeFirst();
        }

        children.add(c1);
        children.add(c2);
        return children;
    }

    public List<Integer[]> cx_crossover(Individual parent1, Individual parent2) {
        List<Integer[]> children = new ArrayList<>();

        List<Integer> p1 = new ArrayList<>(parent1.routes);
        List<Integer> p2 = new ArrayList<>(parent2.routes);
        if(p1.size() != p2.size()) System.out.println("NOT SAME LENGTH!!!!!!!!!!!");

        //random crossover point
        int cross = rand.nextInt(p1.size());

        //initialising children
        Integer[] c1 = new Integer[p1.size()];
        Integer[] c2 = new Integer[p2.size()];

        //find the cycle
        List<Integer> cycle = new ArrayList<>();
        cycle.add(cross);
        int current = p2.indexOf(p1.get(cross));
        while(current != cross) {
            cycle.add(current);
            current = p2.indexOf(p1.get(current));
        }

        //fill spaces in children
        for (int i = 0; i < p1.size(); i++) {
            if(cycle.contains(i)){
                c1[i] = p1.get(i);
                c2[i] = p2.get(i);
            } else {
                c1[i] = p2.get(i);
                c2[i] = p1.get(i);
            }
        }

        children.add(c1);
        children.add(c2);
        return children;
    }

    public void swap_mutation(Individual i) {
        int first = rand.nextInt(1, i.routes.size());
        int second = rand.nextInt(1, i.routes.size());
        int num = i.routes.get(first);
        i.routes.set(first, i.routes.get(second));
        i.routes.set(second, num);
    }

    public void run() {
        List<Individual> parents;
        List<Individual> children = new ArrayList<>();
        List<Integer[]> temp = new ArrayList<>();

        population_init();
        double mean = 0;
        for (int i = 0; i < generations; i++) {
            children = new ArrayList<>();
            mean = 0;
            parents = tournament_selection(2);
            //TODO: figure out where to do the correction
            while(!parents.isEmpty()) {
                temp.addAll(cx_crossover(parents.removeFirst(), parents.removeFirst()));
            }
            while(!temp.isEmpty()) {
                Individual ind = new Individual(Arrays.asList(temp.removeFirst()));
                if(rand.nextDouble() < 0.1) swap_mutation(ind);
                List<Route> decoded = decode(ind);
                //doesn't worg
                //ind = correct(decoded);
                ind.setFitness(fitness(decoded));
                children.add(ind);

                mean += ind.getFitness();
            }
            population = children;
            if((i+1)%100 == 0) {
                Collections.sort(children);
                System.out.println(i+1 + ".gen: " + children.getLast().fitness + ", mean: " + mean/children.size());
            }
        }
        Collections.sort(population);
        //System.out.println(new Statistics(decode(population.getFirst())));
        Collections.reverse(population);
        System.out.println(new Statistics(decode(population.getFirst())));
    }
}
//TODO: because of the coding and decoding we disregard the capacity of the vehicle, the demand goes above the capacity
//TODO: pmx crossover has duplicates