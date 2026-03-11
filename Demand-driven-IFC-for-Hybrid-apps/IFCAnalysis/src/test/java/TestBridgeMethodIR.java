import com.ibm.wala.ipa.cha.ClassHierarchyException;
import iwandroid.frontend.AndroidAnalysis;
import iwandroid.frontend.BridgeMethodIR;
import iwandroid.dbinterfaces.BridgedMethodDb;
import iwandroid.utils.Config;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TestBridgeMethodIR {

    AndroidAnalysis analysis;
    String apkfile =  System.getProperty("user.dir") + "/HybridAppAnalysis/input/app-debug.apk";
    String androidJar = System.getenv("ANDROID_SDK_ROOT")  + "/platforms/android-29/android.jar";
    String database = System.getProperty("user.dir") + "/src/test/resources/Intent.sqlite";
    BridgedMethodDb bridgedMethods;
    Config config;

    @BeforeEach
    void setup() throws ClassHierarchyException, IOException {
        config = Config.emptyConfig();
        config.setAndroidJarpath(androidJar);
        config.setApkFile(apkfile);
        analysis = new AndroidAnalysis(androidJar, apkfile);
        bridgedMethods = BridgedMethodDb.load(database);
    }

    @Test
    void testGenerateBridgeMethod0() {
        var bridgeMethod = bridgedMethods.get(0);
        var ir = new BridgeMethodIR(bridgeMethod, analysis.getCha(), analysis.getCache()).makeIR();
        System.out.println(ir);
        Assertions.assertNotEquals(null, ir);
    }

    @Test
    void testGeneratedBridgeMethod1() {
        var bridgeMethod = bridgedMethods.get(1);
        var ir = new BridgeMethodIR(bridgeMethod, analysis.getCha(), analysis.getCache()).makeIR();
        System.out.println(ir);
        Assertions.assertNotEquals(null, ir);
    }
}
