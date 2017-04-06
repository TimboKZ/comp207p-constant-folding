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
                boolean propagationNeeded = false;
                for (InstructionTargeter targeter : handle.getTargeters()) {
                    if (!(targeter instanceof GotoInstruction)) {
                        propagationNeeded = true;
                        break;
                    }
                }
                if (handle.getTargeters().length == 0 || propagationNeeded) {
                    LoadInstruction load = (LoadInstruction) instruction;
                    int index = load.getIndex();
                    if (this.varsByIndex.containsKey(index)) {
                        optimisationPerformed = true;
                        Number value = this.varsByIndex.get(index);
                        Instruction insert = Util.getConstantPushInstruction(value, constPoolGen);
                        InstructionHandle newHandle = list.append(handle, insert);
                        boolean canDelete = true;
                        for (InstructionTargeter targeter : handle.getTargeters()) {
                            if (targeter instanceof GotoInstruction) {
                                canDelete = false;
                            } else {
                                targeter.updateTarget(handle, newHandle);
                            }
                        }
                        if (canDelete) attemptDelete(list, handle, newHandle);
                    }
                }
            }
        }
        list.setPositions(true);
        return optimisationPerformed ? methodGen.getMethod() : null;
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
