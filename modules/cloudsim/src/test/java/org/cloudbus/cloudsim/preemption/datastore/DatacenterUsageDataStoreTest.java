package org.cloudbus.cloudsim.preemption.datastore;


import org.cloudbus.cloudsim.preemption.DatacenterInfo;
import org.cloudbus.cloudsim.preemption.datastore.DatacenterUsageDataStore;
import org.junit.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by jvmafra on 03/10/16.
 */
public class DatacenterUsageDataStoreTest {
    private static String databaseFile = "datacenterTest.sqlite3";
    private static String databaseURL = "jdbc:sqlite:" + databaseFile;

    private static double DEFAULT_TIME = 1000.5;
    private static Properties properties;

    private DatacenterUsageDataStore datacenterDataStore;

    private static final double TIME = 5.2;
    private static final int VMS_RUNNING = 59;
    private static final int VMS_RUNNING_P0 = 40;
    private static final int VMS_RUNNING_P1 = 15;
    private static final int VMS_RUNNING_P2 = 4;
    private static final int VMS_FOR_SCHEDULING = 12;
    private static final int VMS_FOR_SCHEDULING_P0 = 1;
    private static final int VMS_FOR_SCHEDULING_P1 = 3;
    private static final int VMS_FOR_SCHEDULING_P2 = 8;

    private DatacenterInfo info1, info2, info3;
    private List<DatacenterInfo> datacenterInfoList1, datacenterInfoList2, datacenterInfoList3, datacenterInfoList4, datacenterInfoList5;


    @Before
    public void setUp(){

        // creating the dataStore
        properties = new Properties();
        properties.setProperty(DatacenterUsageDataStore.DATABASE_URL_PROP, databaseURL);

        datacenterDataStore = new DatacenterUsageDataStore(properties);

        // creating and populating the datacenterInfo lists to test
        datacenterInfoList1 = new ArrayList<>();
        datacenterInfoList2 = new ArrayList<>();
        datacenterInfoList3 = new ArrayList<>();
        datacenterInfoList4 = new ArrayList<>();

        info1 = new DatacenterInfo(TIME, VMS_RUNNING, VMS_RUNNING_P0, VMS_RUNNING_P1,
                VMS_RUNNING_P2, VMS_FOR_SCHEDULING, VMS_FOR_SCHEDULING_P0, VMS_FOR_SCHEDULING_P1, VMS_FOR_SCHEDULING_P2);
        info2 = new DatacenterInfo(TIME + 5.4, VMS_RUNNING + 27, VMS_RUNNING_P0 + 27, VMS_RUNNING_P1,
                VMS_RUNNING_P2, VMS_FOR_SCHEDULING + 30, VMS_FOR_SCHEDULING_P0, VMS_FOR_SCHEDULING_P1, VMS_FOR_SCHEDULING_P2 + 30);

        info3 = new DatacenterInfo(TIME - 2.3, VMS_RUNNING + 27, VMS_RUNNING_P0, VMS_RUNNING_P1 + 27,
                VMS_RUNNING_P2, VMS_FOR_SCHEDULING - 1, VMS_FOR_SCHEDULING_P0, VMS_FOR_SCHEDULING_P1, VMS_FOR_SCHEDULING_P2 - 1);

        // list 1 with one element
        datacenterInfoList1.add(info1);

        // list 2 with two elements
        datacenterInfoList2.add(info2);
        datacenterInfoList2.add(info3);

        // list 3 with three elements
        datacenterInfoList3.add(info1);
        datacenterInfoList3.add(info2);
        datacenterInfoList3.add(info3);

        // list 4 is empty and list 5 is null
    }

    @After
    public void tearDown() {
        new File(databaseFile).delete();
    }

    @Test
	public void testAddEmptyOrNullDatacenterInfoList() {

		Assert.assertFalse(datacenterDataStore.addDatacenterInfo(datacenterInfoList5));
		Assert.assertTrue(datacenterDataStore.addDatacenterInfo(datacenterInfoList4));
	}

    @Test
	public void testAddDataCenterInfoList() {
		Assert.assertTrue(datacenterDataStore.addDatacenterInfo(datacenterInfoList1));
		Assert.assertEquals(datacenterDataStore.getAllDatacenterInfo(), datacenterInfoList1);
    }

    @Test
	public void testAddDataCenterInfoList2() {
		Assert.assertTrue(datacenterDataStore.addDatacenterInfo(datacenterInfoList2));
		Assert.assertEquals(datacenterDataStore.getAllDatacenterInfo(), datacenterInfoList2);
	}

    @Test
    public void testAddDataCenterInfoList3() {
        Assert.assertTrue(datacenterDataStore.addDatacenterInfo(datacenterInfoList3));
        Assert.assertEquals(datacenterDataStore.getAllDatacenterInfo(), datacenterInfoList3);
    }

    @Test
	public void testAddDataCenterInfoList4() {

		Assert.assertTrue(datacenterDataStore.addDatacenterInfo(datacenterInfoList1));
		Assert.assertTrue(datacenterDataStore.addDatacenterInfo(datacenterInfoList2));

		//datacenterInfoList3 has elements with the same order of the combination between infoList1 and infoList2
		Assert.assertEquals(datacenterDataStore.getAllDatacenterInfo(), datacenterInfoList3);
	}

    @Test
	public void testGetWithEmptyDB() {
		Assert.assertEquals(datacenterDataStore.getAllDatacenterInfo(), new ArrayList<DatacenterInfo>());
	}

	@Test
    public void testGetAfterInsertMultipleDataCenterInfo(){
        Assert.assertTrue(datacenterDataStore.addDatacenterInfo(datacenterInfoList1));
        Assert.assertTrue(datacenterDataStore.addDatacenterInfo(datacenterInfoList2));

        List<DatacenterInfo> expectedList = new ArrayList<>();
        expectedList.add(info1);
        expectedList.add(info2);
        expectedList.add(info3);

        Assert.assertEquals(expectedList, datacenterDataStore.getAllDatacenterInfo());

        // creating new datacenter infos and lists with them to insert in DB
        DatacenterInfo info4 = new DatacenterInfo(TIME + 10.1, VMS_RUNNING + 27, VMS_RUNNING_P0 + 27, VMS_RUNNING_P1,
                VMS_RUNNING_P2, VMS_FOR_SCHEDULING - 1, VMS_FOR_SCHEDULING_P0, VMS_FOR_SCHEDULING_P1, VMS_FOR_SCHEDULING_P2 - 1);

        DatacenterInfo info5 = new DatacenterInfo(TIME + 10.4, VMS_RUNNING + 27, VMS_RUNNING_P0 + 27, VMS_RUNNING_P1,
                VMS_RUNNING_P2, VMS_FOR_SCHEDULING - 1, VMS_FOR_SCHEDULING_P0, VMS_FOR_SCHEDULING_P1, VMS_FOR_SCHEDULING_P2 - 1);

        DatacenterInfo info6 = new DatacenterInfo(TIME + 8.4, VMS_RUNNING + 27, VMS_RUNNING_P0 + 27, VMS_RUNNING_P1,
                VMS_RUNNING_P2, VMS_FOR_SCHEDULING - 1, VMS_FOR_SCHEDULING_P0, VMS_FOR_SCHEDULING_P1, VMS_FOR_SCHEDULING_P2 - 1);

        DatacenterInfo info7 = new DatacenterInfo(TIME + 9.7, VMS_RUNNING + 27, VMS_RUNNING_P0 + 27, VMS_RUNNING_P1,
                VMS_RUNNING_P2, VMS_FOR_SCHEDULING - 1, VMS_FOR_SCHEDULING_P0, VMS_FOR_SCHEDULING_P1, VMS_FOR_SCHEDULING_P2 - 1);

        List<DatacenterInfo> datacenterInfoList6 = new ArrayList<>();
        List<DatacenterInfo> datacenterInfoList7 = new ArrayList<>();

        datacenterInfoList6.add(info4);
        datacenterInfoList6.add(info5);
        datacenterInfoList6.add(info6);


        Assert.assertTrue(datacenterDataStore.addDatacenterInfo(datacenterInfoList6));
        Assert.assertEquals(6, datacenterDataStore.getAllDatacenterInfo().size());
        expectedList.add(info4);
        expectedList.add(info5);
        expectedList.add(info6);
        Assert.assertEquals(expectedList, datacenterDataStore.getAllDatacenterInfo());

        Assert.assertTrue(datacenterDataStore.addDatacenterInfo(datacenterInfoList7));
        Assert.assertEquals(6, datacenterDataStore.getAllDatacenterInfo().size());

        datacenterInfoList7.add(info7);
        Assert.assertTrue(datacenterDataStore.addDatacenterInfo(datacenterInfoList7));
        expectedList.add(info7);
        Assert.assertEquals(expectedList, datacenterDataStore.getAllDatacenterInfo());

        Assert.assertEquals(7, datacenterDataStore.getAllDatacenterInfo().size());

    }
	
	@Test
	public void testDatacenterWithMoreInformation() {
		datacenterDataStore = new DatacenterUsageDataStore(properties);

		info1 = new DatacenterInfo(TIME, VMS_RUNNING, VMS_RUNNING_P0, 10.5, VMS_RUNNING_P1, 5.7,
                VMS_RUNNING_P2, 4.6, VMS_FOR_SCHEDULING, VMS_FOR_SCHEDULING_P0, 3.3, VMS_FOR_SCHEDULING_P1, 4.4, VMS_FOR_SCHEDULING_P2, 6.7);
		
        info2 = new DatacenterInfo(TIME + 5.4, VMS_RUNNING + 27, VMS_RUNNING_P0 + 27, 20.5, VMS_RUNNING_P1, 5.7,
                VMS_RUNNING_P2,  4.6, VMS_FOR_SCHEDULING + 30, VMS_FOR_SCHEDULING_P0, 3.1, VMS_FOR_SCHEDULING_P1, 7.8, VMS_FOR_SCHEDULING_P2 + 30, 5.7);

        List<DatacenterInfo> info = new ArrayList<DatacenterInfo>();
        info.add(info1);
        info.add(info2);
        
        Assert.assertTrue(datacenterDataStore.addDatacenterInfo(info));
        
        List<DatacenterInfo> expectedInfo = new ArrayList<DatacenterInfo>();
        expectedInfo.add(info1);
        expectedInfo.add(info2);
        
        Assert.assertEquals(expectedInfo, datacenterDataStore.getAllDatacenterInfo());
		
	}
}
