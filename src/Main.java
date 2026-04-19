import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import algorithms.*;
import model.*;
import utils.Reader;
import utils.Statistics;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        //best test files: test20, X148, X223, X275, X317, X429, X513, X701, X979
        String file = "X148";
        Reader reader = new Reader("data/"+file+"-HVRP.vrp");

        List<Vehicle> fleet = reader.getFleet();
        List<Node> nodes = reader.getNodes();
        Node depot = reader.getDepot();

        PrintWriter res_writer = new PrintWriter("test/"+file+"-res.csv");
        res_writer.println("algorithm,seed,result,time,nodes");

        //write capacity and demand on screen
        int f_cap = 0;
        int demand = 0;
        for(Vehicle v: fleet) {
            f_cap += v.getCapacity();
        }
        for(Node n: nodes) {
            demand += n.getDemand();
        }
        System.out.println("Cap: " + f_cap + ", demand: " + demand + "\n");

        long start, end;
        int[] seeds = {32847, 63577, 10531, 40330, 7254};

        //savings
        System.out.println("CW:");
        start = System.currentTimeMillis();
        CWSavings cw = new CWSavings(fleet, nodes, depot);
        cw.run();
        end = System.currentTimeMillis();
        List<Route> cw_res = cw.getRoutes();
        //System.out.println(cw_res);
        eval("cws", res_writer, end-start, cw_res);

        //genetic
        System.out.println("Genetic:");
        for(int seed: seeds) {
            start = System.currentTimeMillis();
            Genetic g = new Genetic(fleet, nodes, depot, 100, 500, seed, false);
            g.run();
            end = System.currentTimeMillis();
            eval("ga", res_writer, end-start, g.getBest(), seed);
        }

        //local search
        System.out.println("Savings + local search:");
        start = System.currentTimeMillis();
        LocalSearch ls = new LocalSearch(cw.getRoutes());
        end = System.currentTimeMillis();
        eval("s+ls", res_writer, end-start, ls.getBest());

        //genetic + savings
        System.out.println("Savings + genetic:");
        for(int seed: seeds) {
            start = System.currentTimeMillis();
            Genetic gs = new Genetic(fleet, nodes, depot, 100, 500, seed, true);
            gs.run();
            end = System.currentTimeMillis();
            eval("ga", res_writer, end-start, gs.getBest(), seed);
        }

        res_writer.close();
    }

    public static void eval(String name, PrintWriter res, long t, List<Route> sol) {
        Statistics stats = new Statistics(sol);
        res.println(name + ", ," + stats.getCost() + ',' + t + ',' + stats.getNode_num());
        System.out.println("Time: " + t);
        System.out.println(stats);
    }

    public static void eval(String name, PrintWriter res, long t, List<Route> sol, int seed) {
        Statistics stats = new Statistics(sol);
        res.println(name + ',' + seed + ',' + stats.getCost()+ ',' + t + ',' + stats.getNode_num());
        System.out.println("Time: " + t);
        System.out.println(stats);
    }
}
