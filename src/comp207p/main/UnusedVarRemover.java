package comp207p.main;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;
import org.apache.bcel.util.InstructionFinder;

import java.util.*;

/**
 * @author Christoph Ulshoefer <christophsulshoefer@gmail.com> 05/04/17.
 */
public class UnusedVarRemover extends Optimiser {

    public UnusedVarRemover(ClassGen classGen, ConstantPoolGen constPoolGen) {
        super(classGen, constPoolGen, DebugStage.Removal);
    }

    /**
     * Removes a store instruction if it is preceded by a constant push if its index is not used by any other local variable
     * instructions (i.e. IINC or load). Relevant constant push is also removed.
     *
     * @return Optimised method or null if no optimisations could be done
     */
    protected Method optimiseMethod(Method method, MethodGen methodGen, InstructionList list) {
        Map<Integer, InstructionHandle> stored = new HashMap<>();
        boolean optimisationPerformed = false;
        for (InstructionHandle handle : list.getInstructionHandles()) {
            if (handle == null) continue;
            Instruction instruction = handle.getInstruction();
            if (instruction instanceof StoreInstruction) {
                StoreInstruction storeInstruction = (StoreInstruction) instruction;
                int storeIndex = storeInstruction.getIndex();
                InstructionHandle redundantStore = stored.get(storeIndex);
                if (redundantStore != null) {
                    optimisationPerformed = optimisationPerformed || removeStoreInstruction(list, redundantStore);
                }
                stored.put(storeIndex, handle);
            } else if (instruction instanceof LocalVariableInstruction) {
                LocalVariableInstruction localVariableInstruction = (LocalVariableInstruction) instruction;
                stored.remove(localVariableInstruction.getIndex());
            }
        }
        for (InstructionHandle unusedStore : stored.values()) {
            optimisationPerformed = optimisationPerformed || removeStoreInstruction(list, unusedStore);
        }
        return optimisationPerformed ? methodGen.getMethod() : null;
    }

    protected boolean removeStoreInstruction(InstructionList list, InstructionHandle handle) {
        InstructionHandle storedValueHandle = handle.getPrev();
        if (storedValueHandle == null || Util.isConstantInstruction(storedValueHandle)) {
            InstructionHandle nextHandle = handle.getNext();
            attemptDelete(list, storedValueHandle, nextHandle);
            attemptDelete(list, handle, nextHandle);
            return true;
        } else {
            return false;
        }
    }
}
