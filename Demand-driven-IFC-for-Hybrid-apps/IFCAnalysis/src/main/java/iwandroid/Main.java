package iwandroid;

import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;
import iwandroid.main.Analyzer;
import iwandroid.utils.Config;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Config.TOOLNAME);


    private static final Options options = new Options();
    private static final Option help = Option.builder().option("h").desc("help").build();
    private static final Option prop = Option.builder().option("p").hasArg().argName("config file (.prop)").desc("Property file for config").build();

    static {
        options.addOption(help);
        options.addOption(prop);
    }

    public static void usage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("iwandroid.Main", options);
    }

    public static void main(String[] args) {
        CommandLine cmd = null;
        try {
            cmd = DefaultParser.builder().build().parse(options, args);
        } catch (ParseException d) {
            usage();
            System.exit(100);
        }

        if (!cmd.hasOption(prop)) {
            logger.error("Invalid property file");
            usage();
            System.exit(100);
        }

        try {
            var configFile = cmd.getOptionValue(prop);
            Config config = Config.fromFile(configFile);
            Analyzer analyzer = new Analyzer(config);
            analyzer.run();
        } catch (IOException | CancelException | WalaException e) {
            e.printStackTrace();
        }
    }

}
