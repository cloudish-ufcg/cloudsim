package org.cloudbus.cloudsim.googletrace.datastore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.googletrace.UsageEntry;

public class UtilizationDataStore extends GoogleDataStore {

	public static final String DATABASE_URL_PROP = "utilization_database_url";
	private static final String UTILIZATION_TABLE_NAME = "usage";
	
	public UtilizationDataStore(Properties properties) {
		super(properties.getProperty(DATABASE_URL_PROP));

		Statement statement = null;
		Connection connection = null;
		try {
			Log.printLine("utilization_database_url=" + getDatabaseURL());

			Class.forName(DATASTORE_SQLITE_DRIVER);

			connection = getConnection();
			statement = connection.createStatement();
			statement
					.execute("CREATE TABLE IF NOT EXISTS usage("
							+ "host_id INTEGER NOT NULL, "
							+ "time REAL NOT NULL, "
							+ "p0Usage REAL, "
							+ "p1Usage REAL, "
							+ "p2Usage REAL, "
							+ "p0Vms INTEGER, "
							+ "p1Vms INTEGER, "
							+ "p2Vms INTEGER, "
							+ "availableMips REAL, "
							+ "PRIMARY KEY (host_id, time)"
							+ ")");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Error while initializing the host usage database store.");
		} finally {
			close(statement, connection);
		}
	}
	
	private static final String INSERT_USAGE_ENTRY_SQL = "INSERT INTO " + UTILIZATION_TABLE_NAME
			+ " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
	
	public boolean addUsageEntries(List<UsageEntry> usageEntries) {
		if (usageEntries == null) {
			Log.printLine("usageEntries must no be null.");
			return false;
		}		
		Log.printLine("Adding " + usageEntries.size() + " usage entries into database.");
		
		if (usageEntries.isEmpty()) {
			return true;
		}
		
		PreparedStatement insertMemberStatement = null;
		
		Connection connection = null;
		
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			
			insertMemberStatement = connection.prepareStatement(INSERT_USAGE_ENTRY_SQL);
			insertMemberStatement = connection
					.prepareStatement(INSERT_USAGE_ENTRY_SQL);
			
			for (UsageEntry entry : usageEntries) {
				insertMemberStatement.setInt(1, entry.getHostId());
				insertMemberStatement.setDouble(2, entry.getTime());
				insertMemberStatement.setDouble(3, entry.getUsageByPriority(0));
				insertMemberStatement.setDouble(4, entry.getUsageByPriority(1));
				insertMemberStatement.setDouble(5, entry.getUsageByPriority(2));
				insertMemberStatement.setInt(6, entry.getNumberOfVmsByPriority(0));
				insertMemberStatement.setInt(7, entry.getNumberOfVmsByPriority(1));
				insertMemberStatement.setInt(8, entry.getNumberOfVmsByPriority(2));
				insertMemberStatement.setDouble(9, entry.getAvailableMips());
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
			Log.printLine("Couldn't add utilization entries.");
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
	
	private static final String SELECT_ALL_USAGE_ENTRIES_SQL = "SELECT * FROM " + UTILIZATION_TABLE_NAME;
	
	public List<UsageEntry> getAllUsageEntries() {
		Statement statement = null;
		Connection conn = null;
		List<UsageEntry> entries = new ArrayList<UsageEntry>();
		
		try {
			conn = getConnection();
			statement = conn.createStatement();
			
			statement.execute(SELECT_ALL_USAGE_ENTRIES_SQL);
			ResultSet rs = statement.getResultSet();
			
			while (rs.next()) {
				entries.add(new UsageEntry(rs.getInt("host_id"), rs
						.getDouble("time"), rs.getDouble("p0Usage"), rs
						.getDouble("p1Usage"), rs.getDouble("p2Usage"), rs
						.getInt("p0Vms"), rs.getInt("p1Vms"), rs
						.getInt("p2Vms"), rs.getDouble("availableMips")));
			}
			return entries;
		} catch (SQLException e) {
			Log.print(e);
			Log.printLine("Couldn't get tasks from DB.");
			return null;
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
}