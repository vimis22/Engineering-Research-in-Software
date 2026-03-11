package iwandroid.ifc;

import com.ibm.wala.dataflow.IFDS.ISupergraph;

public abstract class AbstractIfcAnalysis<T, P, F> {

    protected final ISupergraph<T, P> supergraph;
    protected final P bridgeNode;
    protected final F domain;

    protected AbstractIfcAnalysis(P bridgeNode, F domain, ISupergraph<T, P> supergraph) {
        this.supergraph = supergraph;
        this.bridgeNode = bridgeNode;
        this.domain = domain;
    }
}
