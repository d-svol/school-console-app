DROP TABLE IF EXISTS student_course;

DROP TABLE IF EXISTS courses;

DROP TABLE IF EXISTS students;

DROP TABLE IF EXISTS groups;

CREATE TABLE groups (
	group_id SERIAL PRIMARY KEY,
	group_name VARCHAR(255) NOT NULL
);

CREATE TABLE students (
	student_id SERIAL PRIMARY KEY,
	group_id INTEGER REFERENCES groups (group_id),
	first_name VARCHAR(255) NOT NULL,
	last_name VARCHAR(255)  NOT NULL
);

CREATE TABLE courses (
	course_id SERIAL PRIMARY KEY,
	course_name VARCHAR(255)        NOT NULL,
	course_description VARCHAR(255) NOT NULL
);

CREATE TABLE student_course (
	student_courses_id SERIAL PRIMARY KEY,
	student_id INTEGER NOT NULL REFERENCES students (student_id) ON DELETE CASCADE,
	course_id INTEGER  NOT NULL REFERENCES courses (course_id) ON DELETE CASCADE
);