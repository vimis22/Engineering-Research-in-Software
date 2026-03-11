package iwandroid.ifc;

import java.util.ArrayList;
import java.util.List;

public class JSCoreLibrary {

    private final List<String> coreLibs = new ArrayList<>();

    private JSCoreLibrary() {
        coreLibs.add("Lprologue.js");
    }

    public static boolean exists(String classname) {
        return new JSCoreLibrary().coreLibs.stream().anyMatch(q -> q.contains(classname));
    }
}
