package org.example;

import com.zaxxer.hikari.HikariDataSource;
import org.example.dao.CourseDao;
import org.example.dao.GroupDao;
import org.example.dao.StudentDao;
import org.example.runner.DbLoader;
import org.example.runner.Repl;
import org.example.service.DatabaseManager;
import org.example.service.ReportGenerator;
import org.example.utils.DbUtils;

public class Main {
	public static void main(String[] args) {
		try (HikariDataSource ds = DbUtils.createDataSource()) {
			DbLoader.load(ds);

			StudentDao studentDao = new StudentDao(ds);
			GroupDao groupDao = new GroupDao(ds);
			CourseDao courseDao = new CourseDao(ds);

			ReportGenerator reportGenerator = new ReportGenerator(studentDao, groupDao, courseDao);
			DatabaseManager dbManager = new DatabaseManager(reportGenerator);
			Repl repl = new Repl(dbManager);

			repl.run();
		}
	}
}
