package iwandroid.ifc;

import com.ibm.wala.dataflow.IFDS.IUnaryFlowFunction;
import com.ibm.wala.util.intset.MutableIntSet;
import com.ibm.wala.util.intset.MutableSparseIntSet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class IdentityFunction {
    @Contract(pure = true)
    public static @NotNull IUnaryFlowFunction identity() {
        return d1 -> {
            MutableIntSet results = MutableSparseIntSet.makeEmpty();
            results.add(d1);
            return results;
        };
    }

    private IdentityFunction() {
        throw new AssertionError("Unreachable");
    }
}
