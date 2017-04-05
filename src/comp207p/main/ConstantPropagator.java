package comp207p.main;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ConstantPushInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LDC;
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

    Map<Integer, Number> varsByIndex = null; //simulates the 'register'

    public ConstantPropagator(ClassGen classGen, ConstantPoolGen constPoolGen) {
        super(classGen, constPoolGen);
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
        if (this.classGen.getClassName().contains("ConstantVariableFolding")
                && method.getName().equals("methodThree")) Util.debug = true;
        Util.debug(list);

        int[] positions = list.getInstructionPositions();
        for (int index = 0; index < positions.length; index++) {
            int pos = positions[index];
            InstructionHandle handle = list.findHandle(pos);
            if (handle == null) continue;
            Instruction current = handle.getInstruction();
            try {
                Instruction maybeConstPush = current;
                Instruction maybeStore = handle.getNext().getInstruction();
                updateConstantStore(maybeConstPush, maybeStore);
            } catch (Exception e) {
                //Not enough instructions
            }
            if (Util.isArithmeticLoadInstruction(current)) {
                int loadI = ((LoadInstruction) current).getIndex();
                if (this.varsByIndex.containsKey(loadI)) {
                    Number n = this.varsByIndex.get(loadI);
                    this.insertNumberConstant(list, handle, n);
                    Util.deleteInstruction(list, handle, handle.getPrev());
                }
            }
            //if (list.findHandle(pos).getInstruction() instanceof)
        }
        Util.debug("========");
        Util.debug(list);
        Util.debug = false;
        list.setPositions();
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

    private void insertNumberConstant(InstructionList list, InstructionHandle handle, Number val) {
        if (val instanceof Double) { //Please, dear Java gods, forgive me
            list.insert(handle, new LDC(this.constPoolGen.addDouble((Double) val)));
        } else if (val instanceof Long) {
            list.insert(handle, new LDC(this.constPoolGen.addLong((Long) val)));
        } else if (val instanceof Float) {
            list.insert(handle, new LDC(this.constPoolGen.addFloat((Float) val)));
        } else if (val instanceof Integer) {
            list.insert(handle, new LDC(this.constPoolGen.addInteger((Integer) val)));
        }
    }
}
