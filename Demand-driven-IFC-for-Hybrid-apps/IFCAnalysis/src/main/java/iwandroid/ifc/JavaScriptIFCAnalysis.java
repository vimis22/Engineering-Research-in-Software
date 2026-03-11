package iwandroid.ifc;

import com.ibm.wala.dataflow.IFDS.*;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import iwandroid.dbinterfaces.BridgedMethod;
import iwandroid.utils.SourceSinkManager;

import java.util.HashMap;

public class JavaScriptIFCAnalysis extends  AbstractIfcAnalysis<BasicBlockInContext<IExplodedBasicBlock>, CGNode, FlowFactDomain> {

    protected HashMap<BridgedMethod, BridgeMethodIFCSummaryDriver> bridgesummaries;

    protected JavaScriptIFCAnalysis(CGNode bridgeNode, FlowFactDomain domain, ISupergraph<BasicBlockInContext<IExplodedBasicBlock>, CGNode> supergraph, HashMap<BridgedMethod, BridgeMethodIFCSummaryDriver> bridgesummaries, SourceSinkManager manager) {
        super(bridgeNode, domain, supergraph);
        this.bridgesummaries = bridgesummaries;
    }


}
