package pack4;

import pack3.Addition;

public class TestAddition {

    public static void main(String[] args) {

        Addition obj = new Addition();
        int result = obj.add(10, 20);

        System.out.println("Sum = " + result);
    }

}
