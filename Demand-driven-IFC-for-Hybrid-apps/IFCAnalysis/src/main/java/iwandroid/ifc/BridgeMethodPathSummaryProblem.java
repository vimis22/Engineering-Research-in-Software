package iwandroid.ifc;

import com.ibm.wala.dataflow.IFDS.*;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import iwandroid.utils.SourceSinkManager;

import java.util.ArrayList;
import java.util.Collection;

public class BridgeMethodPathSummaryProblem implements TabulationProblem<BasicBlockInContext<IExplodedBasicBlock>, CGNode, FlowPathFact> {

    private CGNode entrypoint;
    private final FlowPathFactDomain domain;
    private final ISupergraph<BasicBlockInContext<IExplodedBasicBlock>, CGNode> supergraph;
    private SourceSinkManager ssm;
    private final IFlowFunctionMap<BasicBlockInContext<IExplodedBasicBlock>> functionMap;

    protected BridgeMethodPathSummaryProblem(CGNode node,
                                             FlowPathFactDomain domain,
                                             ISupergraph<BasicBlockInContext<IExplodedBasicBlock>, CGNode> supergraph,
                                             SourceSinkManager manager,
                                             IFlowFunctionMap<BasicBlockInContext<IExplodedBasicBlock>> functionMap) {
        this.entrypoint = node;
        this.domain = domain;
        this.supergraph = supergraph;
        this.ssm = manager;
        this.functionMap = functionMap;
    }

    @Override
    public ISupergraph<BasicBlockInContext<IExplodedBasicBlock>, CGNode> getSupergraph() {
        return this.supergraph;
    }

    @Override
    public TabulationDomain<FlowPathFact, BasicBlockInContext<IExplodedBasicBlock>> getDomain() {
        return this.domain;
    }

    @Override
    public IFlowFunctionMap<BasicBlockInContext<IExplodedBasicBlock>> getFunctionMap() {
        return this.functionMap;
    }

    /**
     * Define the set of path edges to start propagation with.
     */
    @Override
    public Collection<PathEdge<BasicBlockInContext<IExplodedBasicBlock>>> initialSeeds() {
        IR ir = entrypoint.getIR();
        Collection<PathEdge<BasicBlockInContext<IExplodedBasicBlock>>> initPathEdges = new ArrayList<>();
        var entryBlock = ir.getControlFlowGraph().entry().getGraphNodeId();
        var entrySuperblock = supergraph.getLocalBlock(this.entrypoint, entryBlock);
        initPathEdges.add(PathEdge.createPathEdge(entrySuperblock, 0, entrySuperblock, 0));
        for (int i=1; i < ir.getSymbolTable().getMaxValueNumber(); ++i) {
            initPathEdges.add(PathEdge.createPathEdge(entrySuperblock, i, entrySuperblock, i));
        }
        return initPathEdges;
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
