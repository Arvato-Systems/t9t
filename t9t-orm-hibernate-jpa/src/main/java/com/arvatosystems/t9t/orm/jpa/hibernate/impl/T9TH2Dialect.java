/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arvatosystems.t9t.orm.jpa.hibernate.impl;

import java.sql.Types;
import org.hibernate.HibernateException;
import org.hibernate.dialect.H2Dialect;

/**
 * Override the H2 Dialect
 *
 */
public class T9TH2Dialect extends H2Dialect {

    private static final long H2_MAX_VARCHAR_LENGTH = 1048576L;
    private static final long H2_MAX_VARBINARY_LENGTH = 1048576L;

    public T9TH2Dialect() {
        super();
        registerColumnType(Types.BINARY, "varbinary");
    }

    @Override
    public String getTypeName(int code, long length, int precision, int scale) throws HibernateException {
        /**
         * fix the error from H2 due to some of the String on @Column(length) is too
         * large for h2
         */
        if (Types.VARCHAR == code && length > H2_MAX_VARCHAR_LENGTH) {
            return super.getTypeName(code, H2_MAX_VARCHAR_LENGTH, precision, scale);
        } else if (Types.VARBINARY == code && length > H2_MAX_VARBINARY_LENGTH) {
            return super.getTypeName(code, H2_MAX_VARBINARY_LENGTH, precision, scale);
        }
        return super.getTypeName(code, length, precision, scale);
    }
}
