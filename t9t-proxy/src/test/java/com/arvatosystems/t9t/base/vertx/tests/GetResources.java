package com.arvatosystems.t9t.base.vertx.tests;

import java.net.URL;
import org.junit.jupiter.api.Test;

public class GetResources {
    @Test
    public void getIcon() throws Exception {
        final URL icon = GetResources.class.getResource("/web/favicon.ico");

        if (icon == null)
            throw new Exception("not found");
    }
}
