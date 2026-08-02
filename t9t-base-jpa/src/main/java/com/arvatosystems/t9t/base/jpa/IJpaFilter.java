package com.arvatosystems.t9t.base.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import de.jpaw.bonaparte.pojos.api.FieldFilter;

public interface IJpaFilter {
    Predicate applyFilter(CriteriaBuilder cb, Path<?> from, FieldFilter f);
}
