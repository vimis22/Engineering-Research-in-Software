package iwandroid.ifc;

import com.ibm.wala.dataflow.IFDS.PathEdge;
import com.ibm.wala.dataflow.IFDS.TabulationDomain;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.util.intset.MutableMapping;

public class FlowFactDomain extends MutableMapping<FlowFact> implements TabulationDomain<FlowFact, BasicBlockInContext<IExplodedBasicBlock>> {


    @Override
    public boolean hasPriorityOver(PathEdge<BasicBlockInContext<IExplodedBasicBlock>> p1, PathEdge<BasicBlockInContext<IExplodedBasicBlock>> p2) {
        return true;
    }


}
