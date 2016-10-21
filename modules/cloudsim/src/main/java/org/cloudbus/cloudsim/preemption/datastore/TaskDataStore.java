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
import org.cloudbus.cloudsim.preemption.TaskState;

public class TaskDataStore extends DataStore {

	public static final String DATABASE_URL_PROP = "output_tasks_database_url";
	
	private static final String GOOGLE_TASK_TABLE_NAME = "googletasks";
			
	public TaskDataStore(Properties properties) {
		super(properties.getProperty(DATABASE_URL_PROP));

		Statement statement = null;
		Connection connection = null;
		try {
			Log.printLine("output_tasks_database_url=" + getDatabaseURL());

			Class.forName(DATASTORE_SQLITE_DRIVER);

			connection = getConnection();
			statement = connection.createStatement();
			statement
					.execute("CREATE TABLE IF NOT EXISTS googletasks("
							+ "taskId INTEGER NOT NULL, "
							+ "cpuReq REAL, "
							+ "submitTime REAL, "
							+ "finishTime REAL, "
							+ "runtime REAL, "
							+ "priority INTEGER, "
							+ "preemptions INTEGER, "
							+ "backfillingChoices INTEGER, "
							+ "PRIMARY KEY (taskId)"
							+ ")");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Error while initializing the GoogleTask database store.");
		} finally {
			close(statement, connection);
		}
	}
	
	private static final String INSERT_TASK_SQL = "INSERT INTO " + GOOGLE_TASK_TABLE_NAME
			+ " VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
	
	public boolean addTaskList(List<TaskState> taskStates) {
		if (taskStates == null) {
			Log.printLine("taskStates must no be null.");
			return false;
		}		
		Log.printLine("Adding " + taskStates.size() + " VMs into database.");
		
		PreparedStatement insertMemberStatement = null;
		
		Connection connection = null;
		
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			
			insertMemberStatement = connection.prepareStatement(INSERT_TASK_SQL);
			insertMemberStatement = connection
					.prepareStatement(INSERT_TASK_SQL);
			
			for (TaskState taskState : taskStates) {
				addTask(insertMemberStatement, taskState);
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
			Log.printLine("Couldn't add tasks.");
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

	private boolean executionFailed(Connection connection, int[] executeBatch)
			throws SQLException {
		for (int i : executeBatch) {
			if (i == PreparedStatement.EXECUTE_FAILED) {
				return true;
			}
		}
		return false;
	}

	private void addTask(PreparedStatement insertMemberStatement,
			TaskState taskState) throws SQLException {
		insertMemberStatement.setInt(1, taskState.getTaskId());
		insertMemberStatement.setDouble(2, taskState.getCpuReq());
		insertMemberStatement.setDouble(3, taskState.getSubmitTime());
		insertMemberStatement.setDouble(4, taskState.getFinishTime());
		insertMemberStatement.setDouble(5, taskState.getRuntime());
		insertMemberStatement.setInt(6, taskState.getPriority());
		insertMemberStatement.setInt(7, taskState.getNumberOfPreemptions());
		insertMemberStatement.setInt(8, taskState.getNumberOfBackfillingChoices());
		insertMemberStatement.addBatch();
	}
	
	private static final String SELECT_ALL_TASKS_SQL = "SELECT * FROM " + GOOGLE_TASK_TABLE_NAME;

	public List<TaskState> getAllTasks() {
		Statement statement = null;
		Connection conn = null;
		List<TaskState> taskStates = new ArrayList<TaskState>();
		
		try {
			conn = getConnection();
			statement = conn.createStatement();

			statement.execute(SELECT_ALL_TASKS_SQL);
			ResultSet rs = statement.getResultSet();

			while (rs.next()) {
				taskStates.add(new TaskState(rs.getInt("taskId"), rs
						.getDouble("cpuReq"), rs.getDouble("submitTime"), rs
						.getDouble("finishTime"), rs.getDouble("runtime"), rs
						.getInt("priority"), rs.getInt("preemptions"), rs
						.getInt("backfillingChoices")));
			}
			return taskStates;
		} catch (SQLException e) {
			Log.print(e);
			Log.printLine("Couldn't get tasks from DB.");
			return null;
		}
	}

}
