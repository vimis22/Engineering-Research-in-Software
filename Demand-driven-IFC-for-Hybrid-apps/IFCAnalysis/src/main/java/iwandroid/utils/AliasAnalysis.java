package iwandroid.utils;

import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;

public class AliasAnalysis {
    public static void computeAliases(PointerAnalysis<InstanceKey> pa, PointerKey key) {
        var instances = pa.getPointsToSet(key);

    }
}
