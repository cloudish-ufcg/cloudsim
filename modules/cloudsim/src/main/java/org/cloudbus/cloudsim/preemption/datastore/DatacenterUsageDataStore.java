package org.cloudbus.cloudsim.preemption.datastore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.preemption.DatacenterInfo;

public class DatacenterUsageDataStore extends DataStore {

	public static final String DATABASE_URL_PROP = "datacenter_database_url";
	private static final String DATACENTER_TABLE_NAME = "datacenterusage";
	
	public DatacenterUsageDataStore(Properties properties) {
		super(properties.getProperty(DATABASE_URL_PROP));

		Statement statement = null;
		Connection connection = null;
		try {
			Log.printLine("datacenter_database_url=" + getDatabaseURL());

			Class.forName(DATASTORE_SQLITE_DRIVER);

			connection = getConnection();
			statement = connection.createStatement();
			statement
					.execute("CREATE TABLE IF NOT EXISTS datacenterusage("
							+ "time REAL NOT NULL, "
							+ "vmsRunning INTEGER, "
							+ "vmsRunningP0 INTEGER, "
							+ "resourcesRunningP0 REAL, "
							+ "vmsRunningP1 INTEGER, "
							+ "resourcesRunningP1 REAL, "
							+ "vmsRunningP2 INTEGER, "
							+ "resourcesRunningP2 REAL, "
							+ "vmsForScheduling INTEGER, "
							+ "vmsForSchedulingP0 INTEGER, "
							+ "resourcesWaitingP0 REAL, "
							+ "vmsForSchedulingP1 INTEGER, "
							+ "resourcesWaitingP1 REAL, "
							+ "vmsForSchedulingP2 INTEGER, "
							+ "resourcesWaitingP2 REAL, "
							+ "PRIMARY KEY (time)"
							+ ")");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Error while initializing the Utilization database store.");
		} finally {
			close(statement, connection);
		}
	}
	
	private static final String INSERT_DATACENTER_INFO_SQL = "INSERT INTO " + DATACENTER_TABLE_NAME
			+ " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	public boolean addDatacenterInfo(List<DatacenterInfo> datacenterInfo) {
		if (datacenterInfo == null) {
			Log.printLine("datacenter must no be null.");
			return false;
		}		
		Log.printLine("Adding " + datacenterInfo.size() + " datacenter info into database.");
		
		if (datacenterInfo.isEmpty()) {
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
			
			for (DatacenterInfo info : datacenterInfo) {
				insertMemberStatement.setDouble(1, info.getTime());
				insertMemberStatement.setInt(2, info.getVmsRunning());
				insertMemberStatement.setInt(3, info.getVmsRunningP0());
				insertMemberStatement.setDouble(4, info.getResourcesRunningP0());
				insertMemberStatement.setInt(5, info.getVmsRunningP1());
				insertMemberStatement.setDouble(6, info.getResourcesRunningP1());
				insertMemberStatement.setInt(7, info.getVmsRunningP2());
				insertMemberStatement.setDouble(8, info.getResourcesRunningP2());
				insertMemberStatement.setInt(9, info.getVmsForScheduling());
				insertMemberStatement.setInt(10, info.getVmsForSchedulingP0());
				insertMemberStatement.setDouble(11, info.getResourcesWaitingP0());
				insertMemberStatement.setInt(12, info.getVmsForSchedulingP1());
				insertMemberStatement.setDouble(13, info.getResourcesWaitingP1());
				insertMemberStatement.setInt(14, info.getVmsForSchedulingP2());
				insertMemberStatement.setDouble(15, info.getResourcesWaitingP2());
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
			Log.printLine("Couldn't add datacenter info entries.");
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

	private static final String SELECT_ALL_ENTRIES_SQL = "SELECT * FROM " + DATACENTER_TABLE_NAME;
	
	public List<DatacenterInfo> getAllDatacenterInfo() {
		Statement statement = null;
		Connection conn = null;
		List<DatacenterInfo> entries = new ArrayList<DatacenterInfo>();
		
		try {
			conn = getConnection();
			statement = conn.createStatement();

			statement.execute(SELECT_ALL_ENTRIES_SQL);
			ResultSet rs = statement.getResultSet();

			while (rs.next()) {
				entries.add(new DatacenterInfo(rs.getDouble("time"), rs
						.getInt("vmsRunning"), rs
						.getInt("vmsRunningP0"), rs
						.getDouble("resourcesRunningP0"), rs
						.getInt("vmsRunningP1"), rs
						.getDouble("resourcesRunningP1"), rs
						.getInt("vmsRunningP2"), rs
						.getDouble("resourcesRunningP2"), rs
						.getInt("vmsForScheduling"), rs
						.getInt("vmsForSchedulingP0"), rs
						.getDouble("resourcesWaitingP0"), rs
						.getInt("vmsForSchedulingP1"), rs
						.getDouble("resourcesWaitingP1"), rs
						.getInt("vmsForSchedulingP2"), rs
						.getDouble("resourcesWaitingP2")));
			}
			return entries;
		} catch (SQLException e) {
			Log.print(e);
			Log.printLine("Couldn't get tasks from DB.");
			return null;
		}
	}

	public List<DatacenterInfo> getDatacenterInfoFinishedBefore(double interestedTime) {
		Statement statement = null;
		Connection conn = null;

		try {
			conn = getConnection();
			statement = conn.createStatement();

			statement.execute("SELECT * FROM " + DATACENTER_TABLE_NAME
					+ " WHERE time <= '" + interestedTime + "'");
			ResultSet rs = statement.getResultSet();

			return generateDatacenterInfoList(rs);
		} catch (SQLException e) {
			Log.print(e);
			Log.printLine("Couldn't get tasks from DB.");
			return null;
		}
	}

	private List<DatacenterInfo> generateDatacenterInfoList(ResultSet rs) throws SQLException{
		List<DatacenterInfo> datacenterInfoList = new ArrayList<>();
		while (rs.next()) {
			datacenterInfoList.add(new DatacenterInfo(rs.getDouble("time"), rs
					.getInt("vmsRunning"), rs.getInt("vmsRunningP0"), rs
					.getDouble("resourcesRunningP0"), rs
					.getInt("vmsRunningP1"), rs
					.getDouble("resourcesRunningP1"), rs
					.getInt("vmsRunningP2"), rs
					.getDouble("resourcesRunningP2"), rs
					.getInt("vmsForScheduling"), rs
					.getInt("vmsForSchedulingP0"), rs
					.getDouble("resourcesWaitingP0"), rs
					.getInt("vmsForSchedulingP1"), rs
					.getDouble("resourcesWaitingP1"), rs
					.getInt("vmsForSchedulingP2"), rs
					.getDouble("resourcesWaitingP2")));
		}

		return datacenterInfoList;
	}
}
