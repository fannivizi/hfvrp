package utils;

import model.Route;

import java.util.List;

public class Statistics {
    private List<Route> routes;
    private int vehicle_num;
    private int node_num;
    private int distance;
    private double efficiency;
    private int cost;

    public Statistics(List<Route> routes) {
        this.routes = routes;
        this.vehicle_num = 0;
        this.node_num = 0;
        this.distance = 0;
        this.efficiency = 0;
        this.cost = 0;

        for(Route route : routes) {
            if(route.distance() != 0 && route.getVehicle() != null) {
                vehicle_num += 1;
                node_num += route.getNodes().size();
                distance += route.distance();
                efficiency += route.efficiency();
                cost += route.totalCost();
            }
        }
        node_num = node_num - vehicle_num*2 + 1;
        efficiency /= vehicle_num;
    }

    public int getVehicle_num() {
        return vehicle_num;
    }

    public int getNode_num() {
        return node_num;
    }

    public int getCost() {
        return cost;
    }

    @Override
    public String toString() {
        return "utils.Statistics: \n" +
                "Vehicles used: " + vehicle_num +
                "\nNodes visited: " + node_num +
                "\nDistance traveled: " + distance +
                "\nEfficiency: " + efficiency +
                "\nTotal cost: " + cost;
    }
}
