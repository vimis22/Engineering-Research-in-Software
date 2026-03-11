package iwandroid.modifiedpaths;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.dataflow.IFDS.*;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.*;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.intset.MutableIntSet;
import com.ibm.wala.util.intset.MutableSparseIntSet;
import iwandroid.accesspaths.AccessGraph;
import iwandroid.accesspaths.FieldGraph;
import iwandroid.utils.IFDSSolutionCollector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import org.apache.logging.log4j.*;

/**
 * Flow functions for modified path analysis
 */
public class ModifiedPathAnalysis {
    private final ICFGSupergraph supergraph;
    private final CGNode bridgeNode;
    private final HashMap<Object, Object> fakeReturns;
    private final AccessPathsDomain domain;
    private static final Logger logger = LogManager.getLogger(ModifiedPathAnalysis.class);
    private static final int FAKERETURNVALUE = Integer.MAX_VALUE;

    public ModifiedPathAnalysis(CallGraph callGraph, CGNode bridgeNode) {
        this.supergraph = ICFGSupergraph.make(callGraph);
        this.bridgeNode = bridgeNode;
        this.domain = new AccessPathsDomain();
        fakeReturns = HashMapFactory.make();
    }


    public AccessPathsDomain domain() {
        return this.domain;
    }

    public CGNode bridgemethod() {
        return this.bridgeNode;
    }

    protected class Flowfunctions implements IFlowFunctionMap<BasicBlockInContext<IExplodedBasicBlock>> {

        @Override
        public IUnaryFlowFunction getNormalFlowFunction(BasicBlockInContext<IExplodedBasicBlock> src, BasicBlockInContext<IExplodedBasicBlock> dst) {
            MutableIntSet entryfacts = MutableSparseIntSet.makeEmpty();
            SSAInstruction instr = getInstruction(src);
            if (instr instanceof SSANewInstruction newInst) {
                return getNewInstructionFlowFunction(newInst, entryfacts);
            }
            if (instr instanceof SSAPutInstruction put) {
                return getPutFieldFlowFunction(put, entryfacts);
            }
            if (instr instanceof SSAGetInstruction get) {
                return getGetFieldFlowFunction(get, entryfacts);
            }
            if (instr instanceof SSAPhiInstruction phi)  {
                return getPhiInstructionFlowFunction(phi, entryfacts);
            }
            if (instr instanceof SSAReturnInstruction returnInst) {
                return getReturnInstructionFlowFunction(returnInst, src.getNode(), entryfacts);
            }

            if (src.isEntryBlock()) {
                return identity(entryfacts);
            } else {
                return IdentityFlowFunction.identity();
            }
        }

        /**
         * Map the return value to a fake return value. The fake return value will be prpoagated to the caller function
         */
        private IUnaryFlowFunction getReturnInstructionFlowFunction(SSAReturnInstruction instr, CGNode node, MutableIntSet entryfacts) {
            int vr = instr.getUse(0);
            return d1 -> {
                AccessGraph graph = domain.getMappedObject(d1);
                if (graph == null) {
                    return entryfacts;
                }
                if (graph.hasBase(vr)) {
                    //Create a fake return value and add it to the
                    AccessGraph returnValue = new AccessGraph(node, FAKERETURNVALUE);
                    fakeReturns.put(node, returnValue);
                    int id = domain.add(returnValue);
                    entryfacts.add(id);
                }
                return entryfacts;
            };
        }

        private IUnaryFlowFunction getPhiInstructionFlowFunction(SSAPhiInstruction instr, MutableIntSet entry) {
        /*
            propagate the dataflow facts for phi instruction v1 = phi(v2, v3)
         */
            int v1 = instr.getDef();
            int v2 = instr.getUse(0);
            int v3 = instr.getUse(1);
            Set<Integer> uses = Set.of(v2, v3);
            return d1 -> {
                AccessGraph ag = domain.getMappedObject(d1);
                if (uses.contains(ag.getBaseVariable())) {
                    AccessGraph dst = new AccessGraph(ag.getCGNode(), v1);
                    int id = domain.add(dst);
                    entry.add(id);
                }
                return entry;
            };
        }

    /*
        for each x = v.f, add the field to the dataflow entry

     */

        private IUnaryFlowFunction getGetFieldFlowFunction(SSAGetInstruction get, MutableIntSet entry) {
            int src = get.getUse(0);
            int dest = get.getDef();

            return i -> {
                var accessgraph = domain.getMappedObject(i);
                if (accessgraph.hasBase(src)) {
                    AccessGraph newAccessgrpah = new AccessGraph(bridgeNode, dest, accessgraph.fieldGraph());
                    int id = domain.add(newAccessgrpah);
                    entry.add(id);
                    return entry;
                }
                return entry;
            };
        }

        /*
            for each x.f = v, add the field and put the dataflow
         */
        private IUnaryFlowFunction getPutFieldFlowFunction(SSAPutInstruction put, MutableIntSet entry) {
            int dest = put.getUse(0); // x
            int src = put.getUse(1); // v
            var field = resolveFieldReference(bridgeNode.getClassHierarchy(), put.getDeclaredField());


            return i -> {
                var accessgraph = domain.getMappedObject(i);
                if (accessgraph.hasBase(dest)) {
                    AccessGraph newAccessgraph = new AccessGraph(bridgeNode, src, FieldGraph.of(field));
                    int id = domain.add(newAccessgraph);
                    entry.add(id);
                }
                return entry; // identity function
            };
        }

        private IField resolveFieldReference(IClassHierarchy cha, FieldReference fr) {
            IField f = cha.resolveField(fr);
            assert f != null : "unable to resolve field [" + fr + "]";
            return f;
        }

        private IUnaryFlowFunction getNewInstructionFlowFunction(SSANewInstruction inst, MutableIntSet entry) {
            return d1 -> {
                AccessGraph newgraph = new AccessGraph(bridgeNode, inst.getDef());
                entry.add(domain.add(newgraph));
                return entry;
            };
        }


        private IUnaryFlowFunction identity(MutableIntSet entry) {
            return d1 -> MutableSparseIntSet.make(entry);
        }

        private SSAInstruction getInstruction(BasicBlockInContext<IExplodedBasicBlock> src) {
            return src.getDelegate().getInstruction();
        }

        private boolean isAndroidLibraryMethodCall(CallSiteReference callsite) {
            MethodReference method = callsite.getDeclaredTarget();
            TypeReference clazz = method.getDeclaringClass();
            return clazz.getName().toString().startsWith("LAndroid");
        }
        /**
         * @param src Call Instruction
         * @param dst Entry of the callee
         * @param ret the return value !! I don't fucking know why is this thing is required here!!
         * @return The flow function for the
         */
        @Override
        public IUnaryFlowFunction getCallFlowFunction(BasicBlockInContext<IExplodedBasicBlock> src, BasicBlockInContext<IExplodedBasicBlock> dst, BasicBlockInContext<IExplodedBasicBlock> ret) {
            SSAInvokeInstruction invoke = (SSAInvokeInstruction) getInstruction(src);

            if (isLibraryFunctionCall(invoke.getCallSite())) {
                // Skip library calls and replace them with identity function
                return IdentityFlowFunction.identity();
            }

            if (isAndroidLibraryMethodCall(invoke.getCallSite())) {
                return IdentityFlowFunction.identity();
            }

            if (invoke.getCallSite().isVirtual()) {
                //todo: handle virtual calls
            }
            // map the positional parameters and propagate the reachable parameters
            // map d1 to the dataflow fact of the called function

            /*
             * For each function call at site f(x, y, z) to a function f(a, b, c), map the access graph of x to a, y to b, and z to c
             */
            return d1 -> {
                MutableIntSet result = MutableSparseIntSet.makeEmpty();
                for (int i = 0; i < invoke.getNumberOfPositionalParameters(); ++i) {
                    AccessGraph graph = domain.getMappedObject(d1);
                    if (graph.hasBase(invoke.getUse(i))) {
                        AccessGraph newgraph = new AccessGraph(dst.getNode(), i, graph.fieldGraph());
                        result.add(domain.add(newgraph));
                    }
                }
                return result;
            };
        }

        private boolean isLibraryFunctionCall(CallSiteReference callsite) {
            return callsite.getDeclaredTarget().getDeclaringClass().getClassLoader().equals(ClassLoaderReference.Primordial);
        }

        private IUnaryFlowFunction empty() {
            return d1 -> MutableSparseIntSet.makeEmpty();
        }

        @Override
        public IFlowFunction getReturnFlowFunction(BasicBlockInContext<IExplodedBasicBlock> call, BasicBlockInContext<IExplodedBasicBlock> src, BasicBlockInContext<IExplodedBasicBlock> dst) {
            // src in the called function, dst is in caller function
            SSAInvokeInstruction invoke = (SSAInvokeInstruction) getInstruction(call);
            if (!invoke.hasDef()) {
                // don't propagate as there is no variable which captures the return value
                return empty();
            }
            int callerDef = invoke.getDef();
            return (IUnaryFlowFunction) d1 -> {
                AccessGraph returnvalue = domain.getMappedObject(d1);
                MutableIntSet result = MutableSparseIntSet.makeEmpty();
                if (returnvalue.hasBase(FAKERETURNVALUE)) {
                    AccessGraph capturingVariable = new AccessGraph(call.getNode(), callerDef);
                    result.add(domain.add(capturingVariable));
                }
                return result;
            };
        }

        @Override
        public IUnaryFlowFunction getCallToReturnFlowFunction(BasicBlockInContext<IExplodedBasicBlock> ssaInstructions, BasicBlockInContext<IExplodedBasicBlock> t1) {
            return IdentityFlowFunction.identity();
        }

        @Override
        public IUnaryFlowFunction getCallNoneToReturnFlowFunction(BasicBlockInContext<IExplodedBasicBlock> ssaInstructions, BasicBlockInContext<IExplodedBasicBlock> t1) {
            return IdentityFlowFunction.identity();
        }
    } // end of flow functions

    public ICFGSupergraph supergraph() {
        return supergraph;
    }

    protected class MPAProblem implements TabulationProblem<BasicBlockInContext<IExplodedBasicBlock>, CGNode, AccessGraph> {

        private final Flowfunctions flowfunctions = new Flowfunctions();

        @Override
        public ISupergraph<BasicBlockInContext<IExplodedBasicBlock>, CGNode> getSupergraph() {
            return supergraph();
        }

        @Override
        public TabulationDomain<AccessGraph, BasicBlockInContext<IExplodedBasicBlock>> getDomain() {
            return domain;
        }

        @Override
        public IFlowFunctionMap<BasicBlockInContext<IExplodedBasicBlock>> getFunctionMap() {
            return this.flowfunctions;
        }

        @Override
        public Collection<PathEdge<BasicBlockInContext<IExplodedBasicBlock>>> initialSeeds() {
            CGNode entryMethod = bridgemethod();
            ArrayList<PathEdge<BasicBlockInContext<IExplodedBasicBlock>>> seeds = new ArrayList<>();
            var entryBlock = entryMethod.getIR().getControlFlowGraph().entry().getGraphNodeId();
            /*
                add all parameters of the bridge method as seed values. It essentially adds, for every
                f(n1, n2, ..., nn), it creates the pathedge <f, n_i> --> <f, n_i> for n_i in {n1, .., nn}
             */
            var entrySuperblock = supergraph().getLocalBlock(entryMethod, entryBlock);
            for (int param : entryMethod.getIR().getParameterValueNumbers()) {
                // ignore the first parameter as it denotes "this" parameter.
                if (param != 0) {
                    AccessGraph paramI = new AccessGraph(entryMethod, param);
                    int id = domain.add(paramI);
                    var edge = PathEdge.createPathEdge(entrySuperblock, id, entrySuperblock, id);
                    logger.debug(String.format("Initialized pathedge  {%s}", edge));
                    seeds.add(edge);
                }
            }
            return seeds;
        }

        @Override
        public IMergeFunction getMergeFunction() {
            return null;
        }
    } // end of MPA Problem

    public TabulationResult<BasicBlockInContext<IExplodedBasicBlock>, CGNode, AccessGraph> analyze() {
        var solver = TabulationSolver.make(new MPAProblem());
        try {
            return solver.solve();
        } catch (CancelException e) {
            throw new IllegalStateException("Cannot solve the constraints");
        }
    }

    public Collection<AccessGraph> solutions() {
        IFDSSolutionCollector<BasicBlockInContext<IExplodedBasicBlock>, CGNode, AccessGraph> results = new IFDSSolutionCollector<>(analyze(), domain);
        return results.collectSolutions();
    }
}
