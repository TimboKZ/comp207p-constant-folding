package comp207p.main;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Timur Kuzhagaliyev
 * @since 2017-03-28 When the value of one variable can be determined at compile-time, we simply
 * replace the variable with that value in later instructions, until the variable receives a new
 * value. We repeat that process for each assignment operation.
 */
public class ConstantPropagator extends Optimiser {

    private Map<Integer, Number> varsByIndex = null; //simulates the 'register'

    public ConstantPropagator(ClassGen classGen, ConstantPoolGen constPoolGen) {
        super(classGen, constPoolGen, DebugStage.Propagation);
    }

    /**
     * If load instruction is not referenced by goto commands, replaces it with a constant push.
     * Otherwise appends constant push after load and re-targets all non-goto commands to the new constant push.
     *
     * @return Optimised method or null if no optimisations could be done
     */
    protected Method optimiseMethod(
            Method method,
            MethodGen methodGen,
            InstructionList list
    ) {
        boolean optimisationPerformed = false;
        this.varsByIndex = new HashMap<>();
        for (InstructionHandle handle : list.getInstructionHandles()) {
            Instruction instruction = handle.getInstruction();
            InstructionHandle nextHandle = handle.getNext();
            if (nextHandle != null) {
                Instruction nextInstruction = nextHandle.getInstruction();
                if (nextInstruction instanceof StoreInstruction) {
                    updateConstantStore(instruction, (StoreInstruction) nextInstruction);
                }
            }
            if (Util.isArithmeticLoadInstruction(instruction)) {
                optimisationPerformed = optimisationPerformed || attemptPropagation(list, handle);
            }
        }
        list.setPositions(true);
        return optimisationPerformed ? methodGen.getMethod() : null;
    }

    /**
     * If load instruction is a target for goto, do nothing.
     * If load instruction is in a loop and neighbour instructions alter the variable referenced by load, do nothing.
     * If none of the above are true, replace load with constant push.
     *
     * @return Determines whether an optimisation has been performed
     */
    private boolean attemptPropagation(InstructionList list, InstructionHandle handle) {
        LoadInstruction load = (LoadInstruction) handle.getInstruction();
        int variableIndex = load.getIndex();
        if (isGotoTarget(handle) || !this.varsByIndex.containsKey(variableIndex)) return false;
        if (isInLoopAndChanges(list, handle, variableIndex)) return false;

        Number value = this.varsByIndex.get(variableIndex);
        Instruction insert = Util.getConstantPushInstruction(value, constPoolGen);
        InstructionHandle newHandle = list.append(handle, insert);
        attemptDelete(list, handle, newHandle);
        return true;
    }

    private boolean isGotoTarget(InstructionHandle handle) {
        for (InstructionTargeter targeter : handle.getTargeters()) {
            if (targeter instanceof GotoInstruction) {
                return true;
            }
        }
        return false;
    }

    private boolean isInLoopAndChanges(InstructionList list, InstructionHandle handle, int variableIndex) {
        int position = handle.getPosition();
        InstructionHandle nextHandle = handle.getNext();
        boolean inLoop = false;
        int loopStart = -1;
        int loopEnd = -1;
        while (nextHandle != null) {
            Instruction nextInstruction = nextHandle.getInstruction();
            if (nextInstruction instanceof GotoInstruction) {
                GotoInstruction gotoInstruction = (GotoInstruction) nextInstruction;
                int targetPosition = gotoInstruction.getTarget().getPosition();
                if (targetPosition < position) {
                    inLoop = true;
                    loopStart = targetPosition;
                    loopEnd = nextHandle.getPosition();
                }
            }
            nextHandle = nextHandle.getNext();
        }

        if (!inLoop) return false;

        for (int i = loopStart; i < loopEnd; i++) {
            InstructionHandle loopHandle = list.findHandle(i);
            if (loopHandle == null) continue;
            Instruction instruction = loopHandle.getInstruction();
            if (instruction instanceof LocalVariableInstruction && !(instruction instanceof LoadInstruction)) {
                LocalVariableInstruction variableInstruction = (LocalVariableInstruction) instruction;
                if (variableInstruction.getIndex() == variableIndex) {
                    return true;
                }
            }
        }

        return false;
    }

    private void updateConstantStore(Instruction current, StoreInstruction next) {
        int index = next.getIndex();
        Number value = Util.extractConstant(current, this.constPoolGen);
        if (value != null) {
            this.varsByIndex.put(index, value);
        } else {
            this.varsByIndex.remove(index);
        }
    }
}
