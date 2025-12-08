package model;

public class Vehicle implements Comparable<Vehicle> {
    private int index;
    private int capacity;
    private int fixed_cost;
    private int unit_distance_cost;

    public Vehicle(int index, int capacity) {
        this.index = index;
        this.capacity = capacity;
        this.fixed_cost = 0;
        this.unit_distance_cost = 0;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getFixed_cost() {
        return fixed_cost;
    }

    public void setFixed_cost(int fixed_cost) {
        this.fixed_cost = fixed_cost;
    }

    public int getUnit_distance_cost() {
        return unit_distance_cost;
    }

    public void setUnit_distance_cost(int unit_distance_cost) {
        this.unit_distance_cost = unit_distance_cost;
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "index=" + index +
                ", capacity=" + capacity +
                ", fixed_cost=" + fixed_cost +
                ", unit_distance_cost=" + unit_distance_cost +
                '}';
    }

    @Override
    public int compareTo(Vehicle o) {
        return Integer.compare(this.getCapacity(), o.getCapacity());
    }
}
