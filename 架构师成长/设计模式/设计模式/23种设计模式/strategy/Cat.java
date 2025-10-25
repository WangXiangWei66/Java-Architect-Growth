package strategy;

public class Cat implements Comparable<Cat> {

    int weight, height;

    public Cat(int height, int weight) {
        this.height = height;
        this.weight = weight;
    }

    public int compareTo(Cat c) {
        if (this.weight < c.weight) return -1;
        else if (this.weight > c.weight) return 1;
        return 0;
    }

    @Override
    public String toString() {
        return "Cat{" +
                "weight=" + weight +
                ", height=" + height +
                '}';
    }
}
