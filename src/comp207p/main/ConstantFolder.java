package comp207p.main;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConstantFolder {
    ClassParser parser = null;
    ClassGen classGen = null;
    ConstantPoolGen constPoolGen = null;
    private Optimiser[] optimisers = null;

    JavaClass original = null;
    JavaClass optimized = null;

    public static List<String> ignoreClasses = new ArrayList<>();
    public static List<String> ignoreMethods = new ArrayList<>();

    public static List<DebugStage> debugStages = new ArrayList<>();
    public static List<String> debugClasses = new ArrayList<>();
    public static List<String> debugMethods = new ArrayList<>();


    public ConstantFolder(String classFilePath) {
        try {
            this.parser = new ClassParser(classFilePath);
            this.original = parser.parse();
            this.classGen = new ClassGen(original);
            this.constPoolGen = classGen.getConstantPool();
            this.optimisers = new Optimiser[]{
                    new SimpleFolder(classGen, constPoolGen),
                    new ConstantPropagator(classGen, constPoolGen),
                    new UnusedVarRemover(classGen, constPoolGen)
            };

            // Choose which classes/methods to ignore (uses AND, *not* OR)
            ignoreClasses.add("DynamicVariableFolding");
            ignoreMethods.add("methodNameHere");

            // Choose which stages/classes/methods to print debug output for (uses AND, *not* OR)
            debugStages.add(DebugStage.Propagation);
            debugClasses.add("DynamicVariableFolding");
            debugMethods.add("methodFour");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void optimize() {
        optimizeMethods(classGen.getMethods());
        classGen.setMajor(50);
        classGen.setConstantPool(constPoolGen);
        //classGen.update();
        optimized = classGen.getJavaClass();
    }

    /**
     * Applies optimisers from `optimisers` array until no changes occur
     */
    public void optimizeMethods(Method[] methods) {
        for (Method originalMethod : methods) {
            Method method = originalMethod;
            Code code = method.getCode();
            if (code == null) continue;
            int iteration = 1;
            boolean optimisationPerformed = true;
            while (optimisationPerformed) {
                optimisationPerformed = false;
                for (Optimiser optimiser : this.optimisers) {
                    Method optimisedMethod = optimiser.optimiseMethod(method, iteration);
                    if (optimisedMethod != null) {
                        optimisationPerformed = true;
                        method = optimisedMethod;
                    }
                }
                iteration++;
            }
            classGen.replaceMethod(originalMethod, method);
        }
    }

    public void write(String optimisedFilePath) {
        this.optimize();

        try {
            FileOutputStream out = new FileOutputStream(new File(optimisedFilePath));
            this.optimized.dump(out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
