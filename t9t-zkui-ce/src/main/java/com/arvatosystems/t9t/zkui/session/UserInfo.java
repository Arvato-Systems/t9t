package com.arvatosystems.t9t.zkui.session;

import java.util.Locale;
import java.util.TimeZone;

public record UserInfo(String screenInfo, String browserTz, Locale locale, TimeZone zkTz) {
}
