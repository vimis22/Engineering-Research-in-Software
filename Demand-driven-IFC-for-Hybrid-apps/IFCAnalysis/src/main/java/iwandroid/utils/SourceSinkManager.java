package iwandroid.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class SourceSinkManager {

    private final String file;
    private final List<String> sources = new ArrayList<>();
    private final List<String> sinks = new ArrayList<>();

    protected SourceSinkManager(String file) {
        this.file = file;
    }

    public static SourceSinkManager make(String file) {
        SourceSinkManager manager = new SourceSinkManager(file);
        manager.parseSourcefile();
        System.out.printf("Loaded %d sources from %s %n", manager.sources.size(), file);
        System.out.printf("Loaded %d sinks from %s %n", manager.sinks.size(), file);
        return manager;
    }

    public boolean isEmpty() {
        return sources.isEmpty() && sinks.isEmpty();
    }

    public void parseLine(String line) {
        line = line.trim();
        if (!line.isEmpty() && !line.startsWith("%")) {
            String[] tokens = line.split("->");
            if (tokens.length == 2) {
                String methodSign = tokens[0].trim();
                String type = tokens[1].trim();
                if (isSourceToken(type))
                    sources.add(methodSign);
                if (isSinkToken(type))
                    sinks.add(methodSign);
                if (isInvalidToken(type))
                    System.err.println("Invalid token: " + line);
            } else {
                System.err.println("Invalid line:" + line);
            }
        }
    }

    public void parseSourcefile() {
        if (!isEmpty()) {
            throw new IllegalStateException("Source sink db already available");
        }
        try (Stream<String> lines = Files.lines(Path.of(this.file))) {
            lines.forEach(this::parseLine);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getSources() {
        return this.sources;
    }

    public List<String> getSinks() {
        return this.sinks;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        sources.forEach(source -> string.append("SOURCE<").append(source).append(">\n"));
        sinks.forEach(sink -> string.append("SINK<").append(sink).append(">\n"));
        return string.toString();
    }

    public boolean isSourceMethod(String methodName) {
        return getSources().stream().anyMatch(q -> q.contains(methodName));
    }

    public boolean isSinkMethod(String methodName) {
        return sinks.stream().anyMatch(q -> q.contains(methodName));
    }

    public boolean isSourceToken(String token) {
        return token.equals("_SOURCE_") || token.equals("_BOTH_");
    }

    public boolean isSinkToken(String token) {
        return token.equals("_SINK_") || token.equals("_BOTH_");
    }

    public boolean isInvalidToken(String token) {
        return !isSourceToken(token) && !isSinkToken(token);
    }

}
