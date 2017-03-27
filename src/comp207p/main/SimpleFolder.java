package comp207p.main;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;

/**
 * @author Timur Kuzhagaliyev (tim.kuzh@gmail.com)
 */
public class SimpleFolder {

    private ClassGen classGen;
    private ConstantPoolGen constantPoolGen;

    public SimpleFolder(ClassGen classGen, ConstantPoolGen constantPoolGen) {
        this.classGen = classGen;
        this.constantPoolGen = constantPoolGen;
    }

    public Method optimiseMethod(Method method) {
        MethodGen methodGen = new MethodGen(method, this.classGen.getClassName(), this.constantPoolGen);
        InstructionList instructionList = methodGen.getInstructionList();

        for (int index = 0; index < instructionList.getLength(); index++) {

        }

        return methodGen.getMethod();
    }

    /**
     * @return Determines whether an optimisation has been performed
     */
    private static boolean handleArithemticInstruction(ArithmeticInstruction instruction, InstructionList instructionList) {

    }

}
