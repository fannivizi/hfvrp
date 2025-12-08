package algorithms;

import model.Node;

public class Saving implements Comparable<Saving>{
    private Node a;
    private Node b;
    private double saving;

    public Saving(Node a, Node b, double saving) {
        this.a = a;
        this.b = b;
        this.saving = saving;
    }

    public Node getA() {
        return a;
    }

    public void setA(Node a) {
        this.a = a;
    }

    public Node getB() {
        return b;
    }

    public void setB(Node b) {
        this.b = b;
    }

    public double getSaving() {
        return saving;
    }

    public void setSaving(double saving) {
        this.saving = saving;
    }

    @Override
    public int compareTo(Saving o) {
        return Double.compare(this.saving, o.saving);
    }

    @Override
    public String toString() {
        return "Saving{" +
                "a=" + a +
                ", b=" + b +
                ", saving=" + saving +
                '}';
    }
}
