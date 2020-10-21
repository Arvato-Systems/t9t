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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;

import com.arvatosystems.t9t.tfi.web.ApplicationSession;

import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.auth.Permissionset;

public class Permissions28 extends Groupbox {
    private static final long serialVersionUID = 5242996162336572551L;
    private static final Logger LOGGER = LoggerFactory.getLogger(Permissions28.class);

    protected ApplicationSession as = ApplicationSession.get();
    protected final Grid grid;
    protected static final OperationType [] OP_TYPES = {
        OperationType.EXECUTE, OperationType.CREATE,  OperationType.READ,       OperationType.UPDATE,   OperationType.DELETE,
        OperationType.SEARCH,  OperationType.LOOKUP,  OperationType.INACTIVATE, OperationType.ACTIVATE, OperationType.VERIFY,
        OperationType.MERGE,   OperationType.PATCH,   OperationType.EXPORT,     OperationType.IMPORT,   OperationType.CONFIGURE,
        OperationType.CONTEXT, OperationType.ADMIN,   OperationType.APPROVE,    OperationType.REJECT,   OperationType.CUSTOM
    };
    protected final List<Checkbox> checkboxes = new ArrayList<Checkbox>(20);

    private Button createButton(String id, Component parent) {
        Button b = new Button();
        b.setHeight(as.translate(Button28.PREFIX_BUTTON28, "height"));
        b.setWidth (as.translate(Button28.PREFIX_BUTTON28, "width"));
        b.setLabel (as.translate(Button28.PREFIX_BUTTON28, id));
        b.setParent(parent);
        return b;
    }

    // protected boolean sendEvents = true;
    protected Permissionset value = null;

    protected void toggleBit(OperationType op, boolean nowChecked) {
        LOGGER.debug("CheckEvent({}, {}) on {}", op, nowChecked, value);
        if (nowChecked) {
            if (value == null) {
                value = Permissionset.ofTokens(op);
            } else if (!value.contains(op)) {
                value.add(op);
            } else {
                return; // no operation - already set
            }
        } else {
            if (value == null) {
                value = Permissionset.ofTokens();
            } else if (value.contains(op)) {
                value.remove(op);
            } else {
                return; // no operation - already removed
            }
        }
        // a change was done - post an event
        Events.postEvent(Events.ON_CHANGE, this, null);
    }

    public void setValue(Permissionset v) {
        // sendEvents = false;
        value = v;
        if (v == null) {
            for (Checkbox cb : checkboxes) {
                cb.setChecked(false);
                cb.setDisabled(true);
            }
        } else {
            for (int i = 0; i < 20; ++i) {
                Checkbox cb = checkboxes.get(i);
                cb.setDisabled(false);
                cb.setChecked(v.contains(OP_TYPES[i]));
            }
        }
        // sendEvents = true;
    }

    public Permissionset getValue() {
        return value;
    }

    protected void noPadding(HtmlBasedComponent t) {
        t.setStyle("padding: 0;");
    }

    public Permissions28() {
        super();
        // setSclass("sperms");
        setZclass("zperms");
        //setStyle("padding: 3px");
        //setContentStyle("border: 1px #404040");
        // create a grid for the checkboxes and buttons
        grid = new Grid();
        grid.setHflex("1");
        grid.setParent(this);
        noPadding(grid);
        // create columns
        Columns cols = new Columns();
        for (int i = 0; i < 5; ++i) {
            Column col = new Column();
            col.setHflex("1");
            col.setParent(cols);
        }
        cols.setParent(grid);
        Rows rows = new Rows();
        // create 4 rows of checkboxes, 5 checkboxes each
        for (int y = 0; y < 4; ++y) {
            Row r = new Row();
            for (int i = 0; i < 5; ++i) {
                final OperationType op = OP_TYPES[5 * y + i];
                final Checkbox cb = new Checkbox();
                noPadding(cb);
                Cell cell = new Cell();
                cell.setParent(r);
                noPadding(cell);
                cb.setLabel(as.translate("t9t.OperationType", op.name()));
                cb.setParent(cell);
                cb.addEventListener(Events.ON_CHECK, (CheckEvent ev) -> {
                    toggleBit(op, ev.isChecked());
                });
                checkboxes.add(cb);
            }
            r.setParent(rows);
        }
        // plus the row for the buttons
        Row r = new Row();
        Cell cell = new Cell();
        cell.setParent(r);
        cell.setColspan(5); // this is what you're looking for
        Hbox h = new Hbox();
        h.setHflex("1");
        h.setAlign("right");
        // h.setParent(cell);
        cell.appendChild(h);
        r.setParent(rows);
        rows.setParent(grid);
        createButton("setAll", h).addEventListener(Events.ON_CLICK, (ev) -> setValue(new Permissionset(0xfffff)));
        createButton("clrAll", h).addEventListener(Events.ON_CLICK, (ev) -> setValue(new Permissionset(0)));
        createButton("ignore", h).addEventListener(Events.ON_CLICK, (ev) -> setValue(null));
    }
}
