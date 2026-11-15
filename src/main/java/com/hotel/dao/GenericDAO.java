package com.hotel.dao;

import java.util.List;

/** Common CRUD contract used by the application's data access classes. */
public interface GenericDAO<T> {
    boolean add(T item);
    boolean update(T item);
    boolean delete(int id);
    List<T> getAll();
}
