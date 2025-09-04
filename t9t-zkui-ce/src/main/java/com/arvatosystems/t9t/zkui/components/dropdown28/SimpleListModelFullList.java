package com.arvatosystems.t9t.zkui.components.dropdown28;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.SimpleListModel;

public class SimpleListModelFullList<E> extends SimpleListModel<E> {

    private static final long serialVersionUID = -4934843732616895559L;
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleListModelFullList.class);

    public SimpleListModelFullList(final List<E> data) {
        super(data);
        LOGGER.debug("created SimpleListModelFullList for {} entries", data.size());
    }

    /**
     * Always returns the full model, ignoring filtering and nRows.
     */
    @Override
    public ListModel<E> getSubModel(final Object value, final int nRows) {
        LOGGER.debug("getSubModel called, returning full list (ignoring filter and nRows)");
        return this;
    }
}
