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
    SimpleFolder folder = null;
    ConstantPropagator propagator = null;
    UnusedVarRemover remover = null;

    JavaClass original = null;
    JavaClass optimized = null;

    public static List<String> ignoreClasses = new ArrayList<>();

    public static List<DebugStage> debugStages = new ArrayList<>();
    public static List<String> debugClasses = new ArrayList<>();
    public static List<String> debugMethods = new ArrayList<>();


    public ConstantFolder(String classFilePath) {
        try {
            this.parser = new ClassParser(classFilePath);
            this.original = parser.parse();
            this.classGen = new ClassGen(original);
            this.constPoolGen = classGen.getConstantPool();
            this.folder = new SimpleFolder(classGen, constPoolGen);
            this.propagator = new ConstantPropagator(classGen, constPoolGen);
            this.remover = new UnusedVarRemover(classGen, constPoolGen);

            // Choose which classes to ignore
            ignoreClasses.add("DynamicVariableFolding");

            // Choose which stages/classes/methods you want to debug:
            debugStages.add(DebugStage.Folding);
            debugClasses.add("ConstantVariableFolding");
            debugMethods.add("methodTwo");
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

    public void optimizeMethods(Method[] methods) {
        for (Method method : methods) {
            Method optimisedMethod = method;
            Code code = optimisedMethod.getCode();
            if (code == null) continue;
            int maxInstructionCount = new InstructionList(code.getCode()).getLength();
            for (int iteration = 0; iteration < maxInstructionCount; iteration++) {
                optimisedMethod = folder.optimiseMethod(optimisedMethod, iteration);
                optimisedMethod = propagator.optimiseMethod(optimisedMethod, iteration);
                optimisedMethod = remover.optimiseMethod(optimisedMethod, iteration);
            }
            classGen.replaceMethod(method, optimisedMethod);
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
