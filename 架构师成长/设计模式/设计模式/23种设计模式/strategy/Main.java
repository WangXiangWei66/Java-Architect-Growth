package strategy;

import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        Cat[] a = {new Cat(3, 3), new Cat(5, 5), new Cat(1, 1)};

        Sorter<Cat> sorter = new Sorter<>();
        sorter.sort(a, (o1, o2) -> {
            if (o1.weight < o2.weight) return -1;
            else if (o1.weight > o2.weight) return 1;
            else return 0;
        });

        int[] c = {9, 2, 3, 5, 7, 1, 4};
        Dog[] d = {new Dog(3), new Dog(5), new Dog(1)};
        System.out.println(Arrays.toString(a));
    }
}
