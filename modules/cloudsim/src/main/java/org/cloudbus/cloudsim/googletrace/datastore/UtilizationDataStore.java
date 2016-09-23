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
import org.cloudbus.cloudsim.googletrace.HostUtilizationEntry;

public class UtilizationDataStore extends GoogleDataStore {

	public static final String DATABASE_URL_PROP = "utilization_database_url";
	private static final String UTILIZATION_TABLE_NAME = "utilization";
	
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
					.execute("CREATE TABLE IF NOT EXISTS utilization("
							+ "host_id INTEGER NOT NULL, "
							+ "time REAL NOT NULL, "
							+ "utilization REAL NOT NULL, "
							+ "PRIMARY KEY (host_id, time)"
							+ ")");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Error while initializing the Utilization database store.");
		} finally {
			close(statement, connection);
		}
	}
	
	private static final String INSERT_UTILIZATION_ENTRY_SQL = "INSERT INTO " + UTILIZATION_TABLE_NAME
			+ " VALUES(?, ?, ?)";

	public boolean addUtilizationEntries(
			List<HostUtilizationEntry> utilizationEntries) {
		
		if (utilizationEntries == null) {
			Log.printLine("utilizationEntries must no be null.");
			return false;
		}		
		Log.printLine("Adding " + utilizationEntries.size() + " utilization entries into database.");
		
		if (utilizationEntries.isEmpty()) {
			return true;
		}
		
		PreparedStatement insertMemberStatement = null;
		
		Connection connection = null;
		
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			
			insertMemberStatement = connection.prepareStatement(INSERT_UTILIZATION_ENTRY_SQL);
			insertMemberStatement = connection
					.prepareStatement(INSERT_UTILIZATION_ENTRY_SQL);
			
			for (HostUtilizationEntry entry : utilizationEntries) {
				insertMemberStatement.setInt(1, entry.getHostId());
				insertMemberStatement.setDouble(2, entry.getTime());
				insertMemberStatement.setDouble(3, entry.getUtilization());
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

	private boolean executionFailed(Connection connection, int[] executeBatch) {
		for (int i : executeBatch) {
			if (i == PreparedStatement.EXECUTE_FAILED) {
				return true;
			}
		}
		return false;
	}

	private static final String SELECT_ALL_ENTRIES_SQL = "SELECT * FROM " + UTILIZATION_TABLE_NAME;

	public List<HostUtilizationEntry> getAllUtilizationEntries() {
		Statement statement = null;
		Connection conn = null;
		List<HostUtilizationEntry> entries = new ArrayList<HostUtilizationEntry>();
		
		try {
			conn = getConnection();
			statement = conn.createStatement();

			statement.execute(SELECT_ALL_ENTRIES_SQL);
			ResultSet rs = statement.getResultSet();

			while (rs.next()) {
				entries.add(new HostUtilizationEntry(rs.getInt("host_id"), rs
						.getDouble("time"), rs.getDouble("utilization")));
			}
			return entries;
		} catch (SQLException e) {
			Log.print(e);
			Log.printLine("Couldn't get tasks from DB.");
			return null;
		}
	}
}