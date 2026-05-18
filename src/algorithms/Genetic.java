package algorithms;

import model.Node;
import model.Route;
import model.Vehicle;
import utils.Statistics;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class Genetic {
    private int population_size;
    private List<Vehicle> fleet;
    private List<Node> nodes;
    private Node depot;
    private Random rand;
    private int seed;
    private List<Individual> population;
    private final int generations;
    private final boolean savings;
    private double mutation_rate;

    public Genetic(List<Vehicle> fleet, List<Node> nodes, Node depot, int population_size, int generations, int seed, boolean savings) {
        this.population_size = population_size;
        this.fleet = new ArrayList<>(fleet);
        nodes.remove(depot);
        this.nodes = new ArrayList<>(nodes);
        this.depot = depot;
        this.rand = new Random(seed);
        this.population = new ArrayList<>();
        this.generations = generations;
        this.savings = savings;
        this.seed = seed;
        if(savings) this.mutation_rate=0.15;
        else this.mutation_rate=0.025;
    }

    public void correct(List<Node> nodes, List<Route> sol) {
        Iterator<Node> it = nodes.iterator();

        //put free nodes in the route if they fit
        while (it.hasNext()) {
            Node n = it.next();

            for(Route r: sol) {
                if(r.getVehicle() != null && r.demand() + n.getDemand() <= r.getVehicle().getCapacity()) {
                    r.addNode(n, r.getNodes().size()-1);
                    it.remove();
                    break;
                }
            }
        }

        //put rest of the nodes in a separate route, so the coded routes are the same length
        if(!nodes.isEmpty()) {
            Route r = new Route((Vehicle) null);
            r.addNode(depot);
            while(!nodes.isEmpty()) {
                r.addNode(nodes.removeFirst());
            }
            sol.add(r);
        }
    }

    //generate random solution
    public List<Route> random_sol() {
        List<Route> sol = new ArrayList<>();
        Collections.shuffle(fleet, rand);
        List<Node> nodes = new ArrayList<>(this.nodes);
        //send vehicle to a random node until it has space
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
        correct(nodes, sol);

        return sol;
    }

    //convert list of Routes to an individual
    //use the negative numbers to indicate the id of the route's vehicle (i.e -3 for vehicle3)
    public Individual code(List<Route> sol) {
        List<Vehicle> fleet = new ArrayList<>(this.fleet);
        Individual i = new Individual();

        for(Route r: sol) {
            for (int j = 1; j < r.getNodes().size(); j++) {
                //if we are at the depot, code the index of the vehicle into the individual to mark the end of the route
                if(r.getNodes().get(j).getIndex() == depot.getIndex()) {
                    if(r.getVehicle() == null) continue;
                    i.addr(-r.getVehicle().getIndex());
                    fleet.remove(r.getVehicle());
                //if we are at a node, just put the index of the node
                } else {
                    i.addr(r.getNodes().get(j).getIndex());
                }
            }
        }

        //put the index of the remaining vehicles at the end, so all the coded solutions are the same length
        for(Vehicle v: fleet) {
            i.addr(-v.getIndex());
        }

        //set the fitness value
        i.setFitness(fitness(sol));

        return i;
    }

    //convert individual to a list of Routes
    public List<Route> decode(Individual sol) {
        List<Route> res = new ArrayList<>();
        List<Integer> routes = new ArrayList<>(sol.routes);
        List<Node> nodes = new ArrayList<>(this.nodes);

        //create an empty route
        res.add(new Route((Vehicle) null));
        res.getLast().addNode(depot);

        //construct routes from array
        while(!routes.isEmpty()) {
            int i = routes.removeFirst();
            //if we are at the end of a route
            if(i < 0) {
                //end the previous route
                res.getLast().addNode(depot);
                for(Vehicle v: fleet) {
                    if(v.getIndex() == -i) {
                        res.getLast().setVehicle(v);
                        break;
                    }
                }
                //if we have more nodes, create new route
                if(!routes.isEmpty()) {
                    res.add(new Route((Vehicle) null));
                    res.getLast().addNode(depot);
                }
            } else {
                //find the node and add it to the route
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
    }

    //generate the initial population
    public void population_init() {
        CWSavings cw = new CWSavings(fleet, nodes, depot);
        cw.run();
        for (int i = 0; i < population_size; i++) {
            //for random ga, set to 0, for improving savings, set, to 0.1
            if(rand.nextDouble() < (savings?0.15:0)) {
                population.add(code(cw.getRoutes()));
            } else {
                population.add(code(random_sol()));
            }
        }
    }

    //calculate fitness
    public double fitness(List<Route> routes) {
        Statistics stats = new Statistics(routes);
        return stats.getCost() + (nodes.size()+1 - stats.getNode_num()) * 100000;
    }

    //values too similar for roulette, for maximizing
    public List<Individual> roulette_selection() {
        Collections.sort(population);
        double sum = 0;
        double next;

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

        //select random individuals from the population and choose the one with the smallest fitness
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

    public List<Individual> first_n_selection(int i) {
        List<Individual> population = new ArrayList<>(this.population);
        Collections.sort(population);
        List<Individual> parents = new ArrayList<>();
        for (int j = 0; j < i; j++) {
            parents.addAll(population.subList(0, population.size()/i));
        }
        while(parents.size() != population_size) {
            parents.add(population.getFirst());
        }
        Collections.shuffle(parents, rand);
        return parents;
    }

    //produces duplicates
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
        int first = rand.nextInt(i.routes.size());
        int second = rand.nextInt(i.routes.size());
        int num = i.routes.get(first);
        i.routes.set(first, i.routes.get(second));
        i.routes.set(second, num);
    }

    public void scramble_mutation(Individual i) {
        int start = rand.nextInt(i.routes.size());
        int end = rand.nextInt(start, i.routes.size());

        Collections.shuffle(i.routes.subList(start, end), rand);
    }

    public void inversion_mutation(Individual i) {
        int start = rand.nextInt(i.routes.size());
        int end = rand.nextInt(start, i.routes.size());

        Collections.reverse(i.routes.subList(start, end));
    }

    public Individual evaluate(Individual ind) {
        List<Route> decoded = decode(ind);
        List<Node> rogue = new ArrayList<>();

        //repair overloaded routes
        Iterator<Route> it = decoded.iterator();
        while(it.hasNext()) {
            Route r = it.next();
            if(r.getVehicle() != null) {
                while(r.demand() > r.getVehicle().getCapacity()) {
                    rogue.add(r.getNodes().remove(r.getNodes().size()-2));
                }
            } else {
                while(r.getNodes().contains(depot)) r.getNodes().remove(depot);
                rogue.addAll(r.getNodes());
                it.remove();
            }

        }

        //put nodes into separate routes if possible
        correct(rogue, decoded);

        //set fitness value
        ind = code(decoded);
        return ind;
    }

    public void run() throws FileNotFoundException {
        List<Individual> parents;
        List<Individual> children;
        List<Integer[]> temp = new ArrayList<>();
        List<Double> bestFitness = new ArrayList<>();
        List<Double> meanFitness = new ArrayList<>();
        double mean;

        //initialise population
        population_init();

        //generation loop
        for (int i = 0; i < generations; i++) {
            children = new ArrayList<>();
            mean = 0;

            //select parents
            if(savings) {
                parents = first_n_selection(3);
            } else {
                parents = tournament_selection(population_size/20);
            }

            //crossover
            while(!parents.isEmpty()) {
                if(savings) {
                    temp.addAll(cx_crossover(parents.removeFirst(), parents.removeFirst()));
                } else {
                    temp.addAll(ox_crossover(parents.removeFirst(), parents.removeFirst()));
                }
            }

            //mutation, repair
            while(!temp.isEmpty()) {
                Individual ind = new Individual(Arrays.asList(temp.removeFirst()));
                double r = rand.nextDouble();
                //for random ga, set to 0.025, for improving savings, set to 0.2
                if(r < mutation_rate) {
                    swap_mutation(ind);
                }
                if(savings) mutation_rate+=0.001/population_size;
                else mutation_rate+=0.001/population_size/2;
                ind = evaluate(ind);
                children.add(ind);

                mean += ind.getFitness();
            }

            //swap the new population for the children
            population = children;

            //calculate and store mean and best
            Collections.sort(children);
            double best = children.getFirst().fitness;
            bestFitness.add(best);
            mean = mean/children.size();
            meanFitness.add(mean);

            //print mean and best
            if((i+1)%50 == 0) {
                System.out.println(i+1 + ".gen: " + best + ", mean: " + mean);
            }
        }

        if(!savings) {
            PrintWriter writer = new PrintWriter("fitness_" + seed + ".csv");

            writer.println("generation,best,mean");

            for (int i = 0; i < bestFitness.size(); i++) {
                writer.println(i + "," + bestFitness.get(i) + "," + meanFitness.get(i));
            }

            writer.close();
        }
    }

    public List<Route> getBest() {
        Collections.sort(population);
        return decode(population.getFirst());
    }
}
