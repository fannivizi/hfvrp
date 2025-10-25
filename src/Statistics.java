import java.util.List;

public class Statistics {
    private List<Path> paths;
    private int vehicle_num;
    private int node_num;
    private int distance;
    private double efficiency;
    private int cost;

    public Statistics(List<Path> paths) {
        this.paths = paths;
        this.vehicle_num = 0;
        this.node_num = 0;
        this.distance = 0;
        this.efficiency = 0;
        this.cost = 0;

        for(Path path: paths) {
            if(path.distance() != 0) {
                vehicle_num += 1;
                node_num += path.getNodes().size();
                distance += path.distance();
                efficiency += path.efficiency();
                cost += path.totalCost();
            }
        }
        node_num = node_num - vehicle_num*2 + 1;
        efficiency /= vehicle_num;
    }

    @Override
    public String toString() {
        return "Statistics: \n" +
                "Vehicles used: " + vehicle_num +
                "\nNodes visited: " + node_num +
                "\nDistance traveled: " + distance +
                "\nEfficiency: " + efficiency +
                "\nTotal cost: " + cost;
    }
}
