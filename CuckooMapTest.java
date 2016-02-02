import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;


public class CuckooMapTest {
	
	CuckooMap<MyInteger, MyString> map;
	List<MyInteger> intList = new ArrayList<MyInteger>();
	List<MyString> stringList = new ArrayList<MyString>();
	
	public CuckooMapTest() {
		for (int i = 0; i < 100 ; ++i) {
			intList.add(new MyInteger(i));
			stringList.add(new MyString("A" + i));
		}
	}
	
	@Before
	public void setup () {
		map = new CuckooMap<>(10);
	}

	@Test
	public void testSizeEmpty() {
		assertEquals(0, map.size());
	}

	@Test
	public void testIsEmptyEmpty() {
		assertTrue(map.isEmpty());
	}

	@Test
	public void testContainsKeySimple() {
		insertSomeElements(7);
		assertTrue(map.containsKey(intList.get(4)));
		assertFalse(map.containsKey(intList.get(40)));
		
	}

	@Test
	public void testContainsValue() {
		insertSomeElements(7);
		assertTrue(map.containsValue(stringList.get(3)));
	}

	@Test
	public void testGet() {
		insertSomeElements(7);
		//map.dumpTables();
		assertSame(stringList.get(5), map.get(intList.get(5)));
		
	}

	@Test
	public void testPutOneElement() {
		assertSame(stringList.get(0), map.put(intList.get(0),  stringList.get(0)));
		//map.dumpTables();	
	}
	
	@Test
	public void testPutMultipleUniqueNoRegrow() {
		insertSomeElements(8);
		assertEquals(8, map.size());
		//map.dumpTables();
	}
	
	@Test
	public void testPutWithDuplicates() {
		insertSomeElements(17);
		MyString rep = new MyString("New Value");
		map.put(intList.get(10), rep);
		assertEquals(rep, map.get(intList.get(10)));
		map.dumpTables();
	}
	
	@Test
	public void testPutWithRegrow() {
		insertSomeElements(100);
		assertEquals(100, map.size());
		//map.dumpTables();
		
	}

	@Test
	public void testRemoveSimple() {
		insertSomeElements(7);
		assertSame(stringList.get(5), map.get(intList.get(5)));
		assertSame(stringList.get(5), map.remove(intList.get(5)));
		assertNull(map.get(intList.get(5)));
		
	}

	@Test
	public void testPutAllSimple() {
		HashMap<MyInteger, MyString> set = new HashMap<>();
		for (int i = 0; i < 7 ; ++i) {
			set.put(intList.get(i), stringList.get(i));
		}
		map.putAll(set);
		//System.out.println(map.keySet());
		assertTrue(map.containsKey(intList.get(3)));
		assertTrue(map.containsValue(stringList.get(4)));
		
	}

	@Test
	public void testClear() {
		insertSomeElements(7);
		map.clear();
		assertTrue(map.isEmpty());
		assertEquals(0, map.size());
	}

	@Test
	public void testKeySetSimple() {
		insertSomeElements(7);
		Set<MyInteger> s = map.keySet();
		//System.out.println(s);
		for (int i = 0; i < 7; ++i) {
			assertTrue(s.contains(intList.get(i)));
		}
		
	}

	@Test
	public void testValuesSimple() {
		insertSomeElements(7);
		Collection<MyString> col = map.values();
		//System.out.println(col);
		for (int i = 0; i < 7; ++i) {
			assertTrue(col.contains(stringList.get(i)));
		}
	}

	@Test
	public void testEntrySetSimple() {
		insertSomeElements(7);
		Set<Entry<MyInteger, MyString>> s = map.entrySet();
		int index = 0;
		//System.out.println(s);
		for (Entry<MyInteger, MyString> entry : s) {
			assertEquals(intList.get(index), entry.getKey());
			assertEquals(stringList.get(index), entry.getValue());
			++index;
		}
	}
	
	private void insertSomeElements(int count) {
		for (int i = 0; i < count ; ++i) {
			map.put(intList.get(i), stringList.get(i));
		}
	}

}
