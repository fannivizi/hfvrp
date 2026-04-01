import java.io.FileNotFoundException;
import java.util.List;

import algorithms.*;
import model.*;
import utils.BestReader;
import utils.Reader;
import utils.Statistics;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        //best test files: test20, X115, X223, X275, X317, X429, X513, X701, X979
        String file = "X429";
        Reader reader = new Reader("data/"+file+"-HVRP.vrp");

        List<Vehicle> fleet = reader.getFleet();
        List<Node> nodes = reader.getNodes();
        Node depot = reader.getDepot();

        BestReader best = new BestReader(fleet, nodes, depot, "data/"+file+"-HVRP.sol");

        int f_cap = 0;
        int demand = 0;
        for(Vehicle v: fleet) {
            f_cap += v.getCapacity();
        }
        for(Node n: nodes) {
            demand += n.getDemand();
        }

        System.out.println("Cap: " + f_cap + ", demand: " + demand + "\n");

        System.out.println("Best:");
        System.out.println(new Statistics(best.getRoutes()));

        NearestNeighbor nn = new NearestNeighbor(fleet, nodes, depot);
        List<Route> nn_res = nn.run();
        //System.out.println(new Statistics(nn_res));

        System.out.println("CW:");
        CWSavings cw = new CWSavings(fleet, nodes, depot);
        cw.run();
        List<Route> cw_res = cw.getRoutes();
        //System.out.println(cw_res);
        System.out.println(new Statistics(cw_res));

        System.out.println("Local search:");
        LocalSearch ls = new LocalSearch(cw.getRoutes());
        System.out.println(new Statistics(ls.getBest()));

        System.out.println("Genetic:");
        Genetic g = new Genetic(fleet, nodes, depot, 200, 500, 32847);
        g.run();
    }
}
