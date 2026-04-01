package model;

public class Node {
    private int index;
    private Coordinate coord;
    private int demand;

    public Node(int index, int x, int y) {
        this.index = index;
        this.coord = new Coordinate(x, y);
        this.demand = 0;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Coordinate getCoord() {
        return coord;
    }

    public void setCoord(Coordinate coord) {
        this.coord = coord;
    }

    public int getDemand() {
        return demand;
    }

    public void setDemand(int demand) {
        this.demand = demand;
    }

    @Override
    public String toString() {
        return "n" + index + ": d=" + demand;
    }

    public Double distance(Node other) {
        return Math.sqrt(Math.pow(other.getCoord().getX() - this.coord.getX(), 2) + Math.pow(other.getCoord().getY() - this.coord.getY(), 2));
    }
}
