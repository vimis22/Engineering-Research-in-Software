package iwandroid.ifc;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AndroidLibraryList {

    public static class LibraryFunc {
        public String className;
        public String functionName;

        public LibraryFunc(String className, String functionName) {
            this.className = className;
            this.functionName = functionName;
        }

        public static LibraryFunc make(String className, String functionName) {
            return new LibraryFunc(className, functionName);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LibraryFunc that = (LibraryFunc) o;
            return className.equals(that.className) && functionName.equals(that.functionName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(className, functionName);
        }
    }

    protected List<LibraryFunc> libraries;
    protected List<String> libraryPatterns;

    public AndroidLibraryList() {
        libraries = new ArrayList<>();
        libraryPatterns = new ArrayList<>();
        populate();
    }

    public void populate() {
        libraries.add(LibraryFunc.make("Landroid/os/Handler", "sendMessage"));
        libraries.add(LibraryFunc.make("Landroid/os/Handler", "obtainMessage"));
        libraries.add(LibraryFunc.make("Landroid/support/v7/app/ActionBarActivity", "onCreate"));
        libraryPatterns.add("Landroid");
        libraryPatterns.add("Ljava");
        libraryPatterns.add("android");
    }

    public boolean contains(String className, String method) {
        return libraryPatterns.stream().anyMatch(className::startsWith)
                || libraries.contains(LibraryFunc.make(className, method));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AndroidLibraryList that = (AndroidLibraryList) o;
        return libraries.equals(that.libraries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(libraries);
    }
}
