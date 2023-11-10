package org.example.dao;

import java.util.List;
import java.util.Optional;

public interface Dao<T> {
	void save(T entity);

	Optional<T> findByID(int id);

	List<T> findAll();

	void update(T entity);

	void delete(T entity);
}
