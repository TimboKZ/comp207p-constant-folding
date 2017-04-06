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

    protected Method optimiseMethod(Method method, MethodGen methodGen, InstructionList list) {
        Map<Integer, InstructionHandle> stored = new HashMap<>();
        for (InstructionHandle handle : list.getInstructionHandles()) {
            if(handle == null) continue;
            Instruction instruction = handle.getInstruction();
            if (instruction instanceof StoreInstruction) {
                StoreInstruction storeInstruction = (StoreInstruction) instruction;
                int storeIndex = storeInstruction.getIndex();
                InstructionHandle redundantStore = stored.get(storeIndex);
                if (redundantStore != null) {
                    removeStoreInstruction(list, redundantStore);
                }
                stored.put(storeIndex, handle);
            } else if(instruction instanceof LocalVariableInstruction) {
                LocalVariableInstruction localVariableInstruction = (LocalVariableInstruction) instruction;
                stored.remove(localVariableInstruction.getIndex());
            }
        }
        for(InstructionHandle unusedStore : stored.values()) {
            removeStoreInstruction(list, unusedStore);
        }
        list.setPositions(true);
        return methodGen.getMethod();
    }

    protected void removeStoreInstruction(InstructionList list, InstructionHandle handle) {
        InstructionHandle storedValueHandle = handle.getPrev();
        if (storedValueHandle == null || Util.isConstantInstruction(storedValueHandle)) {
            InstructionHandle nextHandle = handle.getNext();
            attemptDelete(list, storedValueHandle, nextHandle);
            attemptDelete(list, handle, nextHandle);
        }
    }
}
