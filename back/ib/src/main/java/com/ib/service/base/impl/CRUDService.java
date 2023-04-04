package com.ib.service.base.impl;

import com.ib.service.base.interfaces.ICRUDService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public abstract class CRUDService<T> implements ICRUDService<T> {

    protected abstract JpaRepository<T, Integer> getEntityRepository();

    @Override
    public List<T> getAll() {
        return getEntityRepository().findAll();
    }

    @Override
    public T get(Integer id) throws EntityNotFoundException {
        return findEntityChecked(id);
    }

    @Override
    public T save(T entity) {
        return getEntityRepository().save(entity);
    }

    @Override
    public T update(T entity) {
        return save(entity);
    }

    @Override
    public void delete(Integer id) throws EntityNotFoundException {
        findEntityChecked(id);
        getEntityRepository().deleteById(id);
    }

    private T findEntityChecked(Integer id) throws EntityNotFoundException {
        return getEntityRepository().findById(id).orElseThrow(() -> new EntityNotFoundException("Cannot find entity with id: " + id));
    }
}
