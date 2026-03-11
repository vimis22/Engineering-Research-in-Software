package iwandroid.frontend;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.IRFactory;
import com.ibm.wala.ssa.SSAOptions;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;
import iwandroid.dbinterfaces.BridgedMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Constructs the IR from the given class
 */
public class BridgeMethodIR {
    private final IRFactory<IMethod> irFactory;
    private final String className;
    private final IClassHierarchy cha;
    private final SSAOptions options;
    private final String methodName;

    private static final Logger LOGGER = LoggerFactory.getLogger(BridgeMethodIR.class);

    public BridgeMethodIR(BridgedMethod method, IClassHierarchy cha, AnalysisCache cache) {
        this.irFactory = cache.getIRFactory();
        this.className = method.clazz();
        this.cha = cha;
        this.options = cache.getSSAOptions();
        this.methodName = method.signature();
    }

    /**
     * Constructs the IR for the given methdo
     * @return IR
     */
    public IR makeIR() {
        IClass clazz = cha.lookupClass(TypeReference.find(ClassLoaderReference.Application, className));
        if (clazz == null)
            throw new IllegalArgumentException("Cannot find class " + className + " in class hierarchy");
        IMethod method = clazz.getMethod(Selector.make(methodName));
        if (method == null) {
            throw new IllegalArgumentException("Cannot found method " + methodName + " in class " + className);
        }
        return irFactory.makeIR(method, Everywhere.EVERYWHERE, this.options);
    }

    public IClassHierarchy getCha() {
        return cha;
    }
}
