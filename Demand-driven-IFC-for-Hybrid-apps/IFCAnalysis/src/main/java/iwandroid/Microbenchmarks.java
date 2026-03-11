package iwandroid;

import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;
import iwandroid.main.Analyzer;
import iwandroid.utils.Config;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

public class Microbenchmarks {


    private static final Logger logger = LoggerFactory.getLogger(Config.TOOLNAME);

    private final String benchmarkRoot;
    private final String androidJarPath;
    private final String dbPath;
    private final String susiFile;
    private final String intermediateDir;

    Microbenchmarks(String benchmarkRoot, String androidJarPath, String sourceSinkFile, String dbPath, String intermediateDir) {
        this.benchmarkRoot = benchmarkRoot;
        this.androidJarPath = androidJarPath;
        this.dbPath = dbPath;
        this.susiFile = sourceSinkFile;
        this.intermediateDir = intermediateDir;
    }

    private void runBenchmark(Config config) {
        Analyzer analyzer = new Analyzer(config);
        try {
            analyzer.run();
        } catch (WalaException | IOException | CancelException e) {
            e.printStackTrace();
        }
    }

    public void analyze() {
        List<String> benchmarkFiles = List.of(
                "HelloCordova.apk",
                "HelloHybrid.apk",
                "HelloScript.apk",
                "HelloScript22.apk",
                "HelloScript_simple.apk",
                "HelloScript_test-2.apk",
                "HybridAPIArgNum.apk",
                "JSUpdateCaseD.apk",
                "JsUpdateCaseE.apk",
                "JsUpdateCaseF.apk",
                "JsUpdateCaseG.apk",
                "NormalAliasFlowTest.apk",
                "NormalAliasFlowTest_objfield1.apk",
                "NormalAliasFlowTest_objfield_false.apk",
                "strongUpdate.apk",
                "strongUpdatecaseA.apk",
                "strongUpdatecaseB.apk",
                "strongUpdatecaseC.apk"
        );

        for (var benchmark : benchmarkFiles) {
            Config config = Config.emptyConfig();
            config.setApkFile(Path.of(benchmarkRoot, benchmark).toAbsolutePath().toString());
            config.setAppName(benchmark.replace(".apk", ""));
            config.setDatabase(dbPath);
            config.setSusiFile(susiFile);
            config.setAndroidJarpath(androidJarPath);
            config.setApilevel(27);

            // find the location for javascript files
            var jsFiles = searchJsFile(benchmark);
            if (jsFiles.isEmpty()) {
                logger.error("TERMINATING ANALYSIS OF {}. CANNOT FIND JAVASCRIPT FILE  OR MULTIPLE FILES FOUND", benchmark);
                continue;
            }
            // we assume that there is only one JS file
            var file = jsFiles.get(0);
            String jsDir = file.getParent();
            String fileName = file.getAbsolutePath();
            config.setJsDir(jsDir);
            config.setJsFilepath(fileName);
            runBenchmark(config);
        }
    }

    private List<File> searchJsFile(String benchmark) {
        Path directory = Paths.get(intermediateDir, benchmark);
        List<File> result = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(directory, 2)) {
            paths.filter(path -> path.endsWith(".js")).forEach(path -> {
                File f = new File(path.toString());
                result.add(f);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    public static void main(String[] args) {

        final Option configProp = Option.builder().option("p").hasArg().desc("").build();
        final Options options = new Options();
        options.addOption(configProp);

        try {
            var cmdLine = DefaultParser.builder().build().parse(options, args);
            String propertyFile = cmdLine.getOptionValue(configProp);
            run(propertyFile);
        } catch (ParseException | IOException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("BenchmarkAnalyzer", options);
        }
    }

    private static void run(String propertyFile) throws IOException {
        try(var filestream = new FileInputStream(propertyFile)) {
            Properties prop = new Properties();
            prop.load(filestream);
            String benchmarkRoot = prop.getProperty("benchmarkRoot");
            String androidJarPath = prop.getProperty("androidJarPath");
            String dbPath = prop.getProperty("dbPath");
            String susiFile = prop.getProperty("susiFile");
            String intermediateDir = prop.getProperty("intermediateDir");
            var analysis = new Microbenchmarks(benchmarkRoot, androidJarPath, susiFile, dbPath, intermediateDir);
            analysis.analyze();
        } catch (FileNotFoundException f) {
            logger.error("Unable to find file {}", propertyFile);
        }
    }

}
