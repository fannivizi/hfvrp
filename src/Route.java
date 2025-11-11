import java.util.ArrayList;
import java.util.List;

public class Route {
    private Vehicle vehicle;
    private List<Node> nodes;

    public Route(Vehicle vehicle) {
        this.vehicle = vehicle;
        nodes = new ArrayList<>();
    }

    public Route(Vehicle vehicle, List<Node> nodes) {
        this.vehicle = vehicle;
        this.nodes = nodes;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public void addNode(Node node) {
        this.nodes.add(node);
    }

    @Override
    public String toString() {
        String route = "";
        for(Node node: nodes) {
            route += node + " ";
        }
        return "Path{" +
                "vehicle=" + vehicle +
                ", nodes=" + route +
                "}\n";
    }

    public int distance() {
        int d = 0;
        for (int i = 0; i < nodes.size()-1; i++) {
            d += nodes.get(i).distance(nodes.get(i+1));
        }
        return d;
    }

    public int demand() {
        int demand = 0;
        for (Node n: nodes) {
            demand += n.getDemand();
        }
        return demand;
    }

    public double efficiency() {
         return demand() / (double)vehicle.getCapacity();
    }

    public int totalCost() {
        return vehicle.getFixed_cost() + vehicle.getUnit_distance_cost()*distance();
    }
}
