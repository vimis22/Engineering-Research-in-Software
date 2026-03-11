package iwandroid.frontend;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.Language;
import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.dalvik.classLoader.DexIRFactory;
import com.ibm.wala.dalvik.util.AndroidAnalysisScope;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.CancelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class AndroidAnalysis {


    private final String apkfile;
    private final String androidJar;
    private final AnalysisCache cache;
    private IClassHierarchy cha = null;
    private AnalysisOptions options = null;
    private SSAPropagationCallGraphBuilder cgb = null;
    private CallGraph callGraph = null;
    private PointerAnalysis<InstanceKey> pa = null;

    private static final Logger logger = LoggerFactory.getLogger(AndroidAnalysis.class.getName());

    public AndroidAnalysis(String androidJar, String apkfile) throws ClassHierarchyException, IOException {
        logger.info("Setting up android analysis environment");
        this.androidJar = androidJar;
        logger.info("Using {}", this.androidJar);
        this.apkfile = apkfile;
        logger.info("APK: {}", this.apkfile);
        this.cache = new AnalysisCacheImpl(new DexIRFactory());
        AnalysisScope scope = AndroidAnalysisScope.setUpAndroidAnalysisScope(new File(this.apkfile).toURI(),
                CallGraphTestUtil.REGRESSION_EXCLUSIONS,
                CallGraphTestUtil.class.getClassLoader(),
                new File(this.androidJar).toURI());
        this.cha = ClassHierarchyFactory.make(scope);
        this.options = new AnalysisOptions(scope, new AllApplicationEntrypoints(scope, cha));
        this.cgb = Util.makeZeroCFABuilder(Language.JAVA, this.options, this.cache, this.cha);
    }

    public CallGraph callgraph() throws CancelException {
        if (this.callGraph == null) {
            this.callGraph = callgraphbuilder().makeCallGraph(this.options);
        }
        return this.callGraph;
    }

    public IMethod lookUpMethod(String className, String methodName) {
        IClass clazz = cha.lookupClass(TypeReference.find(ClassLoaderReference.Application, className));
        assert clazz != null : "Cannot find class " + className + " in class hierarchy";
        IMethod method = clazz.getMethod(Selector.make(methodName));
        if (method == null) {
            throw new IllegalArgumentException("Cannot found method " + methodName + " in class " + className);
        }
        return method;
    }

    public Optional<CGNode> nodeForMethod(IMethod method) throws CancelException {
        return callgraph().stream().filter(node -> node.getMethod().equals(method)).findFirst();
    }

    private SSAPropagationCallGraphBuilder callgraphbuilder() {
        return this.cgb;
    }

    public PointerAnalysis<InstanceKey> pointeranalysis() {
        if (this.pa == null) {
            this.pa = callgraphbuilder().getPointerAnalysis();
        }
        return this.pa;
    }

    public AnalysisCache getCache() {
        return cache;
    }

    public IClassHierarchy getCha() {
        return cha;
    }

    public AnalysisOptions getOptions() {
        return options;
    }

    @Override
    public String toString() {
        return "AndroidAnalysis{" +
                "apkfile='" + apkfile + '\'' +
                ", androidJar='" + androidJar + '\'' +
                ", options=" + options +
                '}';
    }
}
