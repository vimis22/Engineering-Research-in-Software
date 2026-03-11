package iwandroid.dbinterfaces;

public record BridgedMethod(String appName, String initiatingClass, String clazz, String interfaceObject, String accessSpecifier,
                            String signature, String initiatingMethod) {

    @Override
    public String toString() {
        return "BridgedMethodInfo{" +
                "appName='" + appName + '\'' +
                ", initiatingClass='" + initiatingClass + '\'' +
                ", clazz='" + clazz + '\'' +
                ", interfaceObject='" + interfaceObject + '\'' +
                ", accessSpecifier='" + accessSpecifier + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }

    public boolean matchClassAndMethodSign(String clazz, String signature) {
        return this.clazz.equals(clazz) && this.signature.equals(signature);
    }
}
