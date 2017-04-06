package comp207p.main;

/**
 * @author Christoph Ulshoefer <christophsulshoefer@gmail.com> 06/04/17.
 */
public class Comparer {
    private boolean performComparison(
            ComparisonType cmpType,
            ArithmeticType number1Type,
            ArithmeticType number2Type,
            Number number1, Number number2) {
        ArithmeticType bestType = getBestType(number1Type, number2Type);
        switch (cmpType) {
            case EQUAL:
                return number1.equals(number2);
            case EQUAL_ZERO:
                return number1.equals(0);
            case NOT_EQUAL_ZERO:
                return !number1.equals(0);
            case GREATER:
                return number1.doubleValue() > number2.doubleValue();
            case GREATER_EQUAL:
                return number1.doubleValue() >= number2.doubleValue();
            case LESS:
                return number1.doubleValue() < number2.doubleValue();
            case LESS_EQUAL:
                return number1.doubleValue() <= number2.doubleValue();
            default:
                throw new IllegalArgumentException();
        }
    }

    private ArithmeticType getBestType(ArithmeticType number1Type, ArithmeticType number2Type) {
        if(number1Type == number2Type) {
            return number1Type;
        } else {
            if(areFloatingType(number1Type, number2Type)) {
                if(number1Type == ArithmeticType.DOUBLE || number2Type == ArithmeticType.DOUBLE) {
                    return ArithmeticType.DOUBLE;
                } else {
                    return ArithmeticType.FLOAT;
                }
            } else {
                if(number1Type == ArithmeticType.LONG || number2Type == ArithmeticType.LONG) {
                    return ArithmeticType.LONG;
                } else {
                    return ArithmeticType.INT;
                }
            }
        }
    }

    private boolean areFloatingType(ArithmeticType number1Type, ArithmeticType number2Type) {
        return number1Type == ArithmeticType.DOUBLE || number2Type == ArithmeticType.DOUBLE
                || number1Type == ArithmeticType.FLOAT || number2Type == ArithmeticType.FLOAT;
    }

    private boolean performEqualComparison(ArithmeticType typeToUse,
                                           Number number1, Number number2) {
        switch(typeToUse) {
            case DOUBLE:
                return number1.doubleValue() == number2.doubleValue();
            case FLOAT:
                return number1.floatValue() == number2.floatValue();
            case INT:
                return number1.intValue() == number2.intValue();
            case LONG:
                return number1.longValue() == number2.longValue();
            default:
                throw new IllegalArgumentException();
        }
    }

    private boolean performNotEqualComparison(ArithmeticType typeToUse,
                                           Number number1, Number number2) {
        return !performEqualComparison(typeToUse, number1, number2);
    }

    private boolean performGreaterEqualComparison(ArithmeticType typeToUse,
                                           Number number1, Number number2) {
        switch(typeToUse) {
            case DOUBLE:
                return number1.doubleValue() >= number2.doubleValue();
            case FLOAT:
                return number1.floatValue() >= number2.floatValue();
            case INT:
                return number1.intValue() >= number2.intValue();
            case LONG:
                return number1.longValue() >= number2.longValue();
            default:
                throw new IllegalArgumentException();
        }
    }

    private boolean performLessEqualComparison(ArithmeticType typeToUse,
                                           Number number1, Number number2) {
        switch(typeToUse) {
            case DOUBLE:
                return number1.doubleValue() <= number2.doubleValue();
            case FLOAT:
                return number1.floatValue() <= number2.floatValue();
            case INT:
                return number1.intValue() <= number2.intValue();
            case LONG:
                return number1.longValue() <= number2.longValue();
            default:
                throw new IllegalArgumentException();
        }
    }
    /*
    private boolean performEqualComparison(ArithmeticType typeToUse,
                                           Number number1, Number number2) {
        switch(typeToUse) {
            case DOUBLE:
                return number1.doubleValue() == number2.doubleValue();
            case FLOAT:
                return number1.floatValue() == number2.floatValue();
            case INT:
                return number1.intValue() == number2.intValue();
            case LONG:
                return number1.longValue() == number2.longValue();
            default:
                throw new IllegalArgumentException();
        }
    }*/
}
