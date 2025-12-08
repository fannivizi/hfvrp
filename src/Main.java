import java.util.List;

public class Main {
    public static void main(String[] args) {
        Reader reader = new Reader("data/test20.vrp");
        //Reader reader = new Reader("data/X115-HVRP.vrp");
        //Reader reader = new Reader("data/X148-HVRP.vrp");
        //Reader reader = new Reader("data/X351-HVRP.vrp");
        List<Vehicle> fleet = reader.getFleet();
        List<Node> nodes = reader.getNodes();
        Node depot = reader.getDepot();

        int f_cap = 0;
        int demand = 0;
        for(Vehicle v: fleet) {
            f_cap += v.getCapacity();
        }
        for(Node n: nodes) {
            demand += n.getDemand();
        }

        System.out.println("Cap: " + f_cap + ", demand: " + demand);

        NearestNeighbor nn = new NearestNeighbor(fleet, nodes, depot);
        List<Route> nn_res = nn.run();
        System.out.println(new Statistics(nn_res));

        CWSavings cw = new CWSavings(fleet, nodes, depot);
        cw.run();
        List<Route> cw_res = cw.getRoutes();
        //System.out.println(cw_res);
        System.out.println(new Statistics(cw_res));
    }
}
