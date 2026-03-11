package iwandroid.ifc;

import com.ibm.wala.dataflow.IFDS.BackwardsSupergraph;
import com.ibm.wala.dataflow.IFDS.ICFGSupergraph;
import com.ibm.wala.dataflow.IFDS.ISupergraph;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;

public class WebviewCallAnalysis {
    private final ISupergraph<BasicBlockInContext<IExplodedBasicBlock>, CGNode> backwardsupergraph;
    private final CGNode entryPoint;
    //    private final AccessGraphDomain domain;
//    private final HashMap<CGNode, IfcAnalysisFact> returnFacts;
    private static final int RETURN_VALUE = Integer.MAX_VALUE;

    WebviewCallAnalysis(CallGraph cg, CGNode entryPoint) {
        this.entryPoint = entryPoint;
        this.backwardsupergraph = BackwardsSupergraph.make(ICFGSupergraph.make(cg));
    }
}
