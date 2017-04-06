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
        if(varOutsideOfThing > -1) {
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

    public boolean methodEight(int a) {
        return 3 + 4 > 7 + 8 + a;
    }

    public int methodNine() {
        boolean some = 3 + 3 > 3 + 3;
        String a = "Random crap";
        if(some) {
            int s = 3 + 2;
            System.out.println(s);
        } else {
            System.out.println("Random print statement FALSE");
            System.out.println("Random print statement FALSE2");
        }
        return 3 + 4;
    }

    public long methodTen() {
        return 3 % 2 + 53 % 7 + 345555555512315L % 1231231231L;
    }

    public int methodEleven() {
        return 4 << (13 | 14);
    }

    public int methodTwelve() {
        int superComplicatedThing = 13 + 17 + 21 * 3;
        switch(superComplicatedThing) {
            case 93:
                superComplicatedThing = superComplicatedThing * 2; //correct
                break;
            case 90:
                return -1;
            default:
                return -1;
        }
        return superComplicatedThing; //186
    }
}
