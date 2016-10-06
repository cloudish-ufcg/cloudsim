package org.cloudbus.cloudsim.preemption.datastore;

import org.cloudbus.cloudsim.preemption.UsageEntry;
import org.cloudbus.cloudsim.preemption.datastore.HostUsageDataStore;
import org.junit.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class HostUsageDataStoreTest {

	private static String databaseFile = "utilizationTest.sqlite3";
	private static String databaseURL = "jdbc:sqlite:" + databaseFile;

	private static double DEFAULT_TIME = 1000.5;
	private static Properties properties;

	private HostUsageDataStore UDataStore;

	private int HostId = 0;
	private UsageEntry entry1, entry2, entry3;
	private List<UsageEntry> entryList1, entryList2, entryList3, entryList4, entryList5;

	private static final double TIME = 5.2;
	private static final double P0_USAGE = 40.558585;
	private static final double P1_USAGE = 21.9875422;
	private static final double P2_USAGE = 5.875646232;
	private static final int P0_VMS = 12;
	private static final int P1_VMS = 1;
	private static final int P2_VMS = 3;
	private static final double AVAILABLE_MIPS = 0.78598;

	@Before
	public void setUp(){

		// creating the dataStore
		properties = new Properties();
		properties.setProperty(HostUsageDataStore.DATABASE_URL_PROP, databaseURL);

		UDataStore = new HostUsageDataStore(properties);

		// creating and populating the entry lists to test
		entryList1 = new ArrayList<>();
		entryList2 = new ArrayList<>();
		entryList3 = new ArrayList<>();
		entryList4 = new ArrayList<>();

		entry1 = new UsageEntry(HostId++, TIME, P0_USAGE, P1_USAGE, P2_USAGE, P0_VMS, P1_VMS, P2_VMS, AVAILABLE_MIPS);
		entry2 = new UsageEntry(HostId++, TIME + 2.3, P0_USAGE + 1, P1_USAGE + 0.5654, P2_USAGE, P0_VMS, P1_VMS, P2_VMS, AVAILABLE_MIPS + 3.8573);
		entry3 = new UsageEntry(HostId++, TIME + 0.4, P0_USAGE - 0.546, P1_USAGE + 1.45, P2_USAGE, P0_VMS, P1_VMS + 1, P2_VMS + 3, AVAILABLE_MIPS);

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
	public void testAddEmptyOrNullUsageEntries() {

		Assert.assertFalse(UDataStore.addUsageEntries(entryList5));
		Assert.assertTrue(UDataStore.addUsageEntries(entryList4));
	}

	@Test
	public void testAddUsageEntries() {

		Assert.assertTrue(UDataStore.addUsageEntries(entryList1));
		Assert.assertEquals(UDataStore.getAllUsageEntries(), entryList1);
	}

	@Test
	public void testAddUsageEntries2() {

		Assert.assertTrue(UDataStore.addUsageEntries(entryList2));
		Assert.assertEquals(UDataStore.getAllUsageEntries(), entryList2);
	}

	@Test
	public void testAddUsageEntries3() {

		Assert.assertTrue(UDataStore.addUsageEntries(entryList3));
		Assert.assertEquals(UDataStore.getAllUsageEntries(), entryList3);
	}

	@Test
	public void testAddUsageEntries4() {

		Assert.assertTrue(UDataStore.addUsageEntries(entryList1));
		Assert.assertTrue(UDataStore.addUsageEntries(entryList2));

		//entryList3 has elements of the same order of the combination between entryList1 and entryList2
		Assert.assertEquals(UDataStore.getAllUsageEntries(), entryList3);
	}

	@Test
	public void testGetEmptyUsageEntry() {
		Assert.assertEquals(UDataStore.getAllUsageEntries(), entryList4);
	}

	@Test
	public void testGetAfterInsertMultipleUsageEntries(){

		Assert.assertTrue(UDataStore.addUsageEntries(entryList1));
		Assert.assertTrue(UDataStore.addUsageEntries(entryList2));

		List<UsageEntry> expectedList = new ArrayList<>();
		expectedList.add(entry1);
		expectedList.add(entry2);
		expectedList.add(entry3);

		Assert.assertEquals(expectedList, UDataStore.getAllUsageEntries());

		// creating new datacenter infos and lists with them to insert in DB
		UsageEntry entry4 = new UsageEntry(HostId++, TIME, P0_USAGE, P1_USAGE, P2_USAGE, P0_VMS, P1_VMS, P2_VMS, AVAILABLE_MIPS);
		UsageEntry entry5 = new UsageEntry(HostId++, TIME, P0_USAGE, P1_USAGE, P2_USAGE, P0_VMS, P1_VMS, P2_VMS, AVAILABLE_MIPS);
		UsageEntry entry6 = new UsageEntry(HostId++, TIME, P0_USAGE, P1_USAGE, P2_USAGE, P0_VMS, P1_VMS, P2_VMS, AVAILABLE_MIPS);
		UsageEntry entry7 = new UsageEntry(HostId++, TIME, P0_USAGE, P1_USAGE, P2_USAGE, P0_VMS, P1_VMS, P2_VMS, AVAILABLE_MIPS);

		List<UsageEntry> entryList6 = new ArrayList<>();
		List<UsageEntry> entryList7 = new ArrayList<>();

		entryList6.add(entry4);
		entryList6.add(entry5);
		entryList6.add(entry6);

		Assert.assertTrue(UDataStore.addUsageEntries(entryList6));
		Assert.assertEquals(6, UDataStore.getAllUsageEntries().size());

		expectedList.add(entry4);
		expectedList.add(entry5);
		expectedList.add(entry6);
		Assert.assertEquals(expectedList, UDataStore.getAllUsageEntries());

		Assert.assertTrue(UDataStore.addUsageEntries(entryList7));
		Assert.assertEquals(6, UDataStore.getAllUsageEntries().size());

		entryList7.add(entry7);
		Assert.assertTrue(UDataStore.addUsageEntries(entryList7));
		expectedList.add(entry7);
		Assert.assertEquals(expectedList, UDataStore.getAllUsageEntries());

		Assert.assertEquals(7, UDataStore.getAllUsageEntries().size());


	}

}
