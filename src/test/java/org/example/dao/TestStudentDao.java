package org.example.dao;

import org.example.exceptions.DbException;
import org.example.model.Course;
import org.example.model.Group;
import org.example.model.Student;
import org.example.runner.DbLoader;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TestStudentDao {
	private StudentDao studentDao;
	private GroupDao groupDao;
	private CourseDao courseDao;
	private Connection connection;

	@BeforeEach
	public void setup() {
		DataSource dataSource = JdbcConnectionPool.create("jdbc:h2:mem:testdb;" + "MODE=PostgreSQL;" + "DATABASE_TO_UPPER=false;" + "DB_CLOSE_ON_EXIT=FALSE", "sa", "");
		try (Connection connection = dataSource.getConnection()) {
			this.connection = connection;
			executeSQLScript("create_table.sql");
			studentDao = new StudentDao(dataSource);
			groupDao = new GroupDao(dataSource);
			courseDao = new CourseDao(dataSource);
		} catch (Exception e) {
			System.err.println("Error creating tables: " + e.getMessage());
			throw new DbException(e);
		}
	}

	@Test
	public void testSave() {
		int studentID = 1;
		List<Course> courseList = new ArrayList<>();
		Group group = new Group(1, "Group");
		Student student = new Student(studentID, "FirstName", "LastName", new Group(1, "Group"), courseList);
		groupDao.save(group);
		studentDao.save(student);
		Optional<Student> loadedStudentOptional = studentDao.findByID(studentID);
		assertTrue(loadedStudentOptional.isPresent());
		Student loadedStudent = loadedStudentOptional.get();
		assertEquals(student.group().id(), loadedStudent.group().id());
		assertEquals(student.firstName(), loadedStudent.firstName());
		assertEquals(student.lastName(), loadedStudent.lastName());
	}

	@Test
	public void findByID() {
		Group group = new Group(1, "Group");
		Student firstStudent = new Student(1, "A", "A", group, new ArrayList<>());
		Student secondStudent = new Student(2, "B", "B", group, new ArrayList<>());
		groupDao.save(group);
		studentDao.save(firstStudent);
		studentDao.save(secondStudent);

		Optional<Student> loadedFirstStudentOptional = studentDao.findByID(1);
		Optional<Student> loadedSecondStudentOptional = studentDao.findByID(2);

		assertTrue(loadedFirstStudentOptional.isPresent());
		Student loadedFirstStudent = loadedFirstStudentOptional.get();
		assertTrue(loadedSecondStudentOptional.isPresent());
		Student loadedSecondStudent = loadedSecondStudentOptional.get();

		assertAll("Student properties",
				() -> assertEquals(firstStudent.id(), loadedFirstStudent.id()),
				() -> assertEquals(firstStudent.firstName(), loadedFirstStudent.firstName()),
				() -> assertEquals(firstStudent.lastName(), loadedFirstStudent.lastName())
		);

		assertAll("Student properties",
				() -> assertEquals(secondStudent.id(), loadedSecondStudent.id()),
				() -> assertEquals(secondStudent.firstName(), loadedSecondStudent.firstName()),
				() -> assertEquals(secondStudent.lastName(), loadedSecondStudent.lastName())
		);

		assertNotEquals(loadedFirstStudent.id(), loadedSecondStudent.id());
	}

	@Test
	public void testAddStudentToCourse() {
		Group group = new Group(1, "Group");
		Course course = new Course(1, "A", "A");
		Student student = new Student(1, "A", "A", group, new ArrayList<>());
		groupDao.save(group);
		courseDao.save(course);
		studentDao.save(student);

		studentDao.addStudentToCourse(student, course.id());
		Optional<Student> loadedStudentOptional = studentDao.findByID(student.id());
		assertTrue(loadedStudentOptional.isPresent());
		Student loadedStudent = loadedStudentOptional.get();

		assertTrue(loadedStudent.courseList().contains(course));
	}

	@Test
	public void testRemoveStudentToCourse() {
		Group group = new Group(1, "Group");
		Course course = new Course(1, "A", "A");
		Student student = new Student(1, "A", "A", group, new ArrayList<>());
		groupDao.save(group);
		courseDao.save(course);
		studentDao.save(student);

		studentDao.addStudentToCourse(student, course.id());
		Optional<Student> loadedStudentOptional = studentDao.findByID(student.id());
		assertTrue(loadedStudentOptional.isPresent());
		Student loadedStudent = loadedStudentOptional.get();
		assertTrue(loadedStudent.courseList().contains(course));

		studentDao.removeStudentFromCourse(student, course.id());
		Optional<Student> updatedStudentOptional = studentDao.findByID(student.id());
		assertTrue(updatedStudentOptional.isPresent());
		Student updatedStudent = updatedStudentOptional.get();
		assertFalse(updatedStudent.courseList().contains(course));
	}


	@Test
	public void findAll() {
		List<Student> studentList = new ArrayList<>();
		Group group = new Group(1, "Group");
		groupDao.save(group);
		for (int i = 1; i <= 10; i++) {
			Student student = new Student(i, "A", "A", new Group(1, "Group"), new ArrayList<>());
			studentList.add(student);
			studentDao.save(student);
		}

		List<Student> loadedStudents = studentDao.findAll();
		for (int i = 0; i < 10; i++) {
			assertEquals(studentList.get(i).id(), loadedStudents.get(i).id());
			assertEquals(studentList.get(i).firstName(), loadedStudents.get(i).firstName());
			assertEquals(studentList.get(i).lastName(), loadedStudents.get(i).lastName());
		}
	}

	@Test
	public void testUpdate() {
		Group group = new Group(1, "Group");
		groupDao.save(group);
		Student student = new Student(1, "A", "A", group, new ArrayList<>());
		studentDao.save(student);
		Student updatedStudent = new Student(1, "B", "B", group, new ArrayList<>());
		studentDao.update(updatedStudent);
		Optional<Student> optionalLoadStudent = studentDao.findByID(1);
		assertTrue(optionalLoadStudent.isPresent());
		Student loadStudent = optionalLoadStudent.get();

		assertEquals(updatedStudent.id(), loadStudent.id());
		assertEquals(updatedStudent.firstName(), loadStudent.firstName());
		assertEquals(updatedStudent.lastName(), loadStudent.lastName());
	}


	@Test
	public void testDelete() {
		Group group = new Group(1, "Group");
		groupDao.save(group);
		Student student = new Student(1, "A", "A", group, new ArrayList<>());
		studentDao.save(student);
		studentDao.delete(student);

		Optional<Student> deletedStudentOptional = studentDao.findByID(student.id());
		assertTrue(deletedStudentOptional.isEmpty());
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
