package comp207p.main;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;

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

    public Method optimiseMethod(Method method, int iteration) {
        MethodGen methodGen = new MethodGen(method, this.classGen.getClassName(), this.constPoolGen);
        InstructionList list = methodGen.getInstructionList();
        String className = this.classGen.getClassName();
        String shortClass = className.substring(className.lastIndexOf('.') + 1).trim();
        String methodName = method.getName();

        if ((ConstantFolder.ignoreClasses.contains(className)
                || ConstantFolder.ignoreClasses.contains(shortClass))
                && ConstantFolder.ignoreMethods.contains(methodName)) {
            return method;
        }

        debugString = shortClass + " --> " + methodName + "() " + this.stage;
        String iterationString = "(it " + iteration + ")";
        Util.debug = ConstantFolder.debugStages.contains(stage)
                && (ConstantFolder.debugClasses.contains(className)
                || ConstantFolder.debugClasses.contains(shortClass))
                && ConstantFolder.debugMethods.contains(methodName);
        if (list == null) return method;
        Util.debug("////////////");
        Util.debug("STR " + debugString + " " + iterationString + "\n");
        Util.debug("BEFORE:");
        Util.debug(list);
        method = this.optimiseMethod(method, methodGen, list);
        Util.debug("AFTER:");
        Util.debug(list);
        Util.debug("END " + debugString + " " + iterationString);
        Util.debug("\\\\\\\\\\\\\\\\\\\\\\\\\n");
        Util.debug = false;
        return method;
    }

    protected void attemptDelete(InstructionList list, InstructionHandle handle) {
        attemptDelete(list, handle, null);
    }

    protected void attemptDelete(InstructionList list, InstructionHandle handle, InstructionHandle replacement) {
        if (handle == null) return;
        try {
            list.delete(handle);
        } catch (TargetLostException e) {
            if(replacement != null) {
                for (InstructionHandle target : e.getTargets()) {
                    for (InstructionTargeter targeter : target.getTargeters()) {
                        targeter.updateTarget(target, replacement);
                    }
                }
            } else {
                System.err.println("Error: (" + debugString + ")");
                System.err.println(e.getClass() + e.getMessage());
                System.err.println();
            }
        }
        list.setPositions(true);
    }

    protected abstract Method optimiseMethod(
            Method method,
            MethodGen methodGen,
            InstructionList list
    );
}