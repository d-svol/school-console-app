package org.example.runner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.exceptions.DbException;
import org.example.model.Course;
import org.example.model.Group;
import org.example.model.Student;
import org.example.service.DataGenerator;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

public class DbLoader {
	public static final String INSERT_GROUPS =
			"INSERT INTO groups (group_name) VALUES (?)";
	private static final String INSERT_STUDENTS =
			"INSERT INTO students (first_name, last_name, group_id) VALUES (?, ?, ?)";
	private static final String INSERT_COURSES =
			"INSERT INTO courses (course_name, course_description) VALUES (?, ?)";
	private static final String INSERT_STUDENT_COURSE =
			"INSERT INTO student_course (student_id, course_id) VALUES (?, ?)";

	private final Connection connection;
	private static final Logger log = LogManager.getLogger(DbLoader.class);

	private DbLoader(Connection connection) {
		this.connection = connection;
	}

	public static void load(DataSource ds) {
		try (Connection connection = ds.getConnection()) {
			DbLoader loader = new DbLoader(connection);
			loader.loadDb();
		} catch (IOException | SQLException e) {
			log.error("Error creating tables: " + e.getMessage(), e);
			throw new DbException("Error creating tables: " + e);
		}
	}

	private void loadDb() throws IOException, SQLException {
		executeSQLScript("create_table.sql");
		populateDB();
	}

	private void populateDB() throws SQLException {
		DataGenerator generatorDB = new DataGenerator();

		insertCoursesIntoDatabase(generatorDB.getCourses());
		insertGroupsIntoDatabase(generatorDB.getGroups());
		insertStudentsIntoDatabase(generatorDB.getStudents());
		insertStudentCoursesRelationsIntoDatabase(generatorDB.getStudents());
	}

	private void executeSQLScript(String scriptFileName) throws IOException, SQLException {
		try (Statement statement = connection.createStatement();
			 InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(DbLoader.class.getResourceAsStream("/" + scriptFileName)))) {

			try (BufferedReader bufferedReader = new BufferedReader(reader)) {
				String line;
				StringBuilder script = new StringBuilder();

				while ((line = bufferedReader.readLine()) != null) {
					script.append(line).append(" ");
					if (line.endsWith(";")) {
						String sqlCommand = script.toString();
						statement.execute(sqlCommand);
						script.setLength(0);
					}
				}
			}
		}
	}

	private void insertCoursesIntoDatabase(List<Course> courses) throws SQLException {
		try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_COURSES)) {
			for (Course course : courses) {
				preparedStatement.setString(1, course.name());
				preparedStatement.setString(2, course.description());
				preparedStatement.addBatch();
			}
			preparedStatement.executeBatch();
		}
	}

	private void insertGroupsIntoDatabase(List<Group> groups) throws SQLException {
		try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_GROUPS)) {
			for (Group group : groups) {
				preparedStatement.setString(1, group.name());
				preparedStatement.executeUpdate();
			}
		}
	}

	private void insertStudentsIntoDatabase(List<Student> students) throws SQLException {
		try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_STUDENTS)) {
			for (Student student : students) {
				preparedStatement.setString(1, student.firstName());
				preparedStatement.setString(2, student.lastName());
				preparedStatement.setInt(3, student.group().id());
				preparedStatement.addBatch();
			}
			preparedStatement.executeBatch();
		}
	}


	private void insertStudentCoursesRelationsIntoDatabase(List<Student> students) throws SQLException {
		try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_STUDENT_COURSE)) {
			for (Student student : students) {
				List<Course> studentCourses = student.courseList();
				preparedStatement.setInt(1, student.id());
				for (Course course : studentCourses) {
					preparedStatement.setInt(2, course.id());
					preparedStatement.addBatch();
				}
			}
			preparedStatement.executeBatch();
		}
	}
}
