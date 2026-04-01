package utils;

import model.Node;
import model.Route;
import model.Vehicle;

import javax.xml.crypto.dsig.spec.XSLTTransformParameterSpec;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BestReader {
    private List<Route> routes;
    private List<Vehicle> fleet;
    private Node depot;
    private List<Node> nodes;

    public BestReader(List<Vehicle> fleet, List<Node> nodes, Node depot, String file) {
        this.routes = new ArrayList<>();
        this.fleet = new ArrayList<>(fleet);
        this.depot = depot;
        this.nodes = new ArrayList<>(nodes);
        read(file);
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public void read(String file) {
        File input = new File(file);

        try (Scanner reader = new Scanner(input)) {

            while(reader.hasNext()) {
                String read = reader.nextLine();
                String[] words = read.split(" ");
                int vehicle = Integer.parseInt(words[1].substring(1, words[1].length()-1));
                for(Vehicle v: fleet) {
                    if(vehicle == v.getIndex()) {
                        fleet.remove(v);
                        routes.add(new Route(v));
                        routes.getLast().addNode(depot);
                        break;
                    }
                }

                for (int i = 2; i < words.length; i++) {
                    int node = Integer.parseInt(words[i]);
                    for(Node n: nodes) {
                        if(node == n.getIndex()) {
                            nodes.remove(n);
                            routes.getLast().addNode(n);
                            break;
                        }
                    }
                }
                routes.getLast().addNode(depot);
            }
        } catch (FileNotFoundException e) {
            System.out.println("error");
            e.printStackTrace();
        }
    }
}
