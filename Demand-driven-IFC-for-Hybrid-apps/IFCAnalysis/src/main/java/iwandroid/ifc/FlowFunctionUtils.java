package iwandroid.ifc;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.CancelException;
import iwandroid.frontend.AndroidAnalysis;
import iwandroid.dbinterfaces.BridgedMethod;
import iwandroid.utils.SourceSinkManager;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class FlowFunctionUtils {

    private FlowFunctionUtils() {}

    protected static SSAInstruction getInstruction(BasicBlockInContext<IExplodedBasicBlock> block) {
        if (block.getDelegate() == null) {
            return null;
        }
        return block.getDelegate().getInstruction();
    }

    protected static IField resolveField(@NotNull IClassHierarchy cha, FieldReference f) {
        IField field = cha.resolveField(f);
        assert field != null;
        return field;
    }



    protected static boolean isSensitiveSource(SourceSinkManager manager, CallSiteReference functionCallSite) {
        String method = functionCallSite.getDeclaredTarget().getName().toString();
        return manager.isSourceMethod(method);
    }

    public static boolean isSensitiveSink(SourceSinkManager manager, CallSiteReference callsite) {
        String method = callsite.getDeclaredTarget().getName().toString();
        return manager.isSinkMethod(method);
    }

    protected static boolean isLibraryCall(CallSiteReference callSite) {
        String methodName = callSite.getDeclaredTarget().getName().toString();
        String className = callSite.getDeclaredTarget().getDeclaringClass().getName().toString();

        if (className.startsWith("Landroid/support/v4/")) {
            return true;
        }
        return new AndroidLibraryList().contains(className, methodName);
    }

//    protected static boolean isLibraryNode(CGNode node) {
//
//    }

    static public Optional<CGNode> findCGNodeForBridgeMethod(BridgedMethod method, AndroidAnalysis analysis) throws CancelException {
        TypeReference reference = TypeReference.find(ClassLoaderReference.Application, method.clazz());
        assert reference != null;
        IClass clazz = analysis.getCha().lookupClass(reference);
        assert clazz != null;
        IMethod entrypointmethod = clazz.getMethod(Selector.make(method.signature()));
        assert entrypointmethod != null;
        return analysis.callgraph().stream().filter(node -> node.getMethod().equals(entrypointmethod)).findFirst();
    }

    static public Optional<CGNode> findCGNodeForBridgeMethod(String sclazz, String method, AndroidAnalysis analysis) throws CancelException {
        IClass clazz = analysis.getCha().lookupClass(TypeReference.find(ClassLoaderReference.Application, sclazz));
        assert clazz != null : "Class is null";
        IMethod entrypointmethod = clazz.getMethod(Selector.make(method));
        assert entrypointmethod != null;
        return analysis.callgraph().stream().filter(node -> node.getMethod().equals(entrypointmethod)).findFirst();
    }
}
