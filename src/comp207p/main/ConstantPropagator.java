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
        for (InstructionHandle handle : list.getInstructionHandles()) {
            //InstructionHandle handle = list.findHandle(pos);
            if (handle == null) continue;
            Instruction current = handle.getInstruction();
            Util.debug(current);
            try {
                InstructionHandle newHandle = handle.getNext();
                if (newHandle == null) continue;
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
                    //attemptDelete(list, handle);
                    Util.deleteInstruction(list, handle, handle.getNext());
                }
            }
            //if (list.findHandle(pos).getInstruction() instanceof)
        }
        list.setPositions(true);
        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        return methodGen.getMethod();
    }

    private void updateConstantStore(Instruction current, Instruction next) {
        if (!(next instanceof StoreInstruction)) return;
        int index = ((StoreInstruction) next).getIndex();

        Number value = Util.extractConstant(current, this.constPoolGen);
        if (value != null) {
            this.varsByIndex.put(index, value);
        } else {
            this.varsByIndex.remove(index);
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
