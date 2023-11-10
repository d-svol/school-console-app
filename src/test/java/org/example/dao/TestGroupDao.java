package org.example.dao;

import org.example.exceptions.DbException;

import org.example.model.Group;
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

public class TestGroupDao {
	private GroupDao groupDao;
	private Connection connection;

	@BeforeEach
	public void setup() {
		DataSource dataSource = JdbcConnectionPool.create("jdbc:h2:mem:testdb;" + "MODE=PostgreSQL;" + "DATABASE_TO_UPPER=false;" + "DB_CLOSE_ON_EXIT=FALSE", "sa", "");
		try (Connection connection = dataSource.getConnection()) {
			this.connection = connection;
			executeSQLScript("create_table.sql");
			groupDao = new GroupDao(dataSource);
		} catch (Exception e) {
			System.err.println("Error creating tables: " + e.getMessage());
			throw new DbException(e);
		}
	}

	@Test
	public void testFindGroupsWithLessOrEqualStudents() throws SQLException {
		int maxStudents = 5;
		for (int i = 1; i <= 10; i++) {
			Group group = new Group(i, "Group_" + i);
			groupDao.save(group);
		}
		List<Group> groups = groupDao.findGroupsWithLessOrEqualStudents(maxStudents);
		assertNotNull(groups);
		assertEquals(10, groups.size());
	}

	@Test
	public void testSave() {
		Group group = new Group(1, "Test");
		groupDao.save(group);
		Optional<Group> loadedGroupOptional = groupDao.findByID(1);
		assertTrue(loadedGroupOptional.isPresent());
		Group loadedGroup = loadedGroupOptional.get();
		assertEquals(group.id(), loadedGroup.id());
		assertEquals(group.name(), loadedGroup.name());
	}

	@Test
	public void findByID() {
		int groupID = 1;
		Group group = new Group(groupID, "Test");
		groupDao.save(group);
		Optional<Group> loadedGroupOptional = groupDao.findByID(groupID);
		assertTrue(loadedGroupOptional.isPresent());
		Group loadedGroup = loadedGroupOptional.get();
		assertEquals(group.id(), loadedGroup.id());
		assertEquals(group.name(), loadedGroup.name());
	}

	@Test
	public void findAll() {
		List<Group> groupList = new ArrayList<>();
		int testID = 9;
		for (int i = 1; i <= 10; i++) {
			Group group = new Group(i, "Group" + i);
			groupList.add(group);
			groupDao.save(group);
		}
		Optional<Group> loadedGroupOptional = groupDao.findByID(10);
		assertTrue(loadedGroupOptional.isPresent());
		Group loadedGroup = loadedGroupOptional.get();
		assertEquals(groupList.get(testID).id(), loadedGroup.id());
		assertEquals(groupList.get(testID).name(), loadedGroup.name());
	}

	@Test
	public void testUpdate() {
		int testID = 1;
		Group group = new Group(testID, "Test Group");
		Group newGroup = new Group(testID, "Updated Group Name");
		groupDao.save(group);
		groupDao.update(newGroup);
		Optional<Group> updatedGroupOptional = groupDao.findByID(testID);
		assertTrue(updatedGroupOptional.isPresent());
		Group updatedGroup = updatedGroupOptional.get();
		assertEquals(newGroup.id(), updatedGroup.id());
		assertEquals(newGroup.name(), updatedGroup.name());
	}

	@Test
	public void testDelete() {
		Group group = new Group(1, "Test Group");
		groupDao.save(group);
		groupDao.delete(group);
		Optional<Group> deletedGroupOptional = groupDao.findByID(group.id());
		assertTrue(deletedGroupOptional.isEmpty());
		assertFalse(deletedGroupOptional.isPresent());
	}

	private void executeSQLScript(String scriptFileName) throws IOException, SQLException {
		try (Statement statement = connection.createStatement(); InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(DbLoader.class.getResourceAsStream("/" + scriptFileName)))) {
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