package iwandroid.ifc;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FlowPathFact implements Iterable<FlowFact> {

    List<FlowFact> flowfacts = new ArrayList<>();

    protected FlowPathFact() {}

    @Contract(" -> new")
    public static @NotNull FlowPathFact makeEmpty() {
        return new FlowPathFact();
    }

    public static @NotNull FlowPathFact make(@NotNull FlowPathFact other) {
        FlowPathFact newFact = makeEmpty();
        newFact.flowfacts.addAll(other.flowfacts);
        return newFact;
    }

    public static @NotNull FlowPathFact make(FlowFact... facts) {
        FlowPathFact pathfact = new FlowPathFact();
        Arrays.stream(facts).forEach(pathfact::append);
        return pathfact;
    }

    public List<FlowFact> tail() {
        if (flowfacts.size() <= 1) {
            return Collections.emptyList();
        } else {
            int last = flowfacts.size()-1;
            return flowfacts.subList(1, last);
        }
    }

    public void append(FlowFact fact) {
        flowfacts.add(fact);
    }

    public FlowFact last() {
        if (flowfacts.isEmpty()) {
            return null;
        }
        int id = flowfacts.size()-1;
        return flowfacts.get(id);
    }

    public FlowFact init() {
        return flowfacts.isEmpty() ? null : flowfacts.get(0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlowPathFact that = (FlowPathFact) o;
        return flowfacts.equals(that.flowfacts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flowfacts);
    }


    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(";","[","]");
        flowfacts.forEach(accesspath -> joiner.add(accesspath.toString()));
        return joiner.toString();
    }

    @NotNull
    @Override
    public Iterator<FlowFact> iterator() {
        return flowfacts.iterator();
    }

    public IFCLabel computeFlowLabel() {
        // since the lattice is simple l <= h, so we don't need to join the security labels
        boolean secret = flowfacts.stream().anyMatch(flowFact -> flowFact.ifclabel()==IFCLabel.SECRET);
        return (secret) ? IFCLabel.SECRET : IFCLabel.PUBLIC;
    }

    public FlowPathFact reverse() {
        FlowPathFact newFlowpath = FlowPathFact.make(this);
        Collections.reverse(newFlowpath.flowfacts);
        return newFlowpath;
    }
}
