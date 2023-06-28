/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.zkui.viewmodel.support;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.QueryParam;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Div;
import org.zkoss.zul.ListModelList;

import com.arvatosystems.t9t.zkui.components.dropdown28.db.Dropdown28Db;
import com.arvatosystems.t9t.zkui.services.ISelectionsResolver;
import com.arvatosystems.t9t.zkui.util.Constants;
import com.arvatosystems.t9t.zkui.util.ZulUtils;

import de.jpaw.dp.Jdp;

public class SelectionsViewModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(SelectionsViewModel.class);
    private ISelectionsResolver selectionResolver;
    private String selected;
    private String title;

    @Wire("#div")
    private Div div;

    @AfterCompose
    public void afterCompose(@ContextParam(ContextType.VIEW) final Component view,
            @QueryParam("qualifier") final String qualifier) {
        Selectors.wireComponents(view, this, false);
        if (qualifier == null) {
            LOGGER.debug("No additional selections required, redirect to homepage.");
            // no additional selections required, redirect to homepage
            Executions.getCurrent().sendRedirect(Constants.ZulFiles.HOME);
            return;
        }

        LOGGER.debug("Resolving additional selections with qualifier {}", qualifier);
        selectionResolver = Jdp.getRequired(ISelectionsResolver.class, qualifier);
        title = ZulUtils.translate("selections", qualifier);
        final Dropdown28Db<?> dropdownComponent = selectionResolver.getDropdownComponent();
        final List<String> selections = selectionResolver.getSelections();
        if (dropdownComponent != null) {
            // use dropdown28db
            dropdownComponent.setParent(div);
            dropdownComponent.setFocus(true);
            dropdownComponent.addEventListener(Events.ON_OK, (e) -> this.submit());
            dropdownComponent.addEventListener(Events.ON_CHANGE, (e) -> {
                final InputEvent ie = (InputEvent) e;
                selected = ie.getValue();
            });
            if (dropdownComponent.getItemCount() == 0) {
                setDefaultAndGotoNext(qualifier);
            }
        } else if (selections != null) {
            createBasicComboBox(div, selections);
            if (selections.isEmpty()) {
                setDefaultAndGotoNext(qualifier);
            }
        } else {
            throw new UnsupportedOperationException("Either getDropdownComponent or getSelections must be provided.");
        }
    }

    /**
     * Usually call when there is no selections,
     * get value from login.additional.selections.<qualifier>.defaultIfEmpty
     *
     *
     * @param qualifier
     */
    private void setDefaultAndGotoNext(final String qualifier) {
        final String configKey = "login.additional.selections." + qualifier + ".defaultIfEmpty";
        final String value = ZulUtils.readConfig(configKey);
        if (value != null) {
            selectionResolver.setSelection(value);
            gotoNextScreen();
        }
    }

    @Command
    public final void submit() {
        selectionResolver.setSelection(selected);
        gotoNextScreen();
    }

    /**
     * Go to next screen that defined in the selectionResolver,
     * if null, jump to homepage
     */
    private void gotoNextScreen() {
        final String nextScreen = selectionResolver.getNextScreen();

        if (nextScreen == null) {
            LOGGER.debug("No next screen configured, redirect to homepage.");
            Executions.getCurrent().sendRedirect(Constants.ZulFiles.HOME);
            return;
        }
        final String url = Constants.ZulFiles.ADDITIONAL_SELECTIONS + "?qualifier=" + nextScreen;
        LOGGER.info("redirecting to selections page with {} qualifier, complete url as {}", nextScreen, url);
        Executions.getCurrent().sendRedirect(url);
    }

    /**
     * create a combobox for the selections
     */
    private Combobox createBasicComboBox(final Component parent, final List<String> selections) {
        final Combobox c = new Combobox();
        c.setModel(new ListModelList<String>(selections));
        c.setFocus(true);
        c.addEventListener(Events.ON_CHANGE, (e) -> {
            final InputEvent ie = (InputEvent) e;
            selected = ie.getValue();
        });
        c.addEventListener(Events.ON_OK, (e) -> this.submit());
        LOGGER.debug("retrieved {} of selections from selections resolver.", selections.size());
        c.setParent(parent);
        return c;
    }

    public String getSelected() {
        return selected;
    }

    public void setSelected(final String selected) {
        this.selected = selected;
    }

    public String getTitle() {
        return title;
    }
}
