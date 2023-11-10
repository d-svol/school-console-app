package org.example.service;

import org.example.exceptions.UserExitException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DatabaseManager {
	private static final String EMPTY_FIELD_ERROR_MESSAGE = "This field cannot be empty. Please enter a valid value";
	private static final String INVALID_FORMAT = "Invalid format. Please enter a valid format";
	private final ReportGenerator reportGenerator;

	public DatabaseManager(ReportGenerator reportGenerator) {
		this.reportGenerator = reportGenerator;
	}


	public void performGroupsByStudentCount(Scanner scanner) {
		boolean validInput = false;
		System.out.print("Enter the maximum number of students for the group (or 'q' to exit): ");
		while (!validInput) {
			String input = scanner.nextLine().trim();
			if (input.equalsIgnoreCase("q")) {
				break;
			}

			try {
				int maxStudentCount = Integer.parseInt(input);
				reportGenerator.printGroupsByStudentCount(maxStudentCount);
				validInput = true;
			} catch (NumberFormatException e) {
				System.out.println(INVALID_FORMAT);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void findStudentsByCourseName(Scanner scanner) {
		boolean validInput = false;
		while (!validInput) {
			System.out.print("Enter the name of the course (or 'q' to exit): ");
			String courseName = scanner.nextLine().trim();
			if (courseName.equalsIgnoreCase("q")) {
				return;
			} else if (courseName.isEmpty()) {
				System.out.println(EMPTY_FIELD_ERROR_MESSAGE);
			} else {
				reportGenerator.printStudentsByCourseName(courseName);
				validInput = true;
			}
		}
	}

	public void addStudent(Scanner scanner) {
		try {
			String firstName = getFirstNameInput(scanner);
			String lastName = getLastNameInput(scanner);
			int groupId = getIdInput(scanner);
			List<Integer> creatListInteger = creatListInteger(scanner);
			reportGenerator.printAddStudent(firstName, lastName, groupId, creatListInteger);
		} catch (UserExitException e) {
			System.out.println(e.getMessage());
		}
	}


	public void deleteStudentById(Scanner scanner) {
		while (true) {
			System.out.print("Enter STUDENT_ID to delete (or 'q' to exit): ");
			String input = scanner.nextLine().trim();

			if (input.equalsIgnoreCase("q")) {
				break;
			}

			try {
				int studentId = Integer.parseInt(input);
				reportGenerator.printDeleteStudentById(studentId);
				break;
			} catch (NumberFormatException e) {
				System.out.println(EMPTY_FIELD_ERROR_MESSAGE);
			}
		}
	}

	public void addStudentsToCourse(Scanner scanner) {
		try {
			int courseId = getIdInput(scanner);
			List<Integer> studentListID = creatListInteger(scanner);

			if (studentListID.isEmpty()) {
				System.out.println(EMPTY_FIELD_ERROR_MESSAGE);
				return;
			}
			reportGenerator.printAddStudentsToCourse(studentListID, courseId);
		} catch (UserExitException e) {
			System.out.println(e.getMessage());
		}
	}

	public void removeStudentToCourse(Scanner scanner) {
		try {
			System.out.println("Select a student by ID");
			int studentId = getIdInput(scanner);
			System.out.println("Select a course by ID");
			int courseId = getIdInput(scanner);
			reportGenerator.printRemoveStudentFromCourse(studentId, courseId);
		} catch (UserExitException e) {
			System.out.println(e.getMessage());
		}
	}

	private String getFirstNameInput(Scanner scanner) throws UserExitException {
		while (true) {
			System.out.println("Enter firstname (or 'q' to exit):");
			String input = scanner.nextLine().trim();
			if (input.equals("q")) {
				throw new UserExitException("User exited input.");
			} else if (input.isEmpty()) {
				System.out.println(EMPTY_FIELD_ERROR_MESSAGE);
			} else {
				return input;
			}
		}
	}

	private String getLastNameInput(Scanner scanner) throws UserExitException {
		while (true) {
			System.out.println("Enter lastname (or 'q' to exit):");
			String input = scanner.nextLine().trim();
			if (input.equals("q")) {
				throw new UserExitException("User exited input.");
			} else if (input.isEmpty()) {
				System.out.println(EMPTY_FIELD_ERROR_MESSAGE);
			} else {
				return input;
			}
		}
	}

	private int getIdInput(Scanner scanner) throws UserExitException {
		while (true) {
			System.out.println("Enter id (or 'q' to exit):");
			String input = scanner.nextLine().trim();
			if (input.equals("q")) {
				throw new UserExitException("User exited input.");
			} else if (input.isEmpty()) {
				System.out.println(EMPTY_FIELD_ERROR_MESSAGE);
			} else {
				try {
					return Integer.parseInt(input);
				} catch (NumberFormatException e) {
					System.out.println(INVALID_FORMAT);
				}
			}
		}
	}

	private List<Integer> creatListInteger(Scanner scanner) throws UserExitException {
		List<Integer> integerList = new ArrayList<>();
		System.out.println("Enter num to add (comma-separated) or 'q' to finish adding courses:");

		while (true) {
			String input = scanner.nextLine().trim();
			if (input.equals("q")) {
				throw new UserExitException("User exited input.");
			} else if (input.isEmpty()) {
				System.out.println(EMPTY_FIELD_ERROR_MESSAGE);
			} else {
				String[] inputArray = input.split(",");
				for (String number : inputArray) {
					try {
						int id = Integer.parseInt(number.trim());
						integerList.add(id);
					} catch (NumberFormatException e) {
						System.out.println(INVALID_FORMAT);
					}
				}
				return integerList;
			}
		}
	}
}

