import java.util.List;

public class Main {
    public static void main(String[] args) {
        Reader reader = new Reader("data/X115-HVRP.vrp");
        //Reader reader = new Reader("data/X148-HVRP.vrp");
        //Reader reader = new Reader("data/X172-HVRP.vrp");
        List<Vehicle> fleet = reader.getFleet();
        List<Node> nodes = reader.getNodes();
        Node depot = reader.getDepot();

        NearestNeighbor nn = new NearestNeighbor(fleet, nodes, depot);
        List<Path> nn_res = nn.run();
        System.out.println(new Statistics(nn_res));

        CWSavings cw = new CWSavings(fleet, nodes, depot);
        cw.run();
        List<Path> cw_res = cw.getPaths();
        System.out.println(cw_res);
        System.out.println(new Statistics(cw_res));
    }
}
