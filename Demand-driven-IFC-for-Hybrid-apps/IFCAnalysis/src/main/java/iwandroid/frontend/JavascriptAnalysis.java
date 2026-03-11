package iwandroid.frontend;

import com.ibm.wala.cast.ir.ssa.AstIRFactory;
import com.ibm.wala.cast.js.ipa.callgraph.JSCallGraphUtil;
import com.ibm.wala.cast.js.translator.CAstRhinoTranslatorFactory;
import com.ibm.wala.cast.types.AstMethodReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.IRFactory;
import com.ibm.wala.ssa.SSAOptions;

import java.util.ArrayList;

public class JavascriptAnalysis {
    private final IRFactory<IMethod> factory;
    private IClassHierarchy cha;

    public JavascriptAnalysis(String... jsFiles) throws ClassHierarchyException {
        JSCallGraphUtil.setTranslatorFactory(new CAstRhinoTranslatorFactory());
        this.cha = JSCallGraphUtil.makeHierarchyForScripts(jsFiles);
        factory =  AstIRFactory.makeDefaultFactory();
    }

    public ArrayList<IMethod> getMethods() {
        ArrayList<IMethod> methods = new ArrayList<>();
        for (IClass klass: this.cha) {
            if (!klass.getName().toString().startsWith("Lprologue.js")) {
                IMethod m = klass.getMethod(AstMethodReference.fnSelector);
                if (m != null)
                    methods.add(m);
            }
        }
        return methods;
    }

    public IR makeIR(IMethod m) {
        return factory.makeIR(m, Everywhere.EVERYWHERE, SSAOptions.defaultOptions());
    }
}
