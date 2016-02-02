import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

public class CuckooMap<K extends Hashable, V> implements Map<K,V> {
    
	private class Bucket<K extends Comparable<K>, V> implements Map.Entry<K, V>, Comparable<Bucket<K,V>> {
		
		private V val;
		private K key;
		
		public Bucket(K key, V value) {
			this.key = key;
			val = value;
		}
		
		public int compareTo(Bucket<K,V> b) {
			return key.compareTo(b.getKey());
		}
		
		public K getKey() {
			return key;
		}
		
		public V getValue() {
			return val;
		}
		
		public V setValue(V value) {
            val = value;
            return val;
		}
	}
	
	private Bucket[] arr1, arr2;
	private final double LOADFACTOR = .8;
	private int count = 0, countSwaps = 0;
	private int p1, p2, cap;

	
	public CuckooMap(int cap) {
    	this.cap = cap;
    	arr1 = new Bucket[cap];
    	arr2 = new Bucket[cap];
    	Random rand = new Random();
		p1 = rand.nextInt(100) + 1;
		p2 = rand.nextInt(100) + 1;
		while (p2 != p1) {
			p2 = rand.nextInt(100) + 1;
		}
    }
	
	public CuckooMap() {
		this(10);
	}
	
	private double loadfactor() {
        double lf = count/(arr1.length + arr2.length);
        return lf;
	}
	
	public boolean isEmpty() {
		return count == 0;
	}
	
	public int size() {
		return count;
	}
	
	public void clear() {
		cap = 10;
		arr1 = new Bucket[cap];
		arr2 = new Bucket[cap];
		count = 0;
		Random rand = new Random();
		p1 = rand.nextInt(100) + 1;
		p2 = rand.nextInt(100) + 1;
		while (p2 != p1) {
			p2 = rand.nextInt(100) + 1;
		}
	}
	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		if (!m.isEmpty()) {
		    Set<?> set = m.entrySet();
		    for (Object entry : set) {
			    put((K) ((Map.Entry<K, V>) entry).getKey(), (V) ((Map.Entry<K, V>) entry).getValue());
		    }
		}    
	}
	
	@SuppressWarnings("unchecked")
	private V putHelper(Bucket b, int index, Bucket[] table) {
		V val = null;
		if (countSwaps >= table.length){
			regrow();
			countSwaps = 0;
			return put((K) b.getKey(), (V) b.getValue());
		}
		Bucket pushed = table[index];
		if (table[index] != null) {
			if (table[index].compareTo(b) == 0) {
				val = (V) table[index].getValue();
				table[index].setValue(b.getValue());
				return val;
			}
			table[index] = b;
			K key = (K) pushed.getKey();
			if (table[index] == arr2[index]) {
				countSwaps++;
				int index1 = (p1 * key.hash1()) % arr1.length;
				val = putHelper(pushed, index1, arr1);
			} else if (table[index] == arr1[index]){
				countSwaps++;
				int index2 = (p2 * key.hash2()) % arr2.length;
				val = putHelper(pushed, index2, arr2);
			}
		} else if (table[index] == null){
			table[index] = b;
			val = null;
			count++;
		}
		return val;
	}
	
	
	public V put(K key, V value) {
		countSwaps = 0;
		if (value == null || key == null) {
			return null;
		}
		if (loadfactor() >= .8) {
			regrow();
		}
		Bucket item = new Bucket<>(key, value);
		int index1 = (p1 * key.hash1()) % arr1.length;
		V ret1 = putHelper(item, index1, arr1);
		return ret1;
	}
	
	public V get(Object key) {
		if (isEmpty() || key == null) {
			return null;
		}
		int index1 = p1 * ((Hashable) key).hash1() % arr1.length;
		int index2 = p2 * ((Hashable) key).hash2() % arr2.length;
		if (arr1[index1] != null) {
			if (arr1[index1].getKey().compareTo(key) == 0)
				return (V) arr1[index1].getValue();
		} else if (arr2[index2] != null) {
			if (arr2[index2].getKey().compareTo(key) == 0)
		        return (V) arr2[index2].getValue();
		}
		return null;
	}
	
	private void regrow() {
		count = 0;
		Random rand = new Random();
		p1 = rand.nextInt(100) + 1;
		p2 = rand.nextInt(100) + 1;
		while (p2 != p1) {
			p2 = rand.nextInt(100) + 1;
		}
		Bucket[] oldArr1 = arr1;
		Bucket[] oldArr2 = arr2;
		arr1 = new Bucket[arr1.length * 2];
		arr2 = new Bucket[arr2.length * 2];
		for (int i = 0; i < oldArr1.length; i++) {
			if (oldArr1[i] != null) {
			    Bucket b = oldArr1[i];
			    V val = put((K)b.getKey(), (V)b.getValue());
			}
			if (oldArr2[i] != null) {
				Bucket b2 = oldArr2[i];
				V val2 = put((K)b2.getKey(), (V)b2.getValue());
			}    
		}
	}
	
	public V remove(Object key) {
		if (isEmpty() || key == null) {
			return null;
		}
		int index1 = p1 * ((Hashable) key).hash1() % arr1.length;
		int index2 = p2 * ((Hashable) key).hash2() % arr2.length;
		if (arr1[index1] != null) {
		    if (arr1[index1].getKey().compareTo(key) == 0) {
			    V val = (V) arr1[index1].getValue();
			    arr1[index1] = null;
			    count--;
			    return val;
		    }
		} else if (arr2[index2] != null) {
			if (arr2[index2].getKey().compareTo(key) == 0) {
			    V val = (V) arr2[index2].getValue();
			    arr2[index2] = null;
			    count--;
			    return val;
			}	    
		}
		return null;
	}
	
	public boolean containsKey(Object key) {
		if (isEmpty() || key == null) {
			return false;
		}
		int index1 = p1 * ((Hashable) key).hash1() % arr1.length;
		int index2 = p2 * ((Hashable) key).hash2() % arr2.length;
		if (arr1[index1] != null) {
		    if (arr1[index1].getKey().compareTo(key) == 0)
			    return true;
		} else if (arr2[index2] != null){
			if (arr2[index2].getKey().compareTo(key) == 0)
			    return true;
		}
	    return false;
	}

	public boolean containsValue(Object value) {
		if (isEmpty() || value == null) {
			return false;
		}
		for (int i = 0; i < arr1.length; i++) {
			if (arr1[i] != null) {
			    if (arr1[i].getValue().equals(value) == true) 
				    return true;
			} else if(arr2[i] != null) {
				if (arr2[i].getValue().equals(value) == true) 
				    return true;
			}
		}
		return false;
	}
	
	public Set<Map.Entry<K,V>> entrySet() {
		if (isEmpty()) {
			return null;
		}
		Set<Map.Entry<K, V>> treeset = new TreeSet<>();
		for (int i = 0; i < arr1.length; i++) {
			if (arr1[i] != null) {
				treeset.add(arr1[i]);
			}
			if (arr2[i] != null) {
				treeset.add(arr2[i]);
			}
		}
		return treeset;
	}
	
	public Set<K> keySet() {
		if (isEmpty()) {
			return null;
		}
		TreeSet<K> treeset = new TreeSet<>();
		for (int i = 0; i < arr1.length; i++) {
			if (arr1[i] != null) {
				treeset.add((K) arr1[i].getKey());
			}
			if (arr2[i] != null) {
				treeset.add((K) arr2[i].getKey());
			}
		}
		return treeset;
	}
	
	public Collection<V> values() {
		if (isEmpty()) {
			return null;
		}
		ArrayList<V> values = new ArrayList<V>();
		for (int i = 0; i < arr1.length; i++) {
			if (arr1[i] != null) {
				values.add((V) arr1[i].getValue());
			}
			if (arr2[i] != null) {
				values.add((V) arr2[i].getValue());
			}
		}
		return values;
	}
}
