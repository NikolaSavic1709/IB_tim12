package com.ib.service.base.interfaces;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface IJPAService<T> extends ICRUDService<T> {
    Iterable<T> getAll(Sort sorter);

    Page<T> getAll(Pageable page);

}
