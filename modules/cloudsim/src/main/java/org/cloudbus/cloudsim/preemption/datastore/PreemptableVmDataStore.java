package org.cloudbus.cloudsim.preemption.datastore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.preemption.PreemptableVm;

public class PreemptableVmDataStore extends DataStore {
	
	private static final int WAITING = 0;
	private static final int RUNNING = 1;

	public static final String CHECKPOINT_DIR_PROP = "checkpoint_dir";
	private static final String VMS_TABLE_NAME = "preemptivevms";
	private double time;
	
	public PreemptableVmDataStore(Properties properties, double time) {
		super("jdbc:sqlite:" + properties.getProperty(CHECKPOINT_DIR_PROP)
				+ "vms-" + properties.getProperty("number_of_hosts") + "-hosts-"
				+ String.valueOf(time));

		this.time = time;
		Statement statement = null;
		Connection connection = null;
		try {
			Log.printLine("vms database url=" + getDatabaseURL());

			Class.forName(DATASTORE_SQLITE_DRIVER);

			connection = getConnection();
			statement = connection.createStatement();
			statement
					.execute("CREATE TABLE IF NOT EXISTS preemptivevms("
							+ "vmId INTEGER NOT NULL, "
							+ "userId INTEGER, "
							+ "cpuReq REAL, "
							+ "memReq REAL, "
							+ "submitTime REAL, "
							+ "priority INTEGER, "
							+ "runtime REAL, "
							+ "startExec REAL, "
							+ "actualRuntime REAL, "
							+ "hostId INTEGER, "
							+ "running INTEGER, "
							+ "PRIMARY KEY (vmId)"
							+ ")");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Error while initializing the vms database store.");
		} finally {
			close(statement, connection);
		}
	}

	private static final String INSERT_DATACENTER_INFO_SQL = "INSERT INTO " + VMS_TABLE_NAME
			+ " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	
	public boolean addWaitingVms(SortedSet<PreemptableVm> waitingVms) {
		return addVms(waitingVms, false);		
	}
	
	public boolean addRunningVms(SortedSet<PreemptableVm> runningVms) {
		return addVms(runningVms, true);
	}

	private boolean addVms(SortedSet<PreemptableVm> runningVms, boolean running) {
		if (runningVms == null) {
			Log.printLine("waitingVms must not be null.");
			return false;
		}		
		Log.printLine("Adding " + runningVms.size() + " waiting VMs into database.");
		
		if (runningVms.isEmpty()) {
			return true;
		}
		
		PreparedStatement insertMemberStatement = null;
		
		Connection connection = null;
		
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			
			insertMemberStatement = connection.prepareStatement(INSERT_DATACENTER_INFO_SQL);
			insertMemberStatement = connection
					.prepareStatement(INSERT_DATACENTER_INFO_SQL);
			
			for (PreemptableVm vm : runningVms) {
				insertMemberStatement.setInt(1, vm.getId());
				insertMemberStatement.setInt(2, vm.getUserId());
				insertMemberStatement.setDouble(3, vm.getMips());
				insertMemberStatement.setDouble(4, vm.getRam());
				insertMemberStatement.setDouble(5, vm.getSubmitTime());
				insertMemberStatement.setInt(6, vm.getPriority());
				insertMemberStatement.setDouble(7, vm.getRuntime());
				insertMemberStatement.setDouble(8, vm.getStartExec());
				insertMemberStatement.setDouble(9, vm.getActualRuntime(time));
				
				if (running) {
					insertMemberStatement.setInt(10, vm.getHost().getId()); //vm is waiting and doesn't have host
					insertMemberStatement.setInt(11, RUNNING);
				} else {
					insertMemberStatement.setInt(10, -1); //vm is waiting and doesn't have host
					insertMemberStatement.setInt(11, WAITING);
				}
				insertMemberStatement.addBatch();
			}
			
			int[] executeBatch = insertMemberStatement.executeBatch();
			
			if (executionFailed(connection, executeBatch)){
				Log.printLine("Rollback will be executed.");
				connection.rollback();
				return false;
			}
			
			connection.commit();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			Log.printLine("Couldn't add vms.");
			try {
				if (connection != null) {
					connection.rollback();
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
				Log.printLine("Couldn't rollback transaction.");
			}
			return false;
		} finally {
			close(insertMemberStatement, connection);
		}
	}
	
	private boolean executionFailed(Connection connection, int[] executeBatch) {
		for (int i : executeBatch) {
			if (i == PreparedStatement.EXECUTE_FAILED) {
				return true;
			}
		}
		return false;
	}

	private static final String SELECT_RUNNING_VMS_SQL = "SELECT * FROM "
			+ VMS_TABLE_NAME + " WHERE running == '" + RUNNING + "'";

	public List<PreemptableVm> getAllRunningVms() {
		Statement statement = null;
		Connection conn = null;
		List<PreemptableVm> runningVms = new ArrayList<PreemptableVm>();
		
		try {
			conn = getConnection();
			statement = conn.createStatement();

			statement.execute(SELECT_RUNNING_VMS_SQL);
			ResultSet rs = statement.getResultSet();

			/*
			 * TODO how to treat the information about Host?
			 */
			while (rs.next()) {
				PreemptableVm vm = new PreemptableVm(rs.getInt("vmId"),
						rs.getInt("userId"), rs.getDouble("cpuReq"),
						rs.getDouble("memReq"), rs.getDouble("submitTime"),
						rs.getInt("priority"), rs.getDouble("runtime"));
				vm.setStartExec(time);
				vm.setActualRuntime(rs.getDouble("actualRuntime"));	
				runningVms.add(vm);
			}
			return runningVms;
		} catch (SQLException e) {
			Log.print(e);
			Log.printLine("Couldn't get tasks from DB.");
			return null;
		}
		
	}

	private static final String SELECT_WAITING_VMS_SQL = "SELECT * FROM "
			+ VMS_TABLE_NAME + " WHERE running == '" + WAITING + "'";
	
	public List<PreemptableVm> getAllWaitingVms() {
		Statement statement = null;
		Connection conn = null;
		List<PreemptableVm> waitingVms = new ArrayList<PreemptableVm>();
		
		try {
			conn = getConnection();
			statement = conn.createStatement();

			statement.execute(SELECT_WAITING_VMS_SQL);
			ResultSet rs = statement.getResultSet();

			while (rs.next()) {
				PreemptableVm vm = new PreemptableVm(rs.getInt("vmId"),
						rs.getInt("userId"), rs.getDouble("cpuReq"),
						rs.getDouble("memReq"), rs.getDouble("submitTime"),
						rs.getInt("priority"), rs.getDouble("runtime"));
				vm.setActualRuntime(rs.getDouble("actualRuntime"));	
				waitingVms.add(vm);
			}
			return waitingVms;
		} catch (SQLException e) {
			Log.print(e);
			Log.printLine("Couldn't get tasks from DB.");
			return null;
		}		
	}	
}
