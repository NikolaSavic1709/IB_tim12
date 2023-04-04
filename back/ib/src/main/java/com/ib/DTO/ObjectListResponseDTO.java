package com.ib.DTO;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
public class ObjectListResponseDTO<T> {

    private Integer totalCount;
    private List<T> results;
}
