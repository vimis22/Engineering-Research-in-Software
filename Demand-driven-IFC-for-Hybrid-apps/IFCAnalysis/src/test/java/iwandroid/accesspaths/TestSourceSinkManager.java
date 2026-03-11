package iwandroid.accesspaths;

import iwandroid.utils.SourceSinkManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestSourceSinkManager {
    SourceSinkManager ssm;
    @BeforeEach
    public void setup() {
        ssm = SourceSinkManager.make("/Users/jyotiprakash/Research/HybridAppsIfcAnalysis/Demand-driven-IFC-for-Hybrid-apps/IFCAnalysis/src/main/resources/SourcesAndSinks.txt");
    }

    @Test
    public void test() {
        System.out.println(ssm.toString());
    }
}
