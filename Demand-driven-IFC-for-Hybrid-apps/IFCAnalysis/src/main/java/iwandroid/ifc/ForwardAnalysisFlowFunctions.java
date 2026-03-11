package iwandroid.ifc;

import com.ibm.wala.classLoader.IField;
import com.ibm.wala.dataflow.IFDS.IFlowFunction;
import com.ibm.wala.dataflow.IFDS.IFlowFunctionMap;
import com.ibm.wala.dataflow.IFDS.IUnaryFlowFunction;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.*;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.intset.MutableIntSet;
import com.ibm.wala.util.intset.MutableSparseIntSet;
import iwandroid.accesspaths.AccessGraph;
import iwandroid.accesspaths.FieldGraph;
import iwandroid.utils.SourceSinkManager;

import java.util.HashMap;

public class ForwardAnalysisFlowFunctions implements IFlowFunctionMap<BasicBlockInContext<IExplodedBasicBlock>> {

    protected FlowFactDomain domain;
    protected CGNode entryPoint;
    protected HashMap<CGNode, FlowFact> returnFacts;
    protected static final int RETURN_VALUE = Integer.MAX_VALUE;
    protected SourceSinkManager ssm;

    private static final boolean TRACE = false;

    public ForwardAnalysisFlowFunctions(CGNode entryPoint, FlowFactDomain domain, SourceSinkManager sources) {
        this.entryPoint = entryPoint;
        this.domain = domain;
        this.returnFacts = HashMapFactory.make();
        this.ssm = sources;
    }

    protected IClassHierarchy getClassHierarchy() {
        return entryPoint.getClassHierarchy();
    }

    public MutableIntSet buildEntryBlockFunction(BasicBlockInContext<IExplodedBasicBlock> src) {
        if (TRACE) {
            System.err.println("\t\t" + src);
        }
        MutableIntSet intset = MutableSparseIntSet.makeEmpty();
        IR ir = src.getNode().getIR();
        for (int vn = 1; vn <= ir.getSymbolTable().getMaxValueNumber(); ++vn) {
            int id = domain.add(new FlowFact(src.getNode(), vn, null, IFCLabel.PUBLIC));
            intset.add(id);
        }
        return intset;
    }

    @Override
    public IUnaryFlowFunction getNormalFlowFunction(BasicBlockInContext<IExplodedBasicBlock> src, BasicBlockInContext<IExplodedBasicBlock> dst) {
        //        logger.info("JP--DEBUG " + src.getDelegate().getInstruction() + " --> " + dst.getDelegate().getInstruction());
        if (TRACE) {
            System.err.println("NORMAL FLOW FUNCTION \n\t" +  src + "\n\t" + dst);
        }

        SSAInstruction inst = FlowFunctionUtils.getInstruction(src);
        MutableIntSet entryfacts = MutableSparseIntSet.makeEmpty();
        if (src.isExitBlock()) {
            entryfacts = buildEntryBlockFunction(src);
        }


        IUnaryFlowFunction result = null;
        if (inst instanceof SSANewInstruction newInst) {
            result = buildNewInstructionFunction(newInst, src.getNode(), entryfacts);
        } else if (inst instanceof SSAPutInstruction put) {
            result = buildPutInstructionFunction(put, src.getNode(), entryfacts);
        } else if (inst instanceof SSAGetInstruction get) {
            result = buildGetInstruction(get, src.getNode(), entryfacts);
        } else if (inst instanceof SSAReturnInstruction returnInst) {
            result = buildReturnInstruction(returnInst, src.getNode(), entryfacts);
        } else if (inst instanceof SSAPhiInstruction phiInst) {
            result = buildPhiInstruction(phiInst, src.getNode(), entryfacts);
        } else {
            result = IdentityFunction.identity();
        }
        return result;
    }

    protected IUnaryFlowFunction buildPhiInstruction(SSAPhiInstruction inst, CGNode node, MutableIntSet entryfacts) {
        return d1 -> {
            MutableIntSet result = MutableSparseIntSet.make(entryfacts);
            result.add(d1);
            for (int i = 0; i < inst.getNumberOfUses(); ++i) {
                int use = inst.getUse(i);
                var srcFact = domain.getMappedObject(d1);
                if (srcFact.getBase() == use) {
                    FlowFact fact = new FlowFact(node, RETURN_VALUE, null, srcFact.ifclabel());
                    result.add(domain.add(fact));
                } else {
                    result.add(d1);
                }
            }
            return result;
        };
    }

    protected IUnaryFlowFunction buildReturnInstruction(SSAReturnInstruction inst, CGNode node, MutableIntSet entryfacts) {
        return d1 -> {
            final MutableIntSet result = MutableSparseIntSet.make(entryfacts);
            int src = inst.getUse(0);
            var fact = domain.getMappedObject(d1);
            if (fact.getBase() == src) {
                FlowFact newFact = new FlowFact(node, RETURN_VALUE, null, fact.ifclabel());
                result.add(domain.add(newFact));
            } else {
                result.add(d1);
            }
            return result;
        };
    }

    protected IUnaryFlowFunction buildGetInstruction(SSAGetInstruction inst, CGNode node, MutableIntSet entryfacts) {
        if (inst.getNumberOfUses() == 0) {
            // in case there aren't any variables in the use set, propagte the defined variable. $$undefined$$ value in
            // use set
            return d1 -> {
                MutableIntSet result = MutableSparseIntSet.makeEmpty();
                result.add(d1);
                int def = inst.getDef();
                if (def != -1) {
                    int newFactId = domain.add(new FlowFact(node, def, null, IFCLabel.PUBLIC));
                    result.add(newFactId);
                }
                return result;
            };

        }
        return d1 -> {

            int src = inst.getUse(0);
            int dst = inst.getDef();
            IField field = FlowFunctionUtils.resolveField(node.getClassHierarchy(), inst.getDeclaredField());
            MutableIntSet result = MutableSparseIntSet.make(entryfacts);
            result.add(d1);
            var srcTaintInfo = domain.getMappedObject(d1);
            if (srcTaintInfo.getBase() == src) {
                FlowFact dstFact = new FlowFact(node, dst, srcTaintInfo.fieldgraph(), srcTaintInfo.ifclabel());
                result.add(domain.add(dstFact));
            }
            return result;
        };
    }

    protected IUnaryFlowFunction buildPutInstructionFunction(SSAPutInstruction inst, CGNode node, MutableIntSet entryfacts) {
        if (inst.isStatic()) {
            return d1 -> {
                MutableIntSet result = MutableSparseIntSet.make(entryfacts);
                result.add(d1);
                return result;
            };
        }

        return d1 -> {
            int dst = inst.getUse(0);
            int src = inst.getUse(1);
            IField field = FlowFunctionUtils.resolveField(getClassHierarchy(), inst.getDeclaredField());
            MutableIntSet result = MutableSparseIntSet.make(entryfacts);
            result.add(d1);
            var srcTaintInfo = domain.getMappedObject(d1);
            if (srcTaintInfo.getBase() == src) {
                FlowFact fact = new FlowFact(node, dst, FieldGraph.of(field), srcTaintInfo.ifclabel());
                result.add(domain.add(fact));
            } else {
                result.add(d1); // return identify function
            }
            return result;
        };
    }

    public IUnaryFlowFunction buildNewInstructionFunction(SSANewInstruction inst, CGNode node, MutableIntSet entryfacts) {
        return d1 -> {
            int def = inst.getDef();
            MutableIntSet result = MutableSparseIntSet.make(entryfacts);
            result.add(d1);
            FlowFact fact = new FlowFact(new AccessGraph(node, def), IFCLabel.PUBLIC);
            int x = domain.add(fact);
            result.add(x);
            result.add(d1);
            return result;
        };
    }

    @Override
    public IUnaryFlowFunction getCallFlowFunction(BasicBlockInContext<IExplodedBasicBlock> src,
                                                  BasicBlockInContext<IExplodedBasicBlock> dst,
                                                  BasicBlockInContext<IExplodedBasicBlock> ret) {
        SSAInvokeInstruction invoke = (SSAInvokeInstruction) FlowFunctionUtils.getInstruction(src);

        if (invoke == null) {
            return EmptyFunction.empty();
        }

        if (FlowFunctionUtils.isSensitiveSource(ssm, invoke.getCallSite())) {
            System.out.println("Reading from sensitive Source " + invoke);
            return d1 -> {
                MutableIntSet result = MutableSparseIntSet.makeEmpty();

                if (invoke.hasDef()) {
                    result.add(d1);
                    int def = invoke.getDef();
                    if (def != -1) {
                        int newFactId = domain.add(new FlowFact(src.getNode(), def, null, IFCLabel.SECRET));
                        result.add(newFactId);
                    }
                }
                return result;
            };
        }

        if (FlowFunctionUtils.isSensitiveSink(ssm, invoke.getCallSite())) {
            System.out.println("Leak to sink " + invoke.getCallSite());
        }

        if (FlowFunctionUtils.isLibraryCall(invoke.getCallSite())) {
            // propagate library calls by replqcing it with identity functions
            if (invoke.hasDef()) {
                return d1 -> {
                    MutableIntSet result = MutableSparseIntSet.makeEmpty();
                    result.add(d1);
                    int def = invoke.getDef();
                    int newFactId = domain.add(new FlowFact(src.getNode(), def, null, IFCLabel.SECRET));
                    result.add(newFactId);
                    return result;
                };
            } else {
                return EmptyFunction.empty();
            }
        }

        // skip analysing these calls and replace it with identity functions
        return d1 -> {
            MutableIntSet result = MutableSparseIntSet.makeEmpty();
            var fact = domain.getMappedObject(d1);
            for (int i = 0; i < invoke.getNumberOfPositionalParameters(); ++i) {
                if (invoke.getUse(i) == fact.getBase()) {
                    if (i != 0) {
                        FlowFact newfact = new FlowFact(dst.getNode(), i + 1, fact.fieldgraph(), fact.ifclabel());
                        result.add(domain.add(newfact));
                    }
                }
            }
            return result;
        };
    }

    @Override
    public IFlowFunction getReturnFlowFunction(BasicBlockInContext<IExplodedBasicBlock> call, BasicBlockInContext<IExplodedBasicBlock> src, BasicBlockInContext<IExplodedBasicBlock> dest) {

        if (call == null) {
            return IdentityFunction.identity();
        }


        SSAAbstractInvokeInstruction callInstruction = (SSAAbstractInvokeInstruction) FlowFunctionUtils.getInstruction(call);
        SSAReturnInstruction returnInst = (SSAReturnInstruction) FlowFunctionUtils.getInstruction(src);

        if (callInstruction == null) {
            return EmptyFunction.empty();
        }

        // In case the return instruction is null, pass the empty set
        if (returnInst == null) {
            return EmptyFunction.empty();
        }

        if (FlowFunctionUtils.isLibraryCall(callInstruction.getCallSite())) {
            int newFactId = domain.add(new FlowFact(src.getNode(), RETURN_VALUE, null, IFCLabel.PUBLIC));
            return (IUnaryFlowFunction) d1 -> {
                MutableIntSet result = MutableSparseIntSet.makeEmpty();
                result.add(d1);
                result.add(newFactId);
                return result;
            };
        }

        return (IUnaryFlowFunction) d1 -> {
            MutableIntSet result = MutableSparseIntSet.makeEmpty();
            // if the use of d1 is reachable
            var flowFact = domain.getMappedObject(d1);
            if (flowFact.getBase() == returnInst.getUse(0)) {
                int def = callInstruction.getDef();
                if (def != -1) {
                    var fakeReturn = returnFacts.get(src.getNode());
                    var newFlowFact = new FlowFact(call.getNode(), def, null, fakeReturn.ifclabel());
                    result.add(domain.add(newFlowFact));
                }
            }
            return result;
        };
    }


    @Override
    public IUnaryFlowFunction getCallToReturnFlowFunction(BasicBlockInContext<IExplodedBasicBlock> ssaInstructions, BasicBlockInContext<IExplodedBasicBlock> t1) {
        return IdentityFunction.identity();
    }

    @Override
    public IUnaryFlowFunction getCallNoneToReturnFlowFunction(BasicBlockInContext<IExplodedBasicBlock> ssaInstructions, BasicBlockInContext<IExplodedBasicBlock> t1) {
        return IdentityFunction.identity();
    }

    public FlowFactDomain getDomain() {
        return domain;
    }
}
