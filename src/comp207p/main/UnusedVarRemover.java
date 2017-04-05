package comp207p.main;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ConstantPushInstruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InstructionTargeter;
import org.apache.bcel.generic.LoadInstruction;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.StoreInstruction;
import org.apache.bcel.generic.TargetLostException;
import org.apache.bcel.util.InstructionFinder;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Christoph Ulshoefer <christophsulshoefer@gmail.com> 05/04/17.
 */
public class UnusedVarRemover extends Optimiser {

    public UnusedVarRemover(ClassGen classGen, ConstantPoolGen constPoolGen) {
        super(classGen, constPoolGen, DebugStage.Removal);
    }

    @Override
    protected Method optimiseMethod(Method method, MethodGen methodGen, InstructionList list) {
        Set<Integer> unusedVars;
        InstructionFinder i = new InstructionFinder(list);
        try {
            unusedVars = getIndicesOfConstantStores(i);
            //Util.debug(unusedVars);
            removeUsedConstants(unusedVars, i);
            Iterator<InstructionHandle[]> storeI = i.search("StoreInstruction");
            for (Integer unusedVarI : unusedVars) {
                storeI = i.search("StoreInstruction");
                removeUnusedVarAtIndex(list, storeI, unusedVarI);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //this.printLocalVars(methodGen);
        list.setPositions(true);
        return methodGen.getMethod();
    }

    private void removeUnusedVarAtIndex(InstructionList list, Iterator<InstructionHandle[]> storeI, Integer unusedVarI) {
        while (storeI.hasNext()) {
            InstructionHandle[] handles = storeI.next();
            InstructionHandle handle = handles[0];
            if (handle == null || handle.getInstruction() == null) {
                Util.debug("Handle without instruction: ");
                continue;
            }
            //Util.debug(handle + ", prev: " + handle.getPrev() + ", next: " + handle.getNext());
            if (((StoreInstruction) handle.getInstruction()).getIndex() == unusedVarI) {
                InstructionHandle next = handle.getNext();
                try {
                    list.delete(handle.getPrev(), handle);
                } catch (TargetLostException tl) {
                    for (InstructionHandle target : tl.getTargets()) {
                        for (InstructionTargeter t : target.getTargeters()) {
                            if (next == null) {
                                //do something?
                            }
                            t.updateTarget(target, next);
                        }
                    }
                }
            }
            // TODO: Really remove the unused vars ._.
        }
    }

    private void removeUsedConstants(Set<Integer> unusedVars, InstructionFinder i) {
        Iterator<InstructionHandle[]> loadI = i.search("LoadInstruction");
        for (Iterator<InstructionHandle[]> it = loadI; it.hasNext(); ) {
            InstructionHandle[] handles = it.next();
            unusedVars.remove(((LoadInstruction) (handles[0].getInstruction())).getIndex());
        }
    }

    private Set<Integer> getIndicesOfConstantStores(InstructionFinder i) {
        Iterator<InstructionHandle[]> storeI = i.search("StoreInstruction");
        Set<Integer> unusedVars = new HashSet<>();
        for (Iterator<InstructionHandle[]> it = storeI; it.hasNext(); ) {
            InstructionHandle[] handles = it.next();
            InstructionHandle prev = handles[0].getPrev();
            if (prev != null && prev.getInstruction() instanceof ConstantPushInstruction) {
                unusedVars.add(((StoreInstruction) (handles[0].getInstruction())).getIndex());
            }
        }
        return unusedVars;
    }

    private void printLocalVars(MethodGen m) {
        LocalVariableGen[] lv = m.getLocalVariables();
        Util.debug(lv.length + " local vars");
        for (int j = 0; j < lv.length; j++) {
            Util.debug(lv[j].getType() + ", " + m.getMaxLocals());
            Util.debug(lv[j]);
        }
    }
}
