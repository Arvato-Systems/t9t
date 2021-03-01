/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.dataloader;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import javax.xml.bind.DatatypeConverter;

public class DbTypeConverter {

    /**
     * Translates a data type from an integer (java.sql.Types value) to a string that represents the corresponding class.
     *
     * @param type The java.sql.Types value to convert to a string representation.
     * @return The class name that corresponds to the given java.sql.Types value, or "java.lang.Object" if the type has no known mapping.
     * @throws ConvertException
     */
    public static Object convert(int type, String value, String pattern) throws ConvertException {
        Object result = null;

        switch (type) {
        case Types.CHAR:
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
        case Types.NVARCHAR:
        case Types.LONGNVARCHAR:
            result = parseString(value);
            break;

        case Types.NUMERIC:
        case Types.DECIMAL:
        case Types.REAL:
        case Types.FLOAT:
        case Types.DOUBLE:
            result = parseBigDecimal(value);
            break;

        case Types.BIT:
            result = parseBoolean(value);
            break;

        case Types.TINYINT:
        case Types.INTEGER:
            result = parseInteger(value);
            break;

        case Types.BIGINT:
            result = parseLong(value);
            break;

        case Types.BINARY:
        case Types.LONGVARBINARY:
        case Types.VARBINARY:
        case Types.BLOB:
            if (!Util.isBlank(value)) {
                result = DatatypeConverter.parseBase64Binary(value);
            }
            break;

        case Types.DATE:
            result = parseDate(value, pattern);
            break;

        case Types.TIME:
            result = parseTime(value, pattern);
            break;

        case Types.TIMESTAMP:
            result = parseTimestamp(value, pattern);
            break;

        case Types.OTHER:
            result = parseUUID(value);
        }

        return result;
    }

    public static Object getDefault(int type) throws ConvertException {
        Object result = null;

        switch (type) {
        case Types.CHAR:
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
        case Types.NVARCHAR:
        case Types.LONGNVARCHAR:
            result = "";
            break;

        case Types.NUMERIC:
        case Types.DECIMAL:
        case Types.REAL:
        case Types.FLOAT:
        case Types.DOUBLE:
            result = new BigDecimal(0);
            break;

        case Types.BIT:
            result = Boolean.TRUE;
            break;

        case Types.TINYINT:
        case Types.INTEGER:
            result = new Integer(0);
            break;

        case Types.BIGINT:
            result = new Long(0);
            break;

        case Types.BINARY:
        case Types.VARBINARY:
        case Types.LONGVARBINARY:
        case Types.BLOB:
            result = DatatypeConverter.parseBase64Binary("");
            break;

        case Types.DATE:
            result = new java.sql.Date(System.currentTimeMillis());
            break;

        case Types.TIME:
            result = new java.sql.Time(System.currentTimeMillis());
            break;

        case Types.TIMESTAMP:
            result = new java.sql.Timestamp(System.currentTimeMillis());
            break;
        }

        return result;
    }

    public static String convertToString(ResultSet resultSet, String columnName) throws SQLException {
        Object value = resultSet.getObject(columnName);

        if (value instanceof byte[]) {
            return DatatypeConverter.printBase64Binary((byte[]) value);
        } else {
            return String.valueOf(value);
        }
    }

    private static java.sql.Date parseDate(String inputValue, String pattern) throws ConvertException {
        if (Util.isBlank(inputValue)) {
            return null;
        }
        return new java.sql.Date(parseUtilDate(inputValue, pattern).getTime());
    }

    private static java.sql.Time parseTime(String inputValue, String pattern) throws ConvertException {
        if (Util.isBlank(inputValue)) {
            return null;
        }
        return new java.sql.Time(parseUtilDate(inputValue, pattern).getTime());
    }

    private static java.sql.Timestamp parseTimestamp(String inputValue, String pattern) throws ConvertException {
        if (Util.isBlank(inputValue)) {
            return null;
        }
        return new java.sql.Timestamp(parseUtilDate(inputValue, pattern).getTime());
    }

    private static Date parseUtilDate(String inputValue, String pattern) throws ConvertException {
        try {
            DateFormat inputFormat = new SimpleDateFormat(pattern);
            return inputFormat.parse(inputValue);
        } catch (Exception ex) {
            throw new ConvertException("Can't parse Date value: " + inputValue + " with pattern " + pattern);
        }
    }

    private static Integer parseInteger(String inputValue) throws ConvertException {
        Integer result = null;
        if (!Util.isBlank(inputValue)) {
            try {
                return new Integer(inputValue);
            } catch (NumberFormatException ex) {
                throw new ConvertException("Can't parse Integer value: " + inputValue);
            }
        }
        return result;

    }

    private static BigDecimal parseBigDecimal(String inputValue) throws ConvertException {
        BigDecimal result = null;
        if (!Util.isBlank(inputValue)) {
            try {
                result = new BigDecimal(inputValue.replace(',', '.').replace("true", "1").replace("false", "0")
                        .replace("TRUE", "1").replace("FALSE", "0"));
            } catch (Exception e) {
                throw new ConvertException("Can't parse BigDecimal value: " + inputValue);
            }
        }
        return result;
    }

    private static Long parseLong(String inputValue) throws ConvertException {
        Long result = null;
        if (!Util.isBlank(inputValue)) {
            try {
                result = new Long(inputValue);
            } catch (NumberFormatException ex) {
                throw new ConvertException("Can't parse Long value: " + inputValue);
            }
        }
        return result;
    }

    private static Boolean parseBoolean(String booleanAsText) throws ConvertException {
        Boolean result = null;
        String value = Util.clean(booleanAsText);
        if (value.isEmpty()) {
            // do nothing - return null
        } else if (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("no") || value.equalsIgnoreCase("off") || value.equalsIgnoreCase("0")) {
            result = Boolean.FALSE;
        } else if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("on") || value.equalsIgnoreCase("1")) {
            result = Boolean.TRUE;
        } else {
            throw new ConvertException("Cannot parse Boolean value: " + booleanAsText + ". Accepted values are: true/false/yes/no/on/off/1/0");
        }
        return result;
    }

    private static String parseString(String inputValue) throws ConvertException {
        String result = Util.clean(inputValue);
        if (result.isEmpty()) {
            return null;
        } else {
            return result;
        }
    }

    private static Object parseUUID(String inputValue) throws ConvertException {
        try {
            return UUID.fromString(inputValue);
        } catch (IllegalArgumentException e) {
            return parseString(inputValue);
        }
    }
}
