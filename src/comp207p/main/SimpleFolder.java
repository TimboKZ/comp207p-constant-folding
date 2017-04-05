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
     * Traverses the method, tries to evaluate as many instructions as possible
     *
     * @return The optimised method
     */
    protected Method optimiseMethod(
            Method method,
            MethodGen methodGen,
            InstructionList list
    ) {
        int[] positions = list.getInstructionPositions();
        for (int index = 0; index < positions.length; index++) {
            int pos = positions[index];
            InstructionHandle handle = list.findHandle(pos);
            /*
            TODO: (Relevant to handleArithmeticInstruction)
            Instead of deleting old handles immediately can try accumulating
            the list of references to delete once the loop is over. Not sure
            if this will work but in theory it should make the check below
            unnecessary.
             */
            if (handle == null) continue;
            Instruction instruction = list.findHandle(pos).getInstruction();
            if (instruction instanceof ArithmeticInstruction) {
                this.handleArithmeticInstruction(index, positions, list);
            }
        }
        list.setPositions(true);
        return methodGen.getMethod();
    }

    /**
     * @return Determines whether an optimisation has been performed
     */
    private boolean handleArithmeticInstruction(int index, int[] positions, InstructionList list) {
        InstructionHandle handle = list.findHandle(positions[index]);
        ArithmeticInstruction instruction = (ArithmeticInstruction) handle.getInstruction();
        ArithmeticType type = Util.extractArithemticType(instruction.getType(this.constPoolGen));
        if (type == ArithmeticType.OTHER) return false;

        InstructionHandle handle1 = list.findHandle(positions[index - 2]);
        InstructionHandle handle2 = list.findHandle(positions[index - 1]);
        Number number1 = Util.extractConstant(handle1, constPoolGen);
        Number number2 = Util.extractConstant(handle2, constPoolGen);
        if (number1 == null || number2 == null) return false;
        // TODO: Delete constants from the pool once they are not used anymore
        // --> Use another optimization for that
        // TODO: Checking pool size revealed they are not deleted immediately,
        // TODO: but maybe this is done during bytecode generation phase?

        ArithmeticOperationType operationType = Util.extractArithmeticOperationType(instruction);
        int constIndex = this.performArithmeticOperation(operationType, type, number1, number2);
        // Couldn't perform operation
        if(constIndex == -1) return false;

        list.insert(handle, new LDC(constIndex));

        attemptDelete(list, handle);
        attemptDelete(list, handle1);
        attemptDelete(list, handle2);

        return true;
    }

    private void attemptDelete(InstructionList list, InstructionHandle handle) {
        try {
            list.delete(handle);
        } catch (Exception e) {
            System.err.println("Error: (" + debugString + ")");
            System.err.println(e.getClass() + e.getMessage());
            System.err.println();
        }
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
