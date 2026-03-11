package iwandroid.ifc;

import com.ibm.wala.dataflow.IFDS.IUnaryFlowFunction;
import com.ibm.wala.util.intset.IntIterator;
import com.ibm.wala.util.intset.IntSet;
import com.ibm.wala.util.intset.MutableSparseIntSet;
import org.jetbrains.annotations.Contract;

public class ComposeFunction {

    public static IUnaryFlowFunction make(IUnaryFlowFunction f1, IUnaryFlowFunction f2) {
        if (f1 == null) {
            return f2;
        } else if (f2 == null) {
            return f1;
        } else {
            return d1 -> {
                MutableSparseIntSet result = MutableSparseIntSet.makeEmpty();
                IntSet in = f1.getTargets(d1);
                IntIterator iterator = in.intIterator();
                while (iterator.hasNext()) {
                    int f11 = iterator.next();
                    result.addAll(f2.getTargets(f11));
                }
                return result;
            };
        }
    }

    private ComposeFunction() {
        throw new AssertionError("unreachable");
    }
}
