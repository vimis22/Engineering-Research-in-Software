import iwandroid.dbinterfaces.BridgedMethodDb;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class BridgeMethodTest {
    BridgedMethodDb bridgeMethodsList;

    String webViewDatabase = "/Users/jyotiprakash/Research/HybridAppsIfcAnalysis/Demand-driven-IFC-for-Hybrid-apps/Database/Intent-new.sqlite";


    @BeforeEach
    void setup() {
        bridgeMethodsList = BridgedMethodDb.load(webViewDatabase);
    }

    @Test
    void testCorrectWebViewList() {
        System.out.println(bridgeMethodsList);
        assertNotEquals(0, bridgeMethodsList.size());
    }


}
