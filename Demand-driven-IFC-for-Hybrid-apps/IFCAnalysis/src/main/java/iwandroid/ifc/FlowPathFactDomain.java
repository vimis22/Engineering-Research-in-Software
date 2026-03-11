package iwandroid.ifc;

import com.ibm.wala.dataflow.IFDS.PathEdge;
import com.ibm.wala.dataflow.IFDS.TabulationDomain;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.util.intset.MutableMapping;

public class FlowPathFactDomain extends MutableMapping<FlowPathFact> implements TabulationDomain<FlowPathFact, BasicBlockInContext<IExplodedBasicBlock>> {

    /**
     * returns {@code true} if p1 should be processed before p2 by the {@link TabulationSolver}
     *
     * <p>For example, if this domain supports a partial order on facts, return true if p1.d2 is
     * weaker than p2.d2 (intuitively p1.d2 meet p2.d2 = p1.d2)
     *
     * <p>return false otherwise
     *
     * @param p1
     * @param p2
     */
    @Override
    public boolean hasPriorityOver(PathEdge<BasicBlockInContext<IExplodedBasicBlock>> p1, PathEdge<BasicBlockInContext<IExplodedBasicBlock>> p2) {
        return false;
    }
}
