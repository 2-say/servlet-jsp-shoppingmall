package com.nhnacademy.shoppingmall.domain.category.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class Categories {
    private final List<Category> categories;
    private final String parentName;
}
