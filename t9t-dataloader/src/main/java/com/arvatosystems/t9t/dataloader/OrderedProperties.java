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
package com.arvatosystems.t9t.dataloader;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

public class OrderedProperties extends Properties {

    private static final long serialVersionUID = -8322955026256703344L;

    private Vector<Object> keys;
    private StringBuffer raw = new StringBuffer();
    private final DateFormat datef = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public OrderedProperties() {
        super();

        keys = new Vector<Object>();
    }

    @Override
    public Enumeration<Object> propertyNames() {
        return keys.elements();
    }

    @Override
    public Set<Object> keySet() {
        return new TreeSet<Object>(keys);
    }

    @Override
    public Object put(Object key, Object value) {
        if (keys.contains(key)) {
            // keys.remove(key);
            raw.append("#").append(datef.format(new Date())).append(" -REP-> ").append(key).append(" = ").append(value).append(" [OLD: ").append(get(key))
                    .append("]\n");
        } else {
            raw.append("#").append(datef.format(new Date())).append(" -NEW-> ").append(key).append(" = ").append(value).append("\n");
            keys.add(key);
        }

        return super.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        keys.remove(key);

        return super.remove(key);
    }

    public String getRAW() {
        return raw != null ? (raw.toString() + "#Order: " + this.keys) : null;
    }

    public void setRAW(String raw) {
        this.raw = new StringBuffer(raw);
    }

}
