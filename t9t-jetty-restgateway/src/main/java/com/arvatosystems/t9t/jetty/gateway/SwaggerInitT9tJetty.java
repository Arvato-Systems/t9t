package com.arvatosystems.t9t.jetty.gateway;

import com.arvatosystems.t9t.jetty.impl.SwaggerInit;

import de.jpaw.dp.Singleton;
import de.jpaw.dp.Specializes;
import io.swagger.v3.oas.models.info.Info;

@Specializes
@Singleton
public class SwaggerInitT9tJetty extends SwaggerInit {
    @Override
    protected Info createRestApiInfoForSwagger() {
        return super.createRestApiInfoForSwagger().version("t9t API 6.2.0");
    }
}
