package iwandroid.ifc;

import com.ibm.wala.dataflow.IFDS.*;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;

import java.util.*;

public class JSAnalysisProblem implements TabulationProblem<BasicBlockInContext<IExplodedBasicBlock>, CGNode, FlowFact> {

    private ISupergraph<BasicBlockInContext<IExplodedBasicBlock>, CGNode> supergraph;
    private List<String> jsFiles;
    private HashMap<CGNode, Set<FlowPathFact>> bridgeSummaries;
    private CGNode entryNode;
    private CallGraph callgraph;
    private JSAnalysisFlowFunction flowFunctions;
    private FlowFactDomain domain;

    public JSAnalysisProblem(CGNode node,
                             ISupergraph<BasicBlockInContext<IExplodedBasicBlock>, CGNode> supergraph,
                             HashMap<CGNode, Set<FlowPathFact>> bridgeSummaries, FlowFactDomain domain) {
        this.supergraph = supergraph;
        this.bridgeSummaries  = bridgeSummaries;
        this.flowFunctions = new JSAnalysisFlowFunction(node, domain, bridgeSummaries);
//        this.flowFunctions = new JSAnalysisFlowFunction(t);
        this.entryNode = node;
        this.domain = new FlowFactDomain();
    }

    @Override
    public ISupergraph<BasicBlockInContext<IExplodedBasicBlock>, CGNode> getSupergraph() {
        return this.supergraph;
    }

    @Override
    public TabulationDomain<FlowFact, BasicBlockInContext<IExplodedBasicBlock>> getDomain() {
        return this.domain;
    }

    @Override
    public IFlowFunctionMap<BasicBlockInContext<IExplodedBasicBlock>> getFunctionMap() {
        return this.flowFunctions;
    }

    /**
     * Define the set of path edges to start propagation with.
     */
    @Override
    public Collection<PathEdge<BasicBlockInContext<IExplodedBasicBlock>>> initialSeeds() {
        Collection<PathEdge<BasicBlockInContext<IExplodedBasicBlock>>> pathEdges = new ArrayList<>();
        IR ir = entryNode.getIR();
        var entryblock = ir.getControlFlowGraph().entry().getGraphNodeId();
        var superentryblock = supergraph.getLocalBlock(this.entryNode, entryblock);
        pathEdges.add(PathEdge.createPathEdge(superentryblock, 0, superentryblock, 0));
        return pathEdges;
    }

    /**
     * Special case: if supportsMerge(), then the problem is not really IFDS anymore. (TODO: rename
     * it?). Instead, we perform a merge operation before propagating at every program point. This
     * way, we can implement standard interprocedural dataflow and ESP-style property simulation, and
     * various other things.
     *
     * @return the merge function, or null if !supportsMerge()
     */
    @Override
    public IMergeFunction getMergeFunction() {
        return null;
    }
}
