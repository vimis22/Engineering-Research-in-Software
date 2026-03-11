package iwandroid.ifc;

import com.ibm.wala.dataflow.IFDS.IUnaryFlowFunction;
import com.ibm.wala.util.intset.MutableSparseIntSet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class EmptyFunction {
    @Contract(pure = true)
    public static @NotNull IUnaryFlowFunction empty() {
        return d1 -> MutableSparseIntSet.makeEmpty();
    }

    private EmptyFunction() {
        throw new AssertionError("Unreachable");
    }
}
