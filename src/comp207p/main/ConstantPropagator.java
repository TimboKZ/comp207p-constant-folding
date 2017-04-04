package comp207p.main;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.StoreInstruction;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * @author Timur Kuzhagaliyev
 * @since 2017-03-28
 * When the value of one variable can be determined at compile-time,
 * we simply replace the variable with that value in later instructions,
 * until the variable receives a new value.
 * We repeat that process for each assignment operation.
 */
public class ConstantPropagator extends Optimiser {

    Stack<Number> runTimeStack = null; //simulates the runtime-stack
    Map<Integer, Number> varsByIndex = null; //simulates the 'register'

    public ConstantPropagator(ClassGen classGen, ConstantPoolGen constPoolGen) {
        super(classGen, constPoolGen);
    }

    /**
     * Replaces variables with constants
     * @param method
     * @param methodGen
     * @param list
     * @return The optimised method
     */
    protected Method optimiseMethod(
            Method method,
            MethodGen methodGen,
            InstructionList list
    ) {
        this.runTimeStack = new Stack<Number>();
        this.varsByIndex = new HashMap<Integer, Number>();
        if(this.classGen.getClassName().contains("ConstantVariableFolding")
                && method.getName().equals("methodOne")) Util.debug = true;
        //Util.debug(list);

        int[] positions = list.getInstructionPositions();
        for (int index = 0; index < positions.length; index++) {
            int pos = positions[index];
            InstructionHandle handle = list.findHandle(pos);
            if (handle == null) continue;
            Instruction instruction = list.findHandle(pos).getInstruction();
            if(instruction instanceof StoreInstruction) {

            }
        }
        Util.debug = false;
        return methodGen.getMethod();
    }

    private boolean isCompiletimeComputable() {
        return true; //TODO: Welp, we run into problems here, if we have side effects/method params
    }
}
