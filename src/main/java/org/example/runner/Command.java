package org.example.runner;

import org.example.service.DatabaseManager;

import java.util.Arrays;
import java.util.Scanner;

public enum Command {
	FIND_ALL_GROUPS("a") {
		@Override
		public void run(DatabaseManager databaseManager, Scanner in) {
			databaseManager.performGroupsByStudentCount(in);
		}
	},

	FIND_ALL_STUDENTS("b") {
		@Override
		public void run(DatabaseManager databaseManager, Scanner in) {
			databaseManager.findStudentsByCourseName(in);
		}
	},

	ADD_NEW_STUDENT("c") {
		@Override
		public void run(DatabaseManager databaseManager, Scanner in) {
			databaseManager.addStudent(in);
		}
	},

	DELETE_STUDENT("d") {
		@Override
		public void run(DatabaseManager databaseManager, Scanner in) {
			databaseManager.deleteStudentById(in);
		}
	},

	ADD_STUDENT_TO_COURSE("e") {
		@Override
		public void run(DatabaseManager databaseManager, Scanner in) {
			databaseManager.addStudentsToCourse(in);
		}
	},

	REMOVE_STUDENT_FROM_COURSE("f") {
		@Override
		public void run(DatabaseManager databaseManager, Scanner in) {
			databaseManager.removeStudentToCourse(in);
		}
	},

	QUIT("q") {
		@Override
		public void run(DatabaseManager databaseManager, Scanner in) {
			System.out.println("Exit");
		}
	},

	UNKNOWN("") {
		@Override
		public void run(DatabaseManager databaseManager, Scanner in) {
			System.out.println("Unknown command requested");
		}
	};

	private final String code;

	Command(String code) {
		this.code = code;
	}

	public static Command parse(String code) {
		return Arrays.stream(values())
				.filter(cmdType -> cmdType.code.equals(code))
				.findFirst()
				.orElse(UNKNOWN);
	}

	public abstract void run(DatabaseManager databaseManager, Scanner in);
}
