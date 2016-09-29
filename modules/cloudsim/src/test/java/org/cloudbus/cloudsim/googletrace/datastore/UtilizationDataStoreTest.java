package org.cloudbus.cloudsim.googletrace.datastore;

import static org.junit.Assert.fail;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.googletrace.HostUtilizationEntry;
import org.junit.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class UtilizationDataStoreTest {

	private static String databaseFile = "utilizationTest.sqlite3";
	private static String databaseURL = "jdbc:sqlite:" + databaseFile;

	private static double DEFAULT_TIME = 1000.5;
	private static Properties properties;

	private UtilizationDataStore UDataStore;

	private int HostId = 0;
	private HostUtilizationEntry entry1, entry2, entry3;
	private List<HostUtilizationEntry> entryList1, entryList2, entryList3, entryList4, entryList5;

	@Before
	public void setUp(){

		// creating the dataStore
		properties = new Properties();
		properties.setProperty(UtilizationDataStore.DATABASE_URL_PROP, databaseURL);

		UDataStore = new UtilizationDataStore(properties);

		// creating and populating the entry lists to test
		entryList1 = new ArrayList<>();
		entryList2 = new ArrayList<>();
		entryList3 = new ArrayList<>();
		entryList4 = new ArrayList<>();

		entry1 = new HostUtilizationEntry(HostId++, DEFAULT_TIME, 0.9876);
		entry2 = new HostUtilizationEntry(HostId++, DEFAULT_TIME + 1, 0.7654);
		entry3 = new HostUtilizationEntry(HostId++, DEFAULT_TIME + 2, 0.6543);

		entryList1.add(entry1);

		entryList2.add(entry2);
		entryList2.add(entry3);

		entryList3.add(entry1);
		entryList3.add(entry2);
		entryList3.add(entry3);
	}

	@After
	public void tearDown() {
		new File(databaseFile).delete();
	}

	@Test
	public void testAddEmptyOrNullUtilizationEntries() {

		Assert.assertFalse(UDataStore.addUtilizationEntries(entryList5));
		Assert.assertTrue(UDataStore.addUtilizationEntries(entryList4));
	}

	@Test
	public void testAddUtilizationEntries() {

		Assert.assertTrue(UDataStore.addUtilizationEntries(entryList1));
		Assert.assertEquals(UDataStore.getAllUtilizationEntries(), entryList1);
	}

	@Test
	public void testAddUtilizationEntries2() {

		Assert.assertTrue(UDataStore.addUtilizationEntries(entryList2));
		Assert.assertEquals(UDataStore.getAllUtilizationEntries(), entryList2);
	}

	@Test
	public void testAddUtilizationEntries3() {

		Assert.assertTrue(UDataStore.addUtilizationEntries(entryList3));
		Assert.assertEquals(UDataStore.getAllUtilizationEntries(), entryList3);
	}

	@Test
	public void testAddUtilizationEntries4() {

		Assert.assertTrue(UDataStore.addUtilizationEntries(entryList1));
		Assert.assertTrue(UDataStore.addUtilizationEntries(entryList2));

		//entryList3 has elements of the same order of the combination fo entryList1 and entryList2
		Assert.assertEquals(UDataStore.getAllUtilizationEntries(), entryList3);
	}

	@Test
	public void testGetEmptyUtilizationEntry() {
		Assert.assertEquals(UDataStore.getAllUtilizationEntries(), entryList4);
	}
}
