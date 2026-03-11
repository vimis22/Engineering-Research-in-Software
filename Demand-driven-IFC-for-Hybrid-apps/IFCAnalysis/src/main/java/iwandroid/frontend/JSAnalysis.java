package iwandroid.frontend;

import com.ibm.wala.cast.js.translator.CAstRhinoTranslatorFactory;
import com.ibm.wala.cast.js.util.JSCallGraphBuilderUtil;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;

import java.io.IOException;
import java.util.Set;

public class JSAnalysis {
    private Set<String> jsfiles;
    private String jsDir;
    CallGraph callGraph;
    IClassHierarchy cha;

    public JSAnalysis(String jsDir, String jsfile) throws WalaException, IOException, CancelException {
        com.ibm.wala.cast.js.ipa.callgraph.JSCallGraphUtil.setTranslatorFactory(new CAstRhinoTranslatorFactory());
//        this.cha = makeHierarchyForScripts(jsfiles);
//        JavaScriptLoaderFactory factory = new JavaScriptLoaderFactory(new CAstRhinoTranslatorFactory());
//        AnalysisScope scope = JSCallGraphBuilderUtil.makeScope(jsfiles, factory, factory.getTheLoader().getLanguage());
//        AnalysisOptions options = JSCallGraphBuilderUtil.makeOptions(scope, cha, )
//        var util = new FieldBasedCallGraphBuilder(cha, makeOptions(scope, cha), new AnalysisCacheImpl(), )
//        this.callGraph = FieldBasedCallGraphBuilder
        try {
            var cgBuilder = JSCallGraphBuilderUtil.makeScriptCGBuilder(jsDir, jsfile);
            this.callGraph = cgBuilder.makeCallGraph(cgBuilder.getOptions());
        } catch (WalaException | IOException | CancelException | IllegalArgumentException e) {
            // do nothing
            System.out.println("Unexpected exception");
            e.printStackTrace();
        }
    }

    public CallGraph getCallGraph() {
        return callGraph;
    }

    //    private ICFGSupergraph supergraph = null;
//    private ForwardAnalysisFlowFunctions flowFunctions = null;
//    CGNode entrypoint;
//    FlowFactDomain domain = null;
//
//    public JSAnalysis(CallGraph jsCallG, CGNode entrypoint, SourceSinkManager manager) {
//        this.supergraph = JSSupergraph.make(jsCallG);
//        domain = new FlowFactDomain();
//        flowFunctions = new ForwardAnalysisFlowFunctions(entrypoint, domain, manager);
//    }
//
//    public void run() {
////        ForwardIFCAnalysisProblem analysis = new ForwardIFCAnalysisProblem(entrypoint, domain, supergraph);
//    }
}
