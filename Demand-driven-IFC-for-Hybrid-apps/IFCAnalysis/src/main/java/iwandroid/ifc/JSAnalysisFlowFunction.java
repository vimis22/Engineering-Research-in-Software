package iwandroid.ifc;

import com.ibm.wala.cast.js.types.JavaScriptMethods;
import com.ibm.wala.dataflow.IFDS.IUnaryFlowFunction;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.util.intset.MutableIntSet;
import com.ibm.wala.util.intset.MutableSparseIntSet;
import iwandroid.utils.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Set;

public class JSAnalysisFlowFunction extends ForwardAnalysisFlowFunctions {


    private static final boolean TRACE = true;

    private final HashMap<CGNode, Set<FlowPathFact>> bridgeSummaries;

    private final static Logger logger = LoggerFactory.getLogger(Config.TOOLNAME);


    public JSAnalysisFlowFunction(CGNode entryPoint, FlowFactDomain domain, HashMap<CGNode, Set<FlowPathFact>> bridgesummaries) {
        super(entryPoint, domain, null);
        this.bridgeSummaries = bridgesummaries;
    }

    @Override
    public IUnaryFlowFunction getCallFlowFunction(BasicBlockInContext<IExplodedBasicBlock> src, BasicBlockInContext<IExplodedBasicBlock> dst, BasicBlockInContext<IExplodedBasicBlock> ret) {
        SSAAbstractInvokeInstruction invoke = (SSAAbstractInvokeInstruction) FlowFunctionUtils.getInstruction(src);

        logger.info("JSAnalysisflowfunction.getCallFlowFunction " + invoke + " target is " + invoke.getDeclaredTarget());
        assert invoke != null;
        // check if the invoking method is a bridge method. In this case propagate the summaries
        if (isBridgeMethod(invoke)) {
            return generateBridgeSummaryFunction(bridgeSummaries.get(dst.getNode()), src, dst);
        }

        if (isJsLib(invoke)) {
            if (TRACE)
                // if the invoking method is a JS library, in this case, don't analyze the library and pass a dummy object for the returned value
                logger.info(invoke + "  is a JS library or constructor reference");
            if (!invoke.hasDef()) {
                return EmptyFunction.empty();
            } else {
                int def = invoke.getDef();
                return d1 -> {
                    MutableSparseIntSet result = MutableSparseIntSet.makeEmpty();
                    int newFactId = domain.add(new FlowFact(src.getNode(), def, null, IFCLabel.PUBLIC));
                    result.add(newFactId);
                    result.add(d1);
                    return result;
                };
            }
        } else {
            // else propagate the parameters from caller to called function
            return d1 -> {
                MutableIntSet result = MutableSparseIntSet.makeEmpty();
                result.add(d1);

                for (int paramI = 0; paramI < invoke.getNumberOfPositionalParameters(); ++paramI) {
                    int actualArg = invoke.getUse(paramI);
                    FlowFact f = domain.getMappedObject(d1);
                    if (f.getBase() == actualArg) {
                        int newFactId = domain.add(new FlowFact(dst.getNode(), paramI + 1, f.fieldgraph(), IFCLabel.PUBLIC));
                        result.add(newFactId);
                    }
                }
                return result;
            };
        }
    }

    private boolean isJsLib(SSAAbstractInvokeInstruction invoke) {
        return JSCoreLibrary.exists(invoke.getDeclaredTarget().getDeclaringClass().toString());
    }

    private boolean isConstructorReference(SSAAbstractInvokeInstruction invoke) {
        return invoke.getDeclaredTarget().equals(JavaScriptMethods.ctorReference);
    }

    private IUnaryFlowFunction generateBridgeSummaryFunction(Set<FlowPathFact> flowPathFacts,
                                                             BasicBlockInContext<IExplodedBasicBlock> src,
                                                             BasicBlockInContext<IExplodedBasicBlock> dst) {
        // map each path of the destination node to the source node
        return d1 -> {
            SSAInvokeInstruction invoke = (SSAInvokeInstruction) FlowFunctionUtils.getInstruction(src);
            assert invoke != null;
            MutableIntSet result = MutableSparseIntSet.makeEmpty();

            for (int paramI = 0; paramI < invoke.getNumberOfPositionalParameters(); ++paramI) {
                logger.info("Potential Integrity Violation " + invoke);
                int aArg = invoke.getUse(paramI);
                var flowfact = domain.getMappedObject(d1);
                if (flowfact.getBase() == aArg) {
                    // find it in formal parameter and build the summary function
                    CGNode targetNode = dst.getNode();
                    int finalParamI = paramI;
                    var paths = flowPathFacts.stream().filter(path -> path.init().getCGNode().equals(targetNode) && (path.init().getBase() == finalParamI));
                    paths.forEach(path -> {
                        int newInitFact = domain.add(
                                new FlowFact(src.getNode(), path.init().getBase(), path.init().fieldgraph(), path.init().ifclabel()));
                        int newTailFact = domain.add(
                                new FlowFact(src.getNode(), path.last().getBase(), path.last().fieldgraph(), path.last().ifclabel()));
                        result.add(newInitFact);
                        result.add(newTailFact);
                    });
                }
            }
            return result;
        };
    }


    private Set<CGNode> getBridgeMethods() {
        return bridgeSummaries.keySet();
    }


    private boolean isBridgeMethod(SSAAbstractInvokeInstruction instruction) {
        var target = instruction.getDeclaredTarget();
        return getBridgeMethods().stream().anyMatch(method -> method.getMethod().getReference().equals(target));
    }

} // JSAnalysisFunction
