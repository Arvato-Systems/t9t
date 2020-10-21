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
package com.arvatosystems.t9t.components;

import org.zkoss.zul.Cell;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;

/**
 * An empty row, just to pad space.
 */
public class Cells8 extends Row {
    private static final long serialVersionUID = -7701935513940L;
    protected String cellHeight = "32px";

    public Cells8() {
        super();
        Cell cell1 = new Cell();
        cell1.setParent(this);
        Label label = new Label();
        label.setParent(cell1);
        if (cellHeight != null) {
            cell1.setHeight(cellHeight);
        }
    }
}
