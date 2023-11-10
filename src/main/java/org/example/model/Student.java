package org.example.model;

import java.util.List;

public record Student(int id, String firstName, String lastName, Group group, List<Course> courseList) {
}