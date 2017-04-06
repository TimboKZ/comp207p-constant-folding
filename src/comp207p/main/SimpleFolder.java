package comp207p.main;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;

/**
 * @author Timur Kuzhagaliyev
 */
public class SimpleFolder extends Optimiser {

    public SimpleFolder(ClassGen classGen, ConstantPoolGen constPoolGen) {
        super(classGen, constPoolGen, DebugStage.Folding);
    }

    /**
     * Replaces arithmetic instructions with constants and converts constants (e.g. casting) where possible
     *
     * @return Optimised method or null if no optimisations could be done
     */
    protected Method optimiseMethod(
            Method method,
            MethodGen methodGen,
            InstructionList list
    ) {
        boolean optimisationPerformed = false;
        for (InstructionHandle handle : list.getInstructionHandles()) {
            if (handle == null) continue;
            Instruction instruction = handle.getInstruction();
            if (instruction instanceof ConversionInstruction) {
                optimisationPerformed = optimisationPerformed || this.handleConversion(list, handle);
            } else if (instruction instanceof ArithmeticInstruction) {
                optimisationPerformed = optimisationPerformed || this.handleArithmeticInstruction(list, handle);
            }
        }
        return optimisationPerformed ? methodGen.getMethod() : null;
    }

    /**
     * @return Determines whether an optimisation has been performed
     */
    private boolean handleConversion(InstructionList list, InstructionHandle handle) {
        ConversionInstruction instruction = (ConversionInstruction) handle.getInstruction();
        ArithmeticType type = Util.extractArithmeticType(instruction.getType(this.constPoolGen));
        if (type == ArithmeticType.OTHER) return false;

        InstructionHandle previousHandle = handle.getPrev();
        Number value = Util.extractConstant(previousHandle, constPoolGen);
        if (value == null) return false;

        Instruction pushInstruction;
        switch(type) {
            case INT:
                pushInstruction = Util.getConstantPushInstruction(value.intValue(), constPoolGen);
                break;
            case LONG:
                pushInstruction = Util.getConstantPushInstruction(value.longValue(), constPoolGen);
                break;
            case FLOAT:
                pushInstruction = Util.getConstantPushInstruction(value.floatValue(), constPoolGen);
                break;
            case DOUBLE:
                pushInstruction = Util.getConstantPushInstruction(value.doubleValue(), constPoolGen);
                break;
            default:
                return false;
        }

        InstructionHandle replacementHandle = list.insert(handle, pushInstruction);

        attemptDelete(list, handle, replacementHandle);
        attemptDelete(list, previousHandle, replacementHandle);
        return true;
    }

    /**
     * @return Determines whether an optimisation has been performed
     */
    private boolean handleArithmeticInstruction(InstructionList list, InstructionHandle handle) {
        ArithmeticInstruction instruction = (ArithmeticInstruction) handle.getInstruction();
        ArithmeticType type = Util.extractArithmeticType(instruction.getType(this.constPoolGen));
        if (type == ArithmeticType.OTHER) return false;

        InstructionHandle handle2 = handle.getPrev();
        InstructionHandle handle1 = handle2.getPrev();
        Number number1 = Util.extractConstant(handle1, constPoolGen);
        Number number2 = Util.extractConstant(handle2, constPoolGen);
        if (number1 == null || number2 == null) return false;

        ArithmeticOperationType operationType = Util.extractArithmeticOperationType(instruction);
        int constIndex = this.performArithmeticOperation(operationType, type, number1, number2);
        if (constIndex == -1) return false;

        Instruction replacementInstruction;

        if(type == ArithmeticType.DOUBLE || type == ArithmeticType.LONG) {
            replacementInstruction = new LDC2_W(constIndex);
        } else {
            replacementInstruction = new LDC(constIndex);
        }

        InstructionHandle replacementHandle = list.insert(handle, replacementInstruction);

        attemptDelete(list, handle, replacementHandle);
        attemptDelete(list, handle1, replacementHandle);
        attemptDelete(list, handle2, replacementHandle);

        return true;
    }

    /**
     * @return Index of the newly inserted constant
     */
    private int performArithmeticOperation(
            ArithmeticOperationType operationType,
            ArithmeticType type,
            Number number1,
            Number number2
    ) {
        switch (operationType) {
            case ADD:
                return performAddition(type, number1, number2);
            case SUB:
                return performSubtraction(type, number1, number2);
            case MUL:
                return performMultiplication(type, number1, number2);
            case DIV:
                return performDivision(type, number1, number2);
        }
        return -1;
    }

    /**
     * @return Index of the newly inserted constant
     */
    private int performAddition(ArithmeticType type, Number number1, Number number2) {
        int index = -1;
        switch (type) {
            case INT:
                index = this.constPoolGen.addInteger((int) number1 + (int) number2);
                break;
            case LONG:
                index = this.constPoolGen.addLong((long) number1 + (long) number2);
                break;
            case FLOAT:
                index = this.constPoolGen.addFloat((float) number1 + (float) number2);
                break;
            case DOUBLE:
                index = this.constPoolGen.addDouble((double) number1 + (double) number2);
                break;
        }
        return index;
    }

    /**
     * @return Index of the newly inserted constant
     */
    private int performSubtraction(ArithmeticType type, Number number1, Number number2) {
        int index = -1;
        switch (type) {
            case INT:
                index = this.constPoolGen.addInteger((int) number1 - (int) number2);
                break;
            case LONG:
                index = this.constPoolGen.addLong((long) number1 - (long) number2);
                break;
            case FLOAT:
                index = this.constPoolGen.addFloat((float) number1 - (float) number2);
                break;
            case DOUBLE:
                index = this.constPoolGen.addDouble((double) number1 - (double) number2);
                break;
        }
        return index;
    }

    /**
     * @return Index of the newly inserted constant
     */
    private int performMultiplication(ArithmeticType type, Number number1, Number number2) {
        int index = -1;
        switch (type) {
            case INT:
                index = this.constPoolGen.addInteger((int) number1 * (int) number2);
                break;
            case LONG:
                index = this.constPoolGen.addLong((long) number1 * (long) number2);
                break;
            case FLOAT:
                index = this.constPoolGen.addFloat((float) number1 * (float) number2);
                break;
            case DOUBLE:
                index = this.constPoolGen.addDouble((double) number1 * (double) number2);
                break;
        }
        return index;
    }

    /**
     * @return Index of the newly inserted constant
     */
    private int performDivision(ArithmeticType type, Number number1, Number number2) {
        int index = -1;
        switch (type) {
            case INT:
                index = this.constPoolGen.addInteger((int) number1 / (int) number2);
                break;
            case LONG:
                index = this.constPoolGen.addLong((long) number1 / (long) number2);
                break;
            case FLOAT:
                index = this.constPoolGen.addFloat((float) number1 / (float) number2);
                break;
            case DOUBLE:
                index = this.constPoolGen.addDouble((double) number1 / (double) number2);
                break;
        }
        return index;
    }

}