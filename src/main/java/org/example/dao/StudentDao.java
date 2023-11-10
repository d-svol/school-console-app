package org.example.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.model.Course;
import org.example.model.Group;
import org.example.model.Student;
import org.example.exceptions.DbException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentDao implements Dao<Student> {
	private static final String INSERT_STUDENT = "INSERT INTO students (group_id, first_name, last_name) VALUES (?, ?, ?)";
	private static final String INSERT_STUDENT_COURSE = "INSERT INTO student_course (student_id, course_id) VALUES (?, ?)";
	private static final String DELETE_STUDENT_COURSE = "DELETE FROM student_course WHERE student_id = ? AND course_id = ?";
	private static final String SELECT_STUDENT_BY_ID =
			"SELECT students.student_id, students.first_name, students.last_name, students.group_id, groups.group_name " +
					"FROM students " +
					"LEFT JOIN groups ON students.group_id = groups.group_id " +
					"WHERE students.student_id = ?";
	private static final String SELECT_ALL_STUDENTS = "SELECT * FROM students";
	private static final String UPDATE_STUDENT = "UPDATE students SET group_id = ?, first_name = ?, last_name = ? WHERE student_id = ?";
	private static final String DELETE_STUDENT = "DELETE FROM students WHERE student_id = ?";
	private static final String SELECT_COURSES_FOR_STUDENT =
			"SELECT c.course_id, c.course_name, c.course_description " +
					"FROM courses c " +
					"JOIN student_course sc ON c.course_id = sc.course_id " +
					"WHERE sc.student_id = ?";
	private static final String SELECT_STUDENTS_BY_COURSE_NAME =
			"SELECT students.student_id, students.first_name, students.last_name " +
					"FROM students " +
					"INNER JOIN student_course ON students.student_id = student_course.student_id " +
					"INNER JOIN courses ON student_course.course_id = courses.course_id " +
					"WHERE courses.course_name = ?";

	private static final Logger log = LogManager.getLogger(StudentDao.class);
	private final DataSource dbPool;

	public StudentDao(DataSource dbPool) {
		this.dbPool = dbPool;
	}

	public List<Student> findStudentsByCourseName(String courseName) {
		List<Student> students = new ArrayList<>();
		try (Connection connection = dbPool.getConnection();
			 PreparedStatement preparedStatement = connection.prepareStatement(SELECT_STUDENTS_BY_COURSE_NAME)) {
			preparedStatement.setString(1, courseName);
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				int studentId = resultSet.getInt("student_id");
				Optional<Student> studentOptional = findByID(studentId);
				Student student = studentOptional.orElse(null);
				students.add(student);
			}
		} catch (SQLException e) {
			log.error("Error while finding students by course name: {}", e.getMessage(), e);
			throw new DbException(e);
		}
		return students;
	}

	public void addStudentToCourse(Student student, int courseId) {
		try (Connection connection = dbPool.getConnection();
			 PreparedStatement preparedStatement = connection.prepareStatement(INSERT_STUDENT_COURSE)) {
			preparedStatement.setInt(1, student.id());
			preparedStatement.setInt(2, courseId);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			log.error("Error add student to course", e);
			throw new DbException(e);
		}
	}

	public void removeStudentFromCourse(Student student, int courseId) {
		try (Connection connection = dbPool.getConnection();
			 PreparedStatement preparedStatement = connection.prepareStatement(DELETE_STUDENT_COURSE)) {
			preparedStatement.setInt(1, student.id());
			preparedStatement.setInt(2, courseId);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			log.error("Error remove student from course", e);
			throw new DbException(e);
		}
	}


	@Override
	public void save(Student student) {
		try (Connection connection = dbPool.getConnection();
			 PreparedStatement preparedStatement = connection.prepareStatement(INSERT_STUDENT)) {
			preparedStatement.setInt(1, student.group().id());
			preparedStatement.setString(2, student.firstName());
			preparedStatement.setString(3, student.lastName());
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			log.error("Error while saving student to the database", e);
			throw new DbException(e);
		}
	}

	@Override
	public Optional<Student> findByID(int id) {
		try (Connection connection = dbPool.getConnection();
			 PreparedStatement preparedStatement = connection.prepareStatement(SELECT_STUDENT_BY_ID)) {
			preparedStatement.setInt(1, id);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					int studentId = resultSet.getInt("student_id");
					String firstName = resultSet.getString("first_name");
					String lastName = resultSet.getString("last_name");
					int groupId = resultSet.getInt("group_id");
					String groupName = resultSet.getString("group_name");
					List<Course> courses = getCoursesForStudent(studentId);
					Group group = new Group(groupId, groupName);
					Student student = new Student(studentId, firstName, lastName, group, courses);
					return Optional.of(student);
				} else {
					return Optional.empty();
				}
			}
		} catch (SQLException e) {
			log.error("Error finding by ID student in the database", e);
			throw new DbException(e);
		}
	}

	@Override
	public List<Student> findAll() {
		List<Student> students = new ArrayList<>();
		try (Connection connection = dbPool.getConnection();
			 PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_STUDENTS)) {
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				int studentId = resultSet.getInt("student_id");
				int groupId = resultSet.getInt("group_id");
				String firstName = resultSet.getString("first_name");
				String lastName = resultSet.getString("last_name");
				Group group = new Group(groupId, "group_id");
				List<Course> courses = getCoursesForStudent(studentId);

				Student student = new Student(studentId, firstName, lastName, group, courses);

				students.add(student);
			}
			return students;
		} catch (SQLException e) {
			log.error("Error find all students to the database", e);
			throw new DbException(e);
		}
	}

	@Override
	public void update(Student student) {
		try (Connection connection = dbPool.getConnection();
			 PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_STUDENT)) {
			preparedStatement.setInt(1, student.id());
			preparedStatement.setString(2, student.firstName());
			preparedStatement.setString(3, student.lastName());
			preparedStatement.setInt(4, student.id());
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			log.error("Error update student to the database", e);
			throw new DbException(e);
		}
	}

	@Override
	public void delete(Student student) {
		try (Connection connection = dbPool.getConnection();
			 PreparedStatement preparedStatement = connection.prepareStatement(DELETE_STUDENT)) {
			preparedStatement.setInt(1, student.id());
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			log.error("Error delete student to the database", e);
			throw new DbException(e);
		}
	}

	private List<Course> getCoursesForStudent(int studentId) {
		List<Course> courses = new ArrayList<>();
		try (Connection connection = dbPool.getConnection();
			 PreparedStatement preparedStatement = connection.prepareStatement(SELECT_COURSES_FOR_STUDENT)) {
			preparedStatement.setInt(1, studentId);
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				int courseId = resultSet.getInt("course_id");
				String courseName = resultSet.getString("course_name");
				String courseDescription = resultSet.getString("course_description");
				courses.add(new Course(courseId, courseName, courseDescription));
			}
		} catch (SQLException e) {
			log.error("Error get courses for student ID to the database", e);
			throw new DbException(e);
		}
		return courses;
	}
}


