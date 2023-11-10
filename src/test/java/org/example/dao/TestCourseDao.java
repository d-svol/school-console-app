package org.example.dao;

import org.example.exceptions.DbException;
import org.example.model.Course;
import org.example.runner.DbLoader;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TestCourseDao {
	private CourseDao courseDao;
	private Connection connection;

	@BeforeEach
	public void setup() {
		DataSource dataSource = JdbcConnectionPool.create("jdbc:h2:mem:testdb;" +
				"MODE=PostgreSQL;" +
				"DATABASE_TO_UPPER=false;" +
				"DB_CLOSE_ON_EXIT=FALSE", "sa", "");
		try (Connection connection = dataSource.getConnection()) {
			this.connection = connection;
			executeSQLScript("create_table.sql");
			courseDao = new CourseDao(dataSource);
		} catch (Exception e) {
			System.err.println("Error creating tables: " + e.getMessage());
			throw new DbException(e);
		}
	}

	@Test
	public void testSave() {
		Course course = new Course(1, "Test Course", "Test Description");
		courseDao.save(course);
		Optional<Course> loadedGroup = courseDao.findByID(course.id());
		assertNotNull(loadedGroup);
		assertEquals(course.name(), loadedGroup.get().name());
		assertEquals(course.description(), loadedGroup.get().description());

	}

	@Test
	public void testFindById() {
		List<Course> courseList = new ArrayList<>();
		int courseID = 10;
		int listID = courseID - 1;
		for (int i = 1; i <= 10; i++) {
			Course course = new Course(i, "Test Course" + i, "Test Description");
			courseDao.save(course);
			courseList.add(course);
		}
		Optional<Course> loadedGroup = courseDao.findByID(courseID);
		assertTrue(loadedGroup.isPresent());
		assertEquals(courseList.get(listID).id(), loadedGroup.get().id());
		assertEquals(courseList.get(listID).name(), loadedGroup.get().name());
		assertEquals(courseList.get(listID).description(), loadedGroup.get().description());
	}

	@Test
	public void testFindAllCourses() {
		Course course1 = new Course(1, "Test Course", "Test Description");
		Course course2 = new Course(2, "Test Course", "Test Description");
		courseDao.save(course1);
		courseDao.save(course2);
		List<Course> courses = courseDao.findAll();
		assertNotNull(courses);
		assertEquals(2, courses.size());
	}


	@Test
	public void testUpdateCourse() {
		int courseID = 1;
		Course course = new Course(courseID, "Test Course", "Test Description");
		Course newCourse = new Course(courseID, "Updated Course Name", "Updated Description");
		courseDao.save(course);
		courseDao.update(newCourse);

		Optional<Course> updatedCourse = courseDao.findByID(courseID);
		assertTrue(updatedCourse.isPresent());
		assertEquals(newCourse.name(), updatedCourse.get().name());
		assertEquals(newCourse.description(), updatedCourse.get().description());
	}

	@Test
	public void testDelete() {
		Course course = new Course(1, "Test Course", "Test Description");
		courseDao.save(course);
		courseDao.delete(course);
		Optional<Course> loadedCourse = courseDao.findByID(course.id());
		assertFalse(loadedCourse.isPresent());
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

	@AfterEach
	public void cleanup() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			System.err.println("Error closing connection: " + e.getMessage());
		}
	}
}