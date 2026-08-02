package com.arvatosystems.t9t.base.jpa;

import jakarta.persistence.criteria.Path;

public interface IJpaPathResolver {
    Path<?> getPath(String fieldName);
}
