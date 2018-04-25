import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

// a comparator that compares Strings
class ValueComparator implements Comparator {
    Map map;

    public ValueComparator(Map map) {
        this.map = map;
    }

    public int compare(Object keyA, Object keyB) {
        Pair<Double,Integer> PvalueA=(Pair<Double,Integer>)map.get(keyA);
        Pair<Double,Integer> PvalueB=(Pair<Double,Integer>)map.get(keyB);
        Comparable valueA = (Comparable) PvalueA.getLeft();
        Comparable valueB = (Comparable) PvalueB.getLeft();
        return valueB.compareTo(valueA);
    }
}