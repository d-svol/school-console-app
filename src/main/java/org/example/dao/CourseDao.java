package org.example.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.exceptions.DbException;
import org.example.model.Course;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CourseDao implements Dao<Course> {
	private static final String INSERT_COURSE = "INSERT INTO courses (course_name, course_description) VALUES (?, ?)";
	private static final String SELECT_COURSE_BY_ID = "SELECT * FROM courses WHERE course_id = ?";
	private static final String SELECT_ALL_COURSES = "SELECT * FROM courses";
	private static final String UPDATE_COURSE = "UPDATE courses SET course_name = ?, course_description = ? WHERE course_id = ?";
	private static final String DELETE_COURSE = "DELETE FROM courses WHERE course_id = ?";

	private static final Logger log = LogManager.getLogger(CourseDao.class);
	private final DataSource dbPool;

	public CourseDao(DataSource dbPool) {
		this.dbPool = dbPool;
	}

	@Override
	public void save(Course course) {
		try (Connection connection = dbPool.getConnection();
			 PreparedStatement preparedStatement = connection.prepareStatement(INSERT_COURSE)) {
			preparedStatement.setString(1, course.name());
			preparedStatement.setString(2, course.description());
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			log.error("Error while saving course to the database", e);
			throw new DbException(e);
		}
	}

	@Override
	public Optional<Course> findByID(int id) {
		try (Connection connection = dbPool.getConnection();
			 PreparedStatement preparedStatement = connection.prepareStatement(SELECT_COURSE_BY_ID)) {
			preparedStatement.setInt(1, id);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					Course course = new Course(
							resultSet.getInt("course_id"),
							resultSet.getString("course_name"),
							resultSet.getString("course_description")
					);
					return Optional.of(course);
				} else {
					return Optional.empty();
				}
			}
		} catch (SQLException e) {
			log.error("Error find ID in the database", e);
			throw new DbException(e);
		}
	}

	@Override
	public List<Course> findAll() {
		List<Course> courses = new ArrayList<>();
		try (Connection connection = dbPool.getConnection();
			 PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_COURSES)) {
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				courses.add(new Course(
						resultSet.getInt("course_id"),
						resultSet.getString("course_name"),
						resultSet.getString("course_description")
				));
			}
			return courses;
		} catch (SQLException e) {
			log.error("Error find all courses in the database", e);
			throw new DbException(e);
		}
	}

	@Override
	public void update(Course course) {
		try (Connection connection = dbPool.getConnection();
			 PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_COURSE)) {
			preparedStatement.setString(1, course.name());
			preparedStatement.setString(2, course.description());
			preparedStatement.setInt(3, course.id());
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			log.error("Error update course in the database" + e);
			throw new DbException(e);
		}
	}

	@Override
	public void delete(Course course) {
		try (Connection connection = dbPool.getConnection();
			 PreparedStatement preparedStatement = connection.prepareStatement(DELETE_COURSE)) {
			preparedStatement.setInt(1, course.id());
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			log.error("Error delete course in the database", e);
			throw new DbException(e);
		}
	}
}
