package iwandroid.ifc;

import com.ibm.wala.dataflow.IFDS.*;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import iwandroid.utils.SourceSinkManager;

import java.util.ArrayList;
import java.util.Collection;

public class ForwardIFCAnalysisProblem implements TabulationProblem<BasicBlockInContext<IExplodedBasicBlock>, CGNode, FlowFact> {

    private final CGNode entrypoint;
    private final FlowFactDomain domain;
    private final ICFGSupergraph supergraph;
    private SourceSinkManager ssm;
    private IFlowFunctionMap<BasicBlockInContext<IExplodedBasicBlock>> flowfunctions;

    public ForwardIFCAnalysisProblem(CGNode node, FlowFactDomain domain, ICFGSupergraph supergraph, SourceSinkManager manager, IFlowFunctionMap<BasicBlockInContext<IExplodedBasicBlock>> flowfunctions) {
        this.entrypoint = node;
        this.domain = domain;
        this.supergraph = supergraph;
        this.ssm = manager;
        this.flowfunctions = flowfunctions;
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
        return this.flowfunctions;
    }

    @Override
    public Collection<PathEdge<BasicBlockInContext<IExplodedBasicBlock>>> initialSeeds() {
        //initialise every symbol in the symbol table
        IR ir = entrypoint.getIR();
        var symbolTable = ir.getSymbolTable();
        Collection<PathEdge<BasicBlockInContext<IExplodedBasicBlock>>> initPathEdges = new ArrayList<>();
        var entryBlock = ir.getControlFlowGraph().entry().getGraphNodeId();
        // add all parameters of the bridge method as seed values
        // essentially, for every f(n1, n2, ..., nn), it creates the pathedge <f, n_i> --> <f, n_i> for n_i in {n1, .., nn}
        var entrySuperblock = supergraph.getLocalBlock(this.entrypoint, entryBlock);
        initPathEdges.add(PathEdge.createPathEdge(entrySuperblock, 0, entrySuperblock, 0));
//        for (int i = 0; i < symbolTable.getMaxValueNumber(); ++i) {
//            int factId = this.domain.add(new IfcAnalysisFact(this.entrypoint, i, null, IFCLabel.PUBLIC));
//            initPathEdges.add(PathEdge.createPathEdge(entrySuperblock, factId, entrySuperblock, factId));
//        }
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
