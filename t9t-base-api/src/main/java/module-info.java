module com.arvatosystems.t9t.base.api {
	exports com.arvatosystems.t9t.base.trns;
	exports com.arvatosystems.t9t.base.crud;
	exports com.arvatosystems.t9t.base.uiprefs;
	exports com.arvatosystems.t9t.base.moduleCfg;
	exports com.arvatosystems.t9t.base.event;
	exports com.arvatosystems.t9t.base;
	exports com.arvatosystems.t9t.base.request;
	exports com.arvatosystems.t9t.base.output;
	exports com.arvatosystems.t9t.base.auth;
	exports com.arvatosystems.t9t.base.misc;
	exports com.arvatosystems.t9t.base.entities;
	exports com.arvatosystems.t9t.base.types;
	exports com.arvatosystems.t9t.base.api;
	exports com.arvatosystems.t9t.base.search;

	requires com.arvatosystems.t9t.annotations;
	requires com.google.common;
	requires de.jpaw.bonaparte.api;
	requires de.jpaw.bonaparte.api.auth;
	requires de.jpaw.bonaparte.api.media;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.jpaw.enums;
	requires de.jpaw.jpaw.util;
	requires org.joda.time;
	requires org.slf4j;
}