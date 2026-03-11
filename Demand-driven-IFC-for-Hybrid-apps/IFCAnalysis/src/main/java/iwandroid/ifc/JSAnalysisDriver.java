package iwandroid.ifc;

import com.ibm.wala.dataflow.IFDS.ISupergraph;
import com.ibm.wala.dataflow.IFDS.TabulationResult;
import com.ibm.wala.dataflow.IFDS.TabulationSolver;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.util.CancelException;

import java.util.HashMap;
import java.util.Set;

public class JSAnalysisDriver {

    protected TabulationResult<BasicBlockInContext<IExplodedBasicBlock>, CGNode, FlowFact> result = null;
    protected CGNode entrypoint;
    protected FlowFactDomain domain;
    protected ISupergraph<BasicBlockInContext<IExplodedBasicBlock>, CGNode> supergraph;
    protected TabulationSolver<BasicBlockInContext<IExplodedBasicBlock>, CGNode, FlowFact> solver;
    protected JSAnalysisFlowFunction flowfunctions;
    protected JSAnalysisProblem problem;

    public JSAnalysisDriver(CGNode entrypoint, ISupergraph<BasicBlockInContext<IExplodedBasicBlock>, CGNode> supergraph, HashMap<CGNode, Set<FlowPathFact>> bridgesummaries) {
        this.entrypoint = entrypoint;
        this.supergraph = supergraph;
        this.domain = new FlowFactDomain();
        this.problem = new JSAnalysisProblem(entrypoint, supergraph, bridgesummaries, this.domain);
    }

    public TabulationResult<BasicBlockInContext<IExplodedBasicBlock>, CGNode, FlowFact> analyze() {
        this.solver = TabulationSolver.make(this.problem);
        try {
            this.result = solver.solve();
            return this.result;
        } catch (CancelException e) {
            throw new IllegalStateException("Failed to solve the constraints");
        }
    }


    public TabulationResult<BasicBlockInContext<IExplodedBasicBlock>, CGNode, FlowFact> getResults() {
        assert this.result != null;
        return this.result;
    }

}
