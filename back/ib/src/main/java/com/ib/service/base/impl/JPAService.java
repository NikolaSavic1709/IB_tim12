package com.ib.service.base.impl;

import com.ib.service.base.interfaces.IJPAService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public abstract class JPAService<T> extends CRUDService<T> implements IJPAService<T> {
    @Override
    public Iterable<T> getAll(Sort sorter) {
            return getEntityRepository().findAll(sorter);
            }

    @Override
    public Page<T> getAll(Pageable page) {
            return getEntityRepository().findAll(page);
    }
}
