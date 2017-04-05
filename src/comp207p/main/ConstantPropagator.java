package comp207p.main;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ConstantPushInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.LDC2_W;
import org.apache.bcel.generic.LoadInstruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.StoreInstruction;

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
     * Replaces variables with constants
     * TODO delete variable store if unnecessary
     *
     * @return The optimised method
     */
    protected Method optimiseMethod(
            Method method,
            MethodGen methodGen,
            InstructionList list
    ) {
        this.varsByIndex = new HashMap<Integer, Number>();
        Util.debug(list);
        for (InstructionHandle handle: list.getInstructionHandles()) {
            //InstructionHandle handle = list.findHandle(pos);
            if (handle == null) continue;
            Instruction current = handle.getInstruction();
            try {
                InstructionHandle newHandle = handle.getNext();
                if(newHandle == null) continue;
                Instruction maybeStore = newHandle.getInstruction();
                updateConstantStore(current, maybeStore);
            } catch (Exception e) {
                e.printStackTrace();
                //Not enough instructions
            }
            if (Util.isArithmeticLoadInstruction(current)) {
                int loadI = ((LoadInstruction) current).getIndex();
                if (this.varsByIndex.containsKey(loadI)) {
                    Number n = this.varsByIndex.get(loadI);
                    Instruction insert = this.getNumberConstantInsertionInstruction(n);
                    list.append(handle, insert);
                    Util.deleteInstruction(list, handle, handle.getNext());
                }
            }
            //if (list.findHandle(pos).getInstruction() instanceof)
        }
        Util.debug("======== After propagating constants");
        Util.debug(list);
        list.setPositions(true);
        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        return methodGen.getMethod();
    }

    /**
     * Puts a constant in the class hashmap if we would store one at runtime. Deletes the constant
     * from the class hashmap if it is not immediately clear that we are storing a constant
     */
    private void updateConstantStore(Instruction maybeConstpush, Instruction maybeStore) {
        boolean isConstPush = maybeConstpush instanceof ConstantPushInstruction;
        boolean isStore = maybeStore instanceof StoreInstruction;
        if (isConstPush && isStore) {
            int varInd = ((StoreInstruction) maybeStore).getIndex();
            Number val = ((ConstantPushInstruction) maybeConstpush).getValue();
            this.varsByIndex.put(varInd, val);
        } else if (!isConstPush && isStore) {
            int varInd = ((StoreInstruction) maybeStore).getIndex();
            this.varsByIndex.remove(varInd);
        }
    }

    private Instruction getNumberConstantInsertionInstruction(Number val) {
        if (val instanceof Double) { //Please, dear Java gods, forgive me
            return new LDC2_W(this.constPoolGen.addDouble(val.doubleValue()));
        } else if (val instanceof Long) {
            return new LDC2_W(this.constPoolGen.addLong(val.longValue()));
        } else if (val instanceof Float) {
            return new LDC(this.constPoolGen.addFloat(val.floatValue()));
        } else if (val instanceof Integer) {
            return new LDC(this.constPoolGen.addInteger(val.intValue()));
        }
        return null;
    }
}
