package org.example.service;

import org.example.dao.CourseDao;
import org.example.dao.GroupDao;
import org.example.dao.StudentDao;
import org.example.model.Course;
import org.example.model.Group;
import org.example.model.Student;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ReportGenerator {
	private final StudentDao studentDao;
	private final GroupDao groupDao;
	private final CourseDao courseDao;

	public ReportGenerator(StudentDao studentDao, GroupDao groupDao, CourseDao courseDao) {
		this.studentDao = studentDao;
		this.groupDao = groupDao;
		this.courseDao = courseDao;
	}

	public void printGroupsByStudentCount(int maxStudentCount) throws SQLException {
		List<Group> groups = groupDao.findGroupsWithLessOrEqualStudents(maxStudentCount);

		System.out.println("Groups of " + maxStudentCount + " or fewer students:");
		for (Group group : groups) {
			System.out.println(group.toString());
		}
	}

	public void printStudentsByCourseName(String courseName) {
		List<Student> students = studentDao.findStudentsByCourseName(courseName);
		if (students.isEmpty()) {
			System.out.println("No students found for the course with name: " + courseName);
		} else {
			System.out.println("Students related to the course '" + courseName + "':");
			for (Student student : students) {
				System.out.println(student.firstName() + " " + student.lastName() + " (ID: " + student.id() + ")");
			}
		}
	}

	public void printAddStudent(String firstName, String lastName, int groupId, List<Integer> courseIds) {
		Optional<Group> groupOptional = groupDao.findByID(groupId);

		if (groupOptional.isEmpty()) {
			System.out.println("Error: Group not found for id: " + groupId);
		} else {
			List<Course> courseList = courseIds.stream()
					.map(courseDao::findByID)
					.flatMap(Optional::stream)
					.collect(Collectors.toList());

			boolean allCoursesFound = courseIds.stream()
					.allMatch(courseId -> courseDao.findByID(courseId).isPresent());

			if (allCoursesFound) {
				Group group = groupOptional.get();
				Student student = new Student(1, firstName, lastName, group, courseList);
				studentDao.save(student);
				System.out.println("Added student: " + student);
			} else {
				courseIds.stream()
						.filter(courseId -> courseDao.findByID(courseId).isEmpty())
						.forEach(courseId -> System.out.println("Warning: Course not found for id: " + courseId));
			}
		}
	}


	public void printDeleteStudentById(int studentId) {
		Optional<Student> studentOptional = studentDao.findByID(studentId);

		studentOptional.ifPresent(student -> {
			studentDao.delete(student);
			System.out.println("Deleted student with ID: " + studentId);
		});

		if (studentOptional.isEmpty()) {
			System.out.println("Student not found with ID: " + studentId);
		}
	}

	public void printAddStudentsToCourse(List<Integer> studentsListID, int courseId) {
		studentsListID.forEach(studentId -> {
			Optional<Student> studentOptional = studentDao.findByID(studentId);
			Optional<Course> courseOptional = courseDao.findByID(courseId);

			if (studentOptional.isPresent() && courseOptional.isPresent()) {
				Student student = studentOptional.get();
				studentDao.addStudentToCourse(student, courseId);
				System.out.println("Added student with ID " + student.id() + " to course with ID: " + courseId);
			} else {
				System.out.println("Error: Student or course not found for IDs - StudentID: " + studentId + ", CourseID: " + courseId);
			}
		});
	}

	public void printRemoveStudentFromCourse(int studentId, int courseId) {
		Optional<Student> studentOptional = studentDao.findByID(studentId);
		Optional<Course> courseOptional = courseDao.findByID(courseId);

		if (studentOptional.isPresent() && courseOptional.isPresent()) {
			Student student = studentOptional.get();
			studentDao.removeStudentFromCourse(student, courseId);
			System.out.println("Removed student with ID " + student.id() + " from course with ID: " + courseId);
		} else {
			System.out.println("Error: Student or course not found for IDs - StudentID: " + studentId + ", CourseID: " + courseId);
		}
	}
}

