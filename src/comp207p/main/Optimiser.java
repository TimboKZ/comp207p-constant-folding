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
    private DebugStage stage;

    protected String debugString = null;

    public Optimiser(ClassGen classGen, ConstantPoolGen constPoolGen, DebugStage stage) {
        this.classGen = classGen;
        this.constPoolGen = constPoolGen;
        this.stage = stage;
    }

    public Method optimiseMethod(Method method) {
        return this.optimiseMethod(method, -1);
    }

    public Method optimiseMethod(Method method, int iteration) {
        MethodGen methodGen = new MethodGen(method, this.classGen.getClassName(), this.constPoolGen);
        InstructionList list = methodGen.getInstructionList();
        String className = this.classGen.getClassName();
        String methodName = method.getName();
        debugString = className + " --> " + methodName + "()";
        String iterationString = "(it.: " + iteration + ")\n";
        Util.debug = ConstantFolder.debugStages.contains(stage)
                && ConstantFolder.debugClasses.contains(className)
                && ConstantFolder.debugMethods.contains(methodName);
        if (list == null) return method;
        Util.debug(">>> " + debugString + " " + this.stage + " START " + iterationString);
        Util.debug("BEFORE:");
        Util.debug(list);
        method = this.optimiseMethod(method, methodGen, list);
        Util.debug("AFTER:");
        Util.debug(list);
        Util.debug("<<< " + debugString + " " + this.stage + " END " + iterationString);
        Util.debug = false;
        return method;
    }

    protected abstract Method optimiseMethod(
            Method method,
            MethodGen methodGen,
            InstructionList list
    );
}
