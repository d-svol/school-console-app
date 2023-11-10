package org.example.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.exceptions.DbException;
import org.example.model.Group;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GroupDao implements Dao<Group> {
	private static final String INSERT_GROUP = "INSERT INTO groups (group_id, group_name) VALUES (?, ?)";
	private static final String SELECT_GROUP_BY_ID = "SELECT * FROM groups WHERE group_id = ?";
	private static final String SELECT_ALL_GROUPS = "SELECT * FROM groups";
	private static final String UPDATE_GROUP = "UPDATE groups SET group_name = ? WHERE group_id = ?";
	private static final String DELETE_GROUP = "DELETE FROM groups WHERE group_id = ?";
	private static final String SELECT_GROUPS_SQL =
			"SELECT groups.group_id, groups.group_name, COUNT(students.student_id) AS student_count " +
					"FROM groups " +
					"LEFT JOIN students ON groups.group_id = students.group_id " +
					"GROUP BY groups.group_id, groups.group_name " +
					"HAVING COUNT(students.student_id) <= ?";

	private static final Logger log = LogManager.getLogger(GroupDao.class);
	private final DataSource dbPool;

	public GroupDao(DataSource dbPool) {
		this.dbPool = dbPool;
	}

	public List<Group> findGroupsWithLessOrEqualStudents(int maxStudents) throws SQLException {
		try (Connection connection = dbPool.getConnection();
			 PreparedStatement preparedStatement = connection.prepareStatement(SELECT_GROUPS_SQL)) {
			List<Group> groups = new ArrayList<>();

			preparedStatement.setInt(1, maxStudents);
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				int groupId = resultSet.getInt("group_id");
				String groupName = resultSet.getString("group_name");
				groups.add(new Group(groupId, groupName));
			}
			return groups;
		}
	}


	@Override
	public void save(Group group) {
		try (Connection connection = dbPool.getConnection();
			 PreparedStatement preparedStatement = connection.prepareStatement(INSERT_GROUP)) {
			preparedStatement.setInt(1, group.id());
			preparedStatement.setString(2, group.name());
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			log.error("Error while saving group to the database", e);
			throw new DbException(e);
		}
	}

	@Override
	public Optional<Group> findByID(int id) {
		try (Connection connection = dbPool.getConnection();
			 PreparedStatement preparedStatement = connection.prepareStatement(SELECT_GROUP_BY_ID)) {
			preparedStatement.setInt(1, id);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					Group group = new Group(
							resultSet.getInt("group_id"),
							resultSet.getString("group_name")
					);
					return Optional.of(group);
				} else {
					return Optional.empty();
				}
			}
		} catch (SQLException e) {
			log.error("Error finding group by ID in the database", e);
			throw new DbException(e);
		}
	}

	@Override
	public List<Group> findAll() {
		List<Group> groups = new ArrayList<>();
		try (Connection connection = dbPool.getConnection();
			 PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_GROUPS)) {
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				groups.add(new Group(
						resultSet.getInt("group_id"),
						resultSet.getString("group_name")));
			}
			if (groups.isEmpty()) {
				throw new DbException("Group not found");
			} else {
				return groups;
			}
		} catch (SQLException e) {
			log.error("Error find all groups in the database", e);
			throw new DbException(e);
		}
	}

	@Override
	public void update(Group group) {
		try (Connection connection = dbPool.getConnection();
			 PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_GROUP)) {
			preparedStatement.setString(1, group.name());
			preparedStatement.setInt(2, group.id());
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			log.error("Error update group in the database" + e);
			throw new DbException(e);
		}
	}

	@Override
	public void delete(Group group) {
		try (Connection connection = dbPool.getConnection();
			 PreparedStatement preparedStatement = connection.prepareStatement(DELETE_GROUP)) {
			preparedStatement.setInt(1, group.id());
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			log.error("Error delete group in the database", e);
			throw new DbException(e);
		}
	}
}
