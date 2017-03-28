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
public abstract class Optimiser {
    protected ClassGen classGen;
    protected ConstantPoolGen constPoolGen;

    public Optimiser(ClassGen classGen, ConstantPoolGen constPoolGen) {
        this.classGen = classGen;
        this.constPoolGen = constPoolGen;
    }

    public Method optimiseMethod(Method method) {
        System.out.println(method.getName());
        MethodGen methodGen = new MethodGen(method, this.classGen.getClassName(), this.constPoolGen);
        InstructionList list = methodGen.getInstructionList();
        if (list == null) return method;
        return this.optimiseMethod(method, methodGen, list);
    }

    protected abstract Method optimiseMethod(
            Method method,
            MethodGen methodGen,
            InstructionList list
    );
}
