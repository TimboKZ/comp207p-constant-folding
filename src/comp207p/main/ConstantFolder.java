package comp207p.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;
import org.apache.bcel.util.InstructionFinder;

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
        ClassGen cgen = new ClassGen(original);
        ConstantPoolGen cpgen = cgen.getConstantPool();

        Method[] methods = cgen.getMethods();
        Method[] newMethods = new Method[methods.length];
        int i = 0;
        for (Method m : methods) {
            newMethods[i] = this.optimiseMethod(cgen, cpgen, m);
            i++;
        }

        // Implement your optimization here
        //
        // TODO: Simple constant folding
        //      TODO: Check if statement only consists of simple constants
        //
        // TODO: Constant propagation
        //
        // TODO:
        gen.setMethods(newMethods);
        gen.setConstantPool(cpgen);
        this.optimized = gen.getJavaClass();
    }

    public Method optimiseMethod(ClassGen classGen, ConstantPoolGen constPGen, Method method) {
        MethodGen methodGen = new MethodGen(method, classGen.getClassName(), constPGen);
        InstructionList instructionList = methodGen.getInstructionList();

        InstructionHandle[] handles = instructionList.getInstructionHandles();
        if (method.getName().equalsIgnoreCase("simple")) {
            for (int i = 0; i < handles.length; i++) {
                InstructionHandle handle = handles[i];
                if (handle.getInstruction() instanceof IADD) {
                    int first = (int) ((LDC) handles[i - 2].getInstruction()).getValue(constPGen);
                    int second = (int) ((LDC) handles[i - 1].getInstruction()).getValue(constPGen);
                    int result = first + second;

                    System.out.println(first + " + " + second + " = " + result);
                    instructionList.insert(handle, new LDC(constPGen.addInteger(result)));
                    try {
                        instructionList.delete(handles[i]);
                        instructionList.delete(handles[i - 1]);
                        instructionList.delete(handles[i - 2]);
                    } catch (TargetLostException e) {
                        e.printStackTrace();
                    }
                }
            }
            System.out.println(instructionList);
        }

        return methodGen.getMethod();
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
