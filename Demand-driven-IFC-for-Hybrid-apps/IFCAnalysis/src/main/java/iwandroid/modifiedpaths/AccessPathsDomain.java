package iwandroid.modifiedpaths;

import com.ibm.wala.dataflow.IFDS.PathEdge;
import com.ibm.wala.dataflow.IFDS.TabulationDomain;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.util.intset.MutableMapping;
import iwandroid.accesspaths.AccessGraph;

public class AccessPathsDomain extends MutableMapping<AccessGraph> implements TabulationDomain<AccessGraph, BasicBlockInContext<IExplodedBasicBlock>> {
    @Override
    public boolean hasPriorityOver(PathEdge<BasicBlockInContext<IExplodedBasicBlock>> pathEdge, PathEdge<BasicBlockInContext<IExplodedBasicBlock>> pathEdge1) {
        return false;
    }
}
