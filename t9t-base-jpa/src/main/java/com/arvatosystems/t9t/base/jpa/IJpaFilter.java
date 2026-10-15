package com.arvatosystems.t9t.base.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import com.arvatosystems.t9t.api.FieldFilter;

public interface IJpaFilter {
    Predicate applyFilter(CriteriaBuilder cb, Path<?> from, FieldFilter f);
}
