import com.ibm.wala.ipa.cha.ClassHierarchyException;
import iwandroid.frontend.JavascriptAnalysis;
import org.junit.jupiter.api.Test;

public class TestJavaScriptAnalysis {
    private final String jsFile = "/Users/jyotiprakash/Research/ModularIfcAnalysis/Demand-driven-IFC-for-Hybrid-apps/src/test/resources/sample.js";

    JavascriptAnalysis analysis = new JavascriptAnalysis(jsFile);

    public TestJavaScriptAnalysis() throws ClassHierarchyException {
    }

    @Test
    void testSampleJsIR() throws ClassHierarchyException {
        var methods = analysis.getMethods();
        if ((methods.size() <= 0)) throw new AssertionError();
    }

}
