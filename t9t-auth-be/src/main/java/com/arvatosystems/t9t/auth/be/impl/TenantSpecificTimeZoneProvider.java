package com.arvatosystems.t9t.auth.be.impl;

import java.time.ZoneId;
import java.time.ZoneOffset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.auth.services.ITenantResolver;
import com.arvatosystems.t9t.base.be.stubs.NoTimeZoneProvider;
import com.arvatosystems.t9t.base.services.ITimeZoneProvider;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

/**
 * Provides a time zone.
 *
 * This implementation determines the time zone specific to the tenant.
 *
 * @see NoTimeZoneProvider
 */
@Singleton
public class TenantSpecificTimeZoneProvider implements ITimeZoneProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(TenantSpecificTimeZoneProvider.class);
    private final ITenantResolver tenantResolver = Jdp.getRequired(ITenantResolver.class);

    @Override
    public ZoneId getTimeZoneOfTenant(Long tenantRef) {
        final String zoneName = tenantResolver.getDTO(tenantRef).getTimeZone();
        if (zoneName == null) {
            return ZoneOffset.UTC;
        }
        try {
            return ZoneId.of(zoneName);
        } catch (Exception e) {
            LOGGER.error("Could not process time zone {}, falling back to UTC: {} {}", zoneName, e.getClass().getSimpleName(), e.getMessage());
            return ZoneOffset.UTC;
        }
    }
}
