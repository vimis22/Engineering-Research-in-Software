package iwandroid.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    private String apkFile;
    private String appName;
    private int apilevel = -1;
    private String androidJarpath;
    private String database;
    private String jsDir;
    private String jsFilepath;
    private String susiFile;

    protected Config() {}

    @Contract(value = " -> new", pure = true)
    public static @NotNull Config emptyConfig() {
        return new Config();
    }

    public String getApkFile() {
        return apkFile;
    }

    public void setApkFile(String apkFile) {
        this.apkFile = apkFile;
    }

    public int getApilevel() {
        return apilevel;
    }

    public void setApilevel(int apilevel) {
        this.apilevel = apilevel;
    }

    public String getAndroidJarpath() {
        return androidJarpath;
    }

    public void setAndroidJarpath(String androidJarpath) {
        this.androidJarpath = androidJarpath;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getSusiFile() {
        return susiFile;
    }

    public void setSusiFile(String susiFile) {
        this.susiFile = susiFile;
    }

    public String getJsDir() {
        return jsDir;
    }

    public void setJsDir(String jsDir) {
        this.jsDir = jsDir;
    }

    public String getJsFilepath() {
        return jsFilepath;
    }

    public void setJsFilepath(String jsFilepath) {
        this.jsFilepath = jsFilepath;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public static Config makeConfig(Properties prop) {
        Config config = Config.emptyConfig();
        config.setAppName(prop.getProperty("appName"));

        if (prop.containsKey("apiLevel")) {
            config.setApilevel(Integer.parseInt(prop.getProperty("apiLevel")));
        } else {
            config.setApilevel(27);
        }

        config.setAndroidJarpath(prop.getProperty("androidJarPath"));
        config.setApkFile(prop.getProperty("apkFile"));
        config.setDatabase(prop.getProperty("dbPath"));
        config.setJsFilepath(prop.getProperty("jsFilePath"));
        config.setJsDir(prop.getProperty("jsDir"));
        config.setSusiFile(prop.getProperty("susiFile"));
        return config;
    }

    public static Config fromFile(String file) {
        try(var inputstream = new FileInputStream(file)) {
            Properties prop = new Properties();
            prop.load(inputstream);
            return makeConfig(prop);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String toString() {
        return "Config{" +
                "apkFile='" + apkFile + '\'' +
                ", appName='" + appName + '\'' +
                ", apilevel=" + apilevel +
                ", androidJarpath='" + androidJarpath + '\'' +
                ", database='" + database + '\'' +
                ", jsDir='" + jsDir + '\'' +
                ", jsFilepath='" + jsFilepath + '\'' +
                ", susiFile='" + susiFile + '\'' +
                '}';
    }

    public static final String TOOLNAME = "IWANDROID";
}
