/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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
package com.arvatosystems.t9t.tfi.component;

public interface Converter {

    /**
     * This method is responsible to print the label in the correct way.<br>
     * e.g.:
     * <ul>
     * <li>Object like: Person</li>
     * <li>With properties: gender (Integer) 0=male / 1=female</li>
     * <li>the following value would be passed</li>
     * <ul>
     * <li>value: Integer(1)</li>
     * <li>wholeDataObject: Person-object</li>
     * <li>fieldName: gender</li>
     * </ul>
     * <li>Return: the string "Female"</li>
     * </ul>
     * @param value The value itself that should be formatted
     * @param wholeDataObject the whole Data Object will be passed for cases if you need additional values from other fields you want to combine
     * @param fieldName the property name of the object
     * @return your formatted label as String
     */
    public String getFormattedLabel(Object value, Object wholeDataObject, String fieldName);

    /**
     * This method is responsible to give back the correct object that the listbox can deal with it in a proper way like sorting, etc.<br>
     * e.g.:
     * <ul>
     * <li>Object like: Person</li>
     * <li>With properties: gender (Integer) 0=male / 1=female</li>
     * <li>the following value would be passed</li>
     * <ul>
     * <li>value: Integer(1)</li>
     * <li>wholeDataObject: Person-object</li>
     * <li>fieldName: gender</li>
     * </ul>
     * <li>Return: a the converted object like String("F")</li>
     * </ul>
     * @param value The value itself that should be formatted
     * @param wholeDataObject the whole Data Object will be passed for cases if you need additional values from other fields you want to combine
     * @param fieldName the property name of the object
     * @return your formatted label as String
     */
    public Object getConvertedValue(Object value, Object wholeDataObject, String fieldName);

}
