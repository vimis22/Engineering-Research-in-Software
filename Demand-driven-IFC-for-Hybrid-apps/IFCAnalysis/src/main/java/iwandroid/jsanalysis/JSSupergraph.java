package iwandroid.jsanalysis;

import com.ibm.wala.dataflow.IFDS.BackwardsSupergraph;
import com.ibm.wala.dataflow.IFDS.ICFGSupergraph;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ipa.cfg.ExplodedInterproceduralCFG;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;


public class JSSupergraph extends ICFGSupergraph {

    protected static final Logger logger = LogManager.getLogger(JSSupergraph.class.getName());


    protected JSSupergraph(ExplodedInterproceduralCFG cfg) {
        super(cfg);
    }

    @Contract("_ -> new")
    public static @NotNull JSSupergraph make(ExplodedInterproceduralCFG cfg) {
        return new JSSupergraph(cfg);
    }

    @Override
    public Iterator<BasicBlockInContext<IExplodedBasicBlock>> getCalledNodes(BasicBlockInContext<IExplodedBasicBlock> call) {
        var backwards = BackwardsSupergraph.make(this);
        DefUse du = call.getNode().getDU();
        du.getDef(call.getLastInstruction().getDef(9));
        var nodes = backwards.getSuccNodes(call);
        logger.debug(call);
        return nodes;
    }
}
