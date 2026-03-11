package iwandroid.dbinterfaces;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;
import java.util.stream.Stream;

public class BridgedMethodDb extends ArrayList<BridgedMethod> implements Iterable<BridgedMethod> {
    private final List<BridgedMethod> bridgedMethods = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger("[IWANDROID]");

    private void add(String appName, String initiatingClass, String bridgedClass, String interfaceObjects, @NotNull String bridgeMethods, String initiatingMethod) {
        for (String bridgeMethod : bridgeMethods.split(".method")) {
            // [0]: .method keyword, [1]: access specifier, [2]: method signature
            if (bridgeMethod.isEmpty()) {
                continue;
            }

            bridgeMethod = bridgeMethod.trim();
            String[] tokens = bridgeMethod.split(" ");

            if (tokens.length < 2) {
                logger.error("Found invalid bridge methods  {}", bridgeMethods);
                continue;
            }
            int lastIndex = tokens.length;
//            String tokenType = tokens[0];
            String accessSpecifier = tokens[0];
            String methodSign = rectifyMethodSignature(tokens[lastIndex-1]);

//            if (!tokenType.equals(".method"))
//                logger.error("Invalid method name");
            BridgedMethod info = new BridgedMethod(appName, initiatingClass, bridgedClass, interfaceObjects, accessSpecifier, methodSign, initiatingMethod);
            bridgedMethods.add(info);
        }
    }

    private String rectifyMethodSignature(String methodName) {
        methodName = methodName.trim();
        int lastIdx = methodName.length()-1;
        if (methodName.charAt(lastIdx) == ';') {
            return methodName.substring(0, lastIdx);
        } else {
            return methodName;
        }
    }

    public static @NotNull BridgedMethodDb load(String dbPath) {
        BridgedMethodDb webViewsList = new BridgedMethodDb();
        String url = "jdbc:sqlite:" + dbPath;
        logger.info("Reading Bridge Interfaces from database {}", dbPath);
        try (Connection connection = DriverManager.getConnection(url)) {
            try(Statement stmt = connection.createStatement()) {
                ResultSet rows = stmt.executeQuery("SELECT * from webview_prime");
                while (rows.next()) {
                    int index = 0;
                    var appName = rows.getString(++index);
                    var initiatingClass = rows.getString(++index);
                    var bridgeClass = rows.getString(++index);
                    var interfaceObject = rows.getString(++index);
                    var bridgeMethods = rows.getString(++index);
                    var initiatingMethod = rows.getString(++index);
                    // Exclude the last ';' from the class names
                    if (!initiatingClass.isEmpty() && !bridgeClass.isEmpty() && !initiatingMethod.isEmpty()) {
                        initiatingClass = initiatingClass.substring(0, initiatingClass.length() - 1);
                        bridgeClass = bridgeClass.substring(0, bridgeClass.length() - 1);
                        webViewsList.add(appName, initiatingClass, bridgeClass, interfaceObject, bridgeMethods, initiatingMethod);
                    } else {
                        logger.warn("Could not found initialingClass and bridge class for {}", appName);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return webViewsList;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        for (var bridgeMethod : bridgedMethods) {
            buffer.append(bridgeMethod);
            buffer.append(System.getProperty("line.separator"));
        }
        return buffer.toString();
    }

    @Override
    public int size() {
        return bridgedMethods.size();
    }

    public List<BridgedMethod> getBridgedMethods() {
        return bridgedMethods;
    }

    @Override
    public BridgedMethod get(int index) {
        return bridgedMethods.get(index);
    }

    @Override
    public Iterator<BridgedMethod> iterator() {
        return bridgedMethods.iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        BridgedMethodDb that = (BridgedMethodDb) o;
        return Objects.equals(bridgedMethods, that.bridgedMethods);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), bridgedMethods);
    }

    public List<BridgedMethod> getBridgeMethodsInClass(String clazz) {
        return this.bridgedMethods.stream().filter(method -> clazz.equals(method.clazz())).toList();
    }

    @Override
    public Stream<BridgedMethod> stream() {
        return bridgedMethods.stream();
    }

    public static @NotNull BridgedMethodDb make(Collection<BridgedMethod> methods) {
        BridgedMethodDb newlist = new BridgedMethodDb();
        newlist.bridgedMethods.addAll(methods);
        return newlist;
    }


    public List<BridgedMethod> selectByAppName(String appName) {
        return bridgedMethods.stream().filter(method -> method.appName().equals(appName)).toList();
    }

    public List<BridgedMethod> selectByBridgeMethodAndInitiatingMethod(String appName, String initiatingMethod) {
        var appNameMethods = selectByAppName(appName);
        return appNameMethods.stream().filter(method -> method.initiatingMethod().equals(initiatingMethod)).toList();
    }
}
