package iwandroid.accesspaths;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.CancelException;
import iwandroid.frontend.AndroidAnalysis;
import iwandroid.dbinterfaces.BridgedMethod;
import iwandroid.dbinterfaces.BridgedMethodDb;
import iwandroid.modifiedpaths.ModifiedPathAnalysis;
import iwandroid.utils.Config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TestMPAAnalysis {

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
    void generateMPAForBridgeMethod() throws CancelException {
        var bridgeMethod = bridgedMethods.get(0);
        var callgraph = analysis.callgraph();
        var method = analysis.nodeForMethod(getMethod(bridgeMethod));
        assert method.isPresent() : "Failed to find method %s in %s".formatted(bridgeMethod.signature(), bridgeMethod.clazz());

        System.out.println(method.get().getIR());
        var mpanalysis = new ModifiedPathAnalysis(callgraph, method.get());
        var result = mpanalysis.analyze();
    }

    private IMethod getMethod(BridgedMethod bridgeMethod) {
        return analysis.lookUpMethod(bridgeMethod.clazz(), bridgeMethod.signature());
    }

    @Test
    void testGeneratedBridgeMethod1() throws CancelException {
        var bridgeMethod = bridgedMethods.get(1);
        var callgraph = analysis.callgraph();
        var method = analysis.nodeForMethod(getMethod(bridgeMethod));
        assert method.isPresent() : "Failed to find method %s in %s".formatted(bridgeMethod.signature(), bridgeMethod.clazz());

        System.out.println(method.get().getIR());
        var mpanalysis = new ModifiedPathAnalysis(callgraph, method.get());
        var result = mpanalysis.analyze();
    }
}