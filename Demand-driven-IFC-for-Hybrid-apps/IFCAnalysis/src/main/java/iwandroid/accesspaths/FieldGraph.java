package iwandroid.accesspaths;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.ibm.wala.classLoader.IField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class FieldGraph implements Cloneable {

  private int head;

  private int tail;

  private MutableGraph<Integer> fieldGraph = GraphBuilder.directed().build();

  private static final Logger logger = LoggerFactory.getLogger(FieldGraph.class.getName());

  private List<IField> fieldtoIntMap = new LinkedList<>();

  public FieldGraph(IField fields[]) {
    assert fields != null && fields.length > 0;

    for (int i = 0; i < fields.length; ++i) {
      fieldGraph.addNode(fieldToInt(fields[i]));
    }

    head = fieldToInt(fields[0]);
    IField from = fields[0];
    for (int i = 1; i < fields.length; ++i) {
      IField to = fields[i];
      fieldGraph.putEdge(fieldToInt(from), fieldToInt(to));
      from = to;
    }
    tail = fieldToInt(fields[fields.length - 1]);
  }

  public static FieldGraph of(IField... fields) {
    return new FieldGraph(fields);
  }

  private int fieldToInt(IField field) {
    if (fieldtoIntMap.contains(field)) {
      return fieldtoIntMap.indexOf(field);
    } else {
      fieldtoIntMap.add(field);
      return fieldtoIntMap.size() - 1;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    FieldGraph that = (FieldGraph) o;
    return head == that.head
        && tail == that.tail
        && Objects.equals(fieldGraph, that.fieldGraph)
        && Objects.equals(fieldtoIntMap, that.fieldtoIntMap);
  }

  @Override
  public int hashCode() {
    return Objects.hash(head, tail, fieldGraph, fieldtoIntMap);
  }

  public FieldGraph clone() throws CloneNotSupportedException {
    try {
      FieldGraph cloned = (FieldGraph) super.clone();
      cloned.fieldGraph = this.fieldGraph;
      cloned.head = this.head;
      cloned.tail = this.tail;
      cloned.fieldtoIntMap = this.fieldtoIntMap;
      return cloned;
    } catch (CloneNotSupportedException e) {
      logger.error("Cannot clone field graph" + e.getMessage());
      e.printStackTrace();
      return null;
    }
  }

  private boolean inALoop(int src) {
    // checks if the src is reachable from itself
    Queue<Integer> q = new LinkedList<>();
    q.add(src);
    while (!q.isEmpty()) {
      var successors = fieldGraph.successors(q.remove());
      if (successors.contains(q)) return true;
      q.addAll(successors);
    }
    return false;
  }

  public Set<FieldGraph> removeHead()  {
    // get the successors of head. For each successor of head, create a new field graph and return
    boolean headInLoop = inALoop(head);
    Set<FieldGraph> graphs = new HashSet<>();
    for (int successor : fieldGraph.successors(head)) {
      try {
        FieldGraph cloned = clone();
        if (!headInLoop) {
          cloned.fieldGraph.removeNode(head);
        }
        cloned.head = successor;
        graphs.add(cloned);
      } catch (CloneNotSupportedException e) {
        logger.error("Cannot clone field [" + e.getMessage() + "]");
        e.printStackTrace();
      }
    }
    return graphs;
  }

  /**
   * Prepend the field f to a field graph
   *
   * @param f
   * @return
   */
  public FieldGraph prependHead(IField f) {
    try {
      FieldGraph cloned = clone();
      int fieldId = cloned.fieldToInt(f);
      if (fieldGraph.nodes().isEmpty()) {
        fieldGraph.addNode(fieldId);
      } else {
        cloned.fieldGraph.putEdge(fieldId, head);
      }
      cloned.head = fieldId;
      return cloned;
    } catch (CloneNotSupportedException e) {
      logger.error("Cannot clone FieldGraph " + e.getMessage());
      return null;
    }
  }

  public boolean isHead(IField f) {
    return fieldToInt(f) == head;
  }

  public boolean isTail(IField f) {
    return fieldToInt(f) == tail;
  }

  @Override
  public String toString() {
//    StringJoiner result = new StringJoiner(";","fields[","]");
    StringBuilder str = new StringBuilder("FieldGraph[");
    fieldGraph.edges().forEach(str::append);
    str.append("]");
    return str.toString();
  }
}
