package iwandroid.accesspaths;

import iwandroid.frontend.AndroidAnalysis;
import iwandroid.dbinterfaces.BridgedMethod;
import iwandroid.utils.SourceSinkManager;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.List;

public class TestBackwardsFlowFunction {
    private String app = "/Users/jyotiprakash/Research/HybridAppsIfcAnalysis/Demand-driven-IFC-for-Hybrid-apps/apps/HelloHybrid.apk";
    private List<BridgedMethod> methods = new ArrayList<>();
    String apkfile = "/Users/jyotiprakash/Research/HybridAppsIfcAnalysis/Demand-driven-IFC-for-Hybrid-apps/apps/HelloHybrid.apk";
    String androidJar = "/Users/jyotiprakash/Library/Android/sdk/platforms/android-29/android.jar";
    AndroidAnalysis analysis = null;
    private String sourceSinkFile = "src/main/resources/SourcesAndSinks.txt";
    private SourceSinkManager manager;


    @BeforeEach
    public void setup() {

    }
}
