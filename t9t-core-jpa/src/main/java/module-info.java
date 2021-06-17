module com.arvatosystems.t9t.core.jpa {
	exports com.arvatosystems.t9t.core.jpa.persistence.impl;
	exports com.arvatosystems.t9t.batch.jpa.persistence;
	exports com.arvatosystems.t9t.bucket.jpa.request;
	exports com.arvatosystems.t9t.event.jpa.persistence.impl;
	exports com.arvatosystems.t9t.plugins.jpa.impl;
	exports com.arvatosystems.t9t.statistics.service.impl;
	exports com.arvatosystems.t9t.core.jpa.entities;
	exports com.arvatosystems.t9t.batch.jpa.mapping.impl;
	exports com.arvatosystems.t9t.core.jpa.mapping.impl;
	exports com.arvatosystems.t9t.batch.jpa.persistence.impl;
	exports com.arvatosystems.t9t.event.jpa.mapping.impl;
	exports com.arvatosystems.t9t.bucket.jpa.impl;
	exports com.arvatosystems.t9t.event.jpa.entities;
	exports com.arvatosystems.t9t.batch.jpa.request;
	exports com.arvatosystems.t9t.plugins.jpa.request;
	exports com.arvatosystems.t9t.bucket.jpa.mapping;
	exports com.arvatosystems.t9t.event.jpa.mapping;
	exports com.arvatosystems.t9t.batch.jpa.entities;
	exports com.arvatosystems.t9t.event.jpa.impl;
	exports com.arvatosystems.t9t.bucket.jpa.mapping.impl;
	exports com.arvatosystems.t9t.core.jpa.mapping;
	exports com.arvatosystems.t9t.bucket.jpa.persistence;
	exports com.arvatosystems.t9t.plugins.jpa.mapping.impl;
	exports com.arvatosystems.t9t.plugins.jpa.persistence.impl;
	exports com.arvatosystems.t9t.bucket.jpa.persistence.impl;
	exports com.arvatosystems.t9t.plugins.jpa.entities;
	exports com.arvatosystems.t9t.core.jpa.impl;
	exports com.arvatosystems.t9t.event.jpa.request;
	exports com.arvatosystems.t9t.event.jpa.persistence;
	exports com.arvatosystems.t9t.batch.jpa.impl;
	exports com.arvatosystems.t9t.plugins.jpa.mapping;
	exports com.arvatosystems.t9t.plugins.jpa.persistence;
	exports com.arvatosystems.t9t.core.jpa.request;
	exports com.arvatosystems.t9t.batch.jpa.mapping;
	exports com.arvatosystems.t9t.bucket.jpa.entities;
	exports com.arvatosystems.t9t.core.jpa.persistence;

	requires com.arvatosystems.t9t.annotations.jpa;
	requires com.arvatosystems.t9t.base.api;
	requires com.arvatosystems.t9t.base.jpa;
	requires com.arvatosystems.t9t.base.sapi;
	requires com.arvatosystems.t9t.core.api;
	requires com.arvatosystems.t9t.core.sapi;
	requires com.arvatosystems.t9t.server;
	requires com.arvatosystems.t9t.cfg;  // FIXME: remove this, only needed in a single place, replace by lookup method
	requires com.google.common;
	requires de.jpaw.annotations;
	requires de.jpaw.bonaparte.api;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.bonaparte.refs;
	requires de.jpaw.jdp;
	requires de.jpaw.jpaw.enums;
	requires de.jpaw.jpaw.util;
	requires de.jpaw.persistence.core;
	requires de.jpaw.persistence.refs;
	requires java.validation;
	requires java.persistence;
	requires org.eclipse.xtext.xbase.lib;
	requires org.joda.time;
	requires org.slf4j;
}