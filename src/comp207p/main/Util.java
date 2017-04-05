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

    public static Number extractConstant(InstructionHandle handle, ConstantPoolGen constPoolGen) {
        return Util.extractConstant(handle.getInstruction(), constPoolGen);
    }

    public static Number extractConstant(Instruction instruction, ConstantPoolGen constPoolGen) {
        if (instruction instanceof LDC) {
            LDC ldc = (LDC) instruction;
            Object value = ldc.getValue(constPoolGen);
            if (value instanceof Number) {
                return (Number) value;
            }
        }
        return null;
    }

    public static ArithmeticType extractArithemticType(Type type) {
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
        switch(type) {
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

    /**
     * Deletes the next instruction
     * Try/catch block done as per suggestion in:
     * https://commons.apache.org/proper/commons-bcel/apidocs/org/apache/bcel/generic/TargetLostException.html
     * @param list
     * @param toDelete
     */
    public static void deleteInstruction(InstructionList list, InstructionHandle toDelete, InstructionHandle newTarget) {
        if(toDelete == null) return;
        try {
            //System.out.println("deleting: " + toDelete + ", " + newTarget);
            list.delete(toDelete);
        } catch(TargetLostException e) {
            //e.printStackTrace();
            for (InstructionHandle target : e.getTargets()) {
                for (InstructionTargeter targeter : target.getTargeters()) {
                    System.out.println(toDelete + "targeter: " + targeter + ", " + target + ", " + newTarget);
                    targeter.updateTarget(target, newTarget);
                }
            }
        }
    }

}
