package comp207p.target;

/**
 * @author Christoph Ulshoefer <christophsulshoefer@gmail.com> 06/04/17.
 */
public class CheekyFolding {
    public int methodOne() {
        int a = 30;
        int b = 3;
        int c;

        c = b * 4;
        if (c > 10) {
            c = c - 10;
        }
        if (c < 0) {
            c = c - 10;
        }
        return c;
    }

    public int methodTwo() {
        return methodOne() * methodOne() - 3 + 5;
    }

    public int methodThree() {
        int c = 4;
        if(false) {
            c = 3;
        }
        return c; //should return 4
    }

    public int methodFour() {
        int c;
        if(true) {
            c = 4;
        } else {
            c = 3;
        }
        int d;
        if(false) {
            d = 3 * c;
        } else {
            d = 10 * c;
        }
        return c + d;
    }

    public int methodFive(int varOutsideOfThing) {
        int c = 3;
        int d;
        if(varOutsideOfThing > 3) {
            d = 13;
        } else {
            d = 15;
        }
        return 4 + c + d;
    }

    public int methodSix() {
        return 3 + methodFive(0);
    }

    public int methodSeven() {
        int c = 4;
        for(int i = 0; i + c < 0; i++) {
            c = 13;
        }
        return c;
    }
}
