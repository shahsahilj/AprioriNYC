import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


public class SortMap {
	public static TreeMap<String,Double> SortByValue(HashMap<String, Double> map){
		ValueComparator vc =  new ValueComparator(map);
		TreeMap<String,Double> sortedMap = new TreeMap<String,Double>(vc);
		sortedMap.putAll(map);
		return sortedMap;
	}
	

}
class ValueComparator implements Comparator<String> {
	 
    Map<String, Double> map;
 
    public ValueComparator(Map<String, Double> base) {
        this.map = base;
    }
 
    public int compare(String a, String b) {
        if (map.get(a) >= map.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys 
    }
}
