package algorithms;

import java.util.ArrayList;
import java.util.List;

public class Individual implements Comparable<Individual>{
    List<Integer> routes;
    double fitness;

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

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public double getFitness() {
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
        return Double.compare(o.fitness, this.fitness);
    }
}
