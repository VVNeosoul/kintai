package com.example.kintai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class PageResult<T> {
    private final List<T> items;
    private final int page;
    private final int totalPages;
    private final long totalItems;
}