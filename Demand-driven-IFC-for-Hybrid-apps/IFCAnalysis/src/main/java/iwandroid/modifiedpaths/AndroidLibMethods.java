package iwandroid.modifiedpaths;

import com.ibm.wala.dataflow.IFDS.IUnaryFlowFunction;

import java.util.ArrayList;
import java.util.List;

public class AndroidLibMethods {
    private static class AndroidLibMethod {
        public String clazz;
        public String method;

        public AndroidLibMethod(String clazz, String method) {
            this.clazz = clazz;
            this.method = method;
        }
    }

    private static List<AndroidLibMethod> methods = new ArrayList<>();

//    static {
//        methods.add(new AndroidLibMethod("Landroid/util/Log", ))
//    }
}
