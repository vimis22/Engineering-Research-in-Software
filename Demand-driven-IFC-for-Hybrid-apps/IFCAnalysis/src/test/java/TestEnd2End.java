import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;
import iwandroid.main.Analyzer;
import iwandroid.utils.Config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class TestEnd2End {


    String benchmarkRoot = "/Users/jyotiprakash/Research/HybridAppsIfcAnalysis/Demand-driven-IFC-for-Hybrid-apps/Microbenchmarks";
    String androidJarPAth = "/Users/jyotiprakash/Library/Android/sdk/platforms/android-29/android.jar";
    String dbFile = "/Users/jyotiprakash/Research/HybridAppsIfcAnalysis/Demand-driven-IFC-for-Hybrid-apps/Microbenchmarks/Database/Intent.sqlite";
    int apiLevel = 29;
    String susiFile = "/Users/jyotiprakash/Research/HybridAppsIfcAnalysis/Demand-driven-IFC-for-Hybrid-apps/IFCAnalysis/resource/SourcesAndSinks.txt";
    @BeforeEach
    void setup() {

    }

    Config makeBasicConfig() {
        Config config = Config.emptyConfig();
        config.setApilevel(apiLevel);
        config.setAndroidJarpath(androidJarPAth);
        config.setDatabase(dbFile);
        config.setSusiFile(susiFile);
        return config;
    }

    public String getAppPath(String appName) {
        String appsRoot = Paths.get(benchmarkRoot, "apps").toAbsolutePath().toString();
        Path p = Paths.get(appsRoot, appName);
        assert Files.exists(p);
        return p.toAbsolutePath().toString();
    }

    public Path getIntermediateFile(String... path) {
        String intermediateRoot = Paths.get(benchmarkRoot, "output", "intermediate").toAbsolutePath().toString();
        Path p = Paths.get(intermediateRoot, path);
        assert Files.exists(p);
        return p.toAbsolutePath();
    }

    @Test
    void helloCordova() throws WalaException, IOException, CancelException {
        Config config = makeBasicConfig();
        config.setApkFile(getAppPath("HelloCordova.apk"));
        config.setAppName("HelloCordova");

        Path jsFile = getIntermediateFile("HelloCordova", "assets", "www", "cordova.js");
        config.setJsFilepath(jsFile.toString());
        config.setJsDir(jsFile.toFile().getParent());
        Analyzer analyzer = new Analyzer(config);
        analyzer.run();
    }

    @Test
    void helloScript() throws WalaException, IOException, CancelException {
        Config config = makeBasicConfig();
        config.setApkFile(getAppPath("HelloScript.apk"));
        config.setAppName("HelloScript");
            Path jsDir = getIntermediateFile("HelloScript", "assets", "www", "js" );
        config.setJsDir(jsDir.toString());
        Path jsFile = Paths.get("contact.js");
        config.setJsFilepath(jsFile.toString());
        Analyzer analyzer = new Analyzer(config);
        analyzer.run();
    }

    @Test
    void JSUpdateCaseD() throws WalaException, IOException, CancelException {
        Config config = makeBasicConfig();
        config.setApkFile(getAppPath("JSUpdateCaseD.apk"));
        config.setAppName("JSUpdateCaseD");
        Path jsDir = getIntermediateFile("JSUpdateCaseD", "assets" );
        config.setJsDir(jsDir.toString());
        Path jsFile = Paths.get("script.js");
        config.setJsFilepath(jsFile.toString());
        Analyzer analyzer = new Analyzer(config);
        analyzer.run();
    }

    @Test
    void case1() throws WalaException, IOException, CancelException {
        Config config = Config.fromFile("/Users/jyotiprakash/Research/HybridAppsIfcAnalysis/Demand-driven-IFC-for-Hybrid-apps/IFCAnalysis/dataUpload/partA/config/au.com.wallaceit.reddinator_68.prop");
        Analyzer analyzer = new Analyzer(config);
        analyzer.run();
    }
}
