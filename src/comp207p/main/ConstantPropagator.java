package comp207p.main;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;

/**
 * @author Timur Kuzhagaliyev
 * @since 2017-03-28
 */
public class ConstantPropagator extends Optimiser {

    public ConstantPropagator(ClassGen classGen, ConstantPoolGen constPoolGen) {
        super(classGen, constPoolGen);
    }

    protected Method optimiseMethod(
            Method method,
            MethodGen methodGen,
            InstructionList list
    ) {
        if(this.classGen.getClassName().contains("ConstantVariableFolding")
                && method.getName().equals("methodOne")) Util.debug = true;
        Util.debug(list);
        Util.debug = false;
        return methodGen.getMethod();
    }

}
