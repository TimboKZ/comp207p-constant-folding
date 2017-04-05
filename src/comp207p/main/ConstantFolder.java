package comp207p.main;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ConstantFolder {
    ClassParser parser = null;
    ClassGen gen = null;

    JavaClass original = null;
    JavaClass optimized = null;

    public ConstantFolder(String classFilePath) {
        try {
            this.parser = new ClassParser(classFilePath);
            this.original = this.parser.parse();
            this.gen = new ClassGen(this.original);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void optimize() {
        ClassGen classGen = new ClassGen(original);
        ConstantPoolGen constantPoolGen = classGen.getConstantPool();
        SimpleFolder folder = new SimpleFolder(classGen, constantPoolGen);
        ConstantPropagator propagator = new ConstantPropagator(classGen, constantPoolGen);

        Method[] methods = classGen.getMethods();
        int methodCount = methods.length;
        Method[] newMethods = new Method[methodCount];
        //System.out.println(gen.getJavaClass());
        for (int i = 0; i < methodCount; i++) {
            Method optimisedMethod = methods[i];
            //optimisedMethod = folder.optimiseMethod(optimisedMethod);
            optimisedMethod = propagator.optimiseMethod(optimisedMethod);
            gen.replaceMethod(methods[i], optimisedMethod);
        }
        gen.setConstantPool(constantPoolGen);
        //gen.update();
        //System.out.println(gen.getJavaClass());
        this.optimized = gen.getJavaClass();
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
