package algorithms;

import java.util.ArrayList;
import java.util.List;

public class Individual implements Comparable<Individual>{
    List<Integer> routes;
    int fitness;

    public Individual() {
        this.routes = new ArrayList<>();
        this.fitness = -1;
    }

    public Individual(List<Integer> routes) {
        this.routes = routes;
    }

    public void addr(int s) {
        routes.add(s);
    }

    public void setFitness(int fitness) {
        this.fitness = fitness;
    }

    public int getFitness() {
        return fitness;
    }

    @Override
    public String toString() {
        return "Ind{" +
                "routes='" + routes + '\'' +
                ", fitness='" + fitness + '\'' +
                '}';
    }

    @Override
    public int compareTo(Individual o) {
        return Integer.compare(o.fitness, this.fitness);
    }
}
