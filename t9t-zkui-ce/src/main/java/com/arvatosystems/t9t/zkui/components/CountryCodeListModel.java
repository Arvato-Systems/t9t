package com.arvatosystems.t9t.zkui.components;

import com.arvatosystems.t9t.zkui.util.Constants;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;

import java.util.List;

@Singleton
@Named("countryCode")
public class CountryCodeListModel implements IStringListModel {

    @Nonnull
    @Override
    public List<String> getListModel() {
        return Constants.COUNTRY_MODEL_DATA;
    }
}
