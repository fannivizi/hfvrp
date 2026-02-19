package utils;

import model.Node;
import model.Vehicle;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
public class Reader {
    private List<Node> nodes = new ArrayList<>();
    private List<Vehicle> fleet = new ArrayList<>();
    private Node depot;
    public void read(String file) {
        File input = new File(file);

        try (Scanner reader = new Scanner(input)) {
            int num_nodes = 0;
            int num_vehicles = 0;

            while(reader.hasNext()) {
                String line = reader.next();
                switch (line) {
                    case "DIMENSION:":
                        num_nodes = reader.nextInt();
                        break;
                    case "VEHICLES:":
                        num_vehicles = reader.nextInt();
                        break;
                    case "NODE_COORD_SECTION":
                        for (int i = 0; i < num_nodes; i++) {
                            Node node = new Node(reader.nextInt(), reader.nextInt(), reader.nextInt());
                            nodes.add(node);
                        }
                        break;
                    case "DEMAND_SECTION":
                        for (int i = 0; i < num_nodes; i++) {
                            reader.nextInt();
                            Node node = nodes.get(i);
                            node.setDemand(reader.nextInt());
                        }
                        break;
                    case "CAPACITY_SECTION":
                        for (int i = 0; i < num_vehicles; i++) {
                            Vehicle vehicle = new Vehicle(reader.nextInt(), reader.nextInt());
                            fleet.add(vehicle);
                        }
                        break;
                    case "VEHICLES_FIXED_COST_SECTION":
                        for (int i = 0; i < num_vehicles; i++) {
                            reader.nextInt();
                            Vehicle vehicle = fleet.get(i);
                            vehicle.setFixed_cost(reader.nextInt()/10);
                        }
                        break;
                    case "VEHICLES_UNIT_DISTANCE_COST_SECTION":
                        for (int i = 0; i < num_vehicles; i++) {
                            reader.nextInt();
                            Vehicle vehicle = fleet.get(i);
                            vehicle.setUnit_distance_cost(reader.nextInt()/10);
                        }
                        break;
                    case "DEPOT_SECTION":
                        int index = reader.nextInt();
                        depot = nodes.get(index-1);
                        break;
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("error");
            e.printStackTrace();
        }
    }

    public Reader(String file) {
        read(file);
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public List<Vehicle> getFleet() {
        return fleet;
    }

    public void setFleet(List<Vehicle> fleet) {
        this.fleet = fleet;
    }

    public Node getDepot() {
        return depot;
    }

    public void setDepot(Node depot) {
        this.depot = depot;
    }
}