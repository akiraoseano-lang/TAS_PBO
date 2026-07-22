package com.project.tas_pbo.DAO;

import com.project.tas_pbo.model.BaseModel;
import java.util.List;

public interface ICrudDAO<T extends BaseModel> {
    List<T> getAll();
    T getById(int id);
    boolean add(T entity);
    boolean update(T entity);
    boolean delete(int id);
}
