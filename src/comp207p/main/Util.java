package comp207p.main;

import org.apache.bcel.generic.*;

/**
 * @author Timur Kuzhagaliyev
 */
public class Util {

    public static boolean debug = false;

    public static void debug(Object object) {
        if (Util.debug) System.out.println(object);
    }

    public static boolean isConstantInstruction(InstructionHandle handle) {
        return isConstantInstruction(handle.getInstruction());
    }

    public static boolean isConstantInstruction(Instruction instruction) {
        if (instruction instanceof LDC) return true;
        if (instruction instanceof LDC2_W) return true;
        if (instruction instanceof ConstantPushInstruction) return true;
        return false;
    }

    public static Number extractConstant(InstructionHandle handle, ConstantPoolGen constPoolGen) {
        return Util.extractConstant(handle.getInstruction(), constPoolGen);
    }

    public static Number extractConstant(Instruction instruction, ConstantPoolGen constPoolGen) {
        try {
            if (instruction instanceof LDC) {
                LDC ldc = (LDC) instruction;
                Object value = ldc.getValue(constPoolGen);
                if (value instanceof Number) {
                    return (Number) value;
                }
            }
            if (instruction instanceof LDC2_W) {
                LDC2_W ldc2_w = (LDC2_W) instruction;
                if (extractArithmeticType(ldc2_w.getType(constPoolGen)) != ArithmeticType.OTHER) {
                    return ldc2_w.getValue(constPoolGen);
                }
            }
        } catch (Exception e) {
            System.err.println("Could not extract constant!");
            System.err.println(e.getClass() + e.getMessage());
            System.err.println();
            return null;
        }
        if (instruction instanceof ConstantPushInstruction) {
            ConstantPushInstruction push = (ConstantPushInstruction) instruction;
            return push.getValue();
        }
        return null;
    }

    public static Instruction getConstantPushInstruction(Number val, ConstantPoolGen constPoolGen) {
        if (val instanceof Double) {
            return new LDC2_W(constPoolGen.addDouble(val.doubleValue()));
        } else if (val instanceof Long) {
            return new LDC2_W(constPoolGen.addLong(val.longValue()));
        } else if (val instanceof Float) {
            return new LDC(constPoolGen.addFloat(val.floatValue()));
        } else if (val instanceof Integer) {
            return new LDC(constPoolGen.addInteger(val.intValue()));
        }
        return null;
    }

    public static ArithmeticType extractArithmeticType(Type type) {
        if (type == Type.INT) return ArithmeticType.INT;
        if (type == Type.LONG) return ArithmeticType.LONG;
        if (type == Type.FLOAT) return ArithmeticType.FLOAT;
        if (type == Type.DOUBLE) return ArithmeticType.DOUBLE;
        return ArithmeticType.OTHER;
    }

    public enum ArithmeticInstructionHack {
        IADD,
        ISUB,
        IMUL,
        IDIV,
        LADD,
        LSUB,
        LMUL,
        LDIV,
        FADD,
        FSUB,
        FMUL,
        FDIV,
        DADD,
        DSUB,
        DMUL,
        DDIV,
    }

    public static ArithmeticOperationType extractArithmeticOperationType(ArithmeticInstruction instruction) {
        String className = instruction.getClass().getSimpleName();
        ArithmeticInstructionHack type;
        try {
            type = ArithmeticInstructionHack.valueOf(className);
        } catch (Exception e) {
            return ArithmeticOperationType.OTHER;
        }
        switch (type) {
            case IADD:
            case LADD:
            case FADD:
            case DADD:
                return ArithmeticOperationType.ADD;
            case ISUB:
            case LSUB:
            case FSUB:
            case DSUB:
                return ArithmeticOperationType.SUB;
            case IMUL:
            case LMUL:
            case FMUL:
            case DMUL:
                return ArithmeticOperationType.MUL;
            case IDIV:
            case LDIV:
            case DDIV:
            case FDIV:
                return ArithmeticOperationType.DIV;
            default:
                return ArithmeticOperationType.OTHER;
        }
    }

    public static boolean isArithmeticLoadInstruction(Instruction i) {
        return i instanceof LoadInstruction && !(i instanceof ALOAD); //ALOAD = object reference
    }

}
