module com.arvatosystems.t9t.base.jpa {
	exports com.arvatosystems.t9t.base.jpa.entityListeners;
	exports com.arvatosystems.t9t.base.jpa.entities;
	exports com.arvatosystems.t9t.base.jpa.ormspecific;
	exports com.arvatosystems.t9t.base.jpa;
	exports com.arvatosystems.t9t.base.jpa.impl;
	exports com.arvatosystems.t9t.base.jpa.impl.idgenerators;

	requires com.arvatosystems.t9t.base.api;
	requires com.arvatosystems.t9t.base.sapi;
	requires com.arvatosystems.t9t.cfg;
	requires com.google.common;
	requires de.jpaw.annotations;
	requires de.jpaw.bonaparte.api;
	requires de.jpaw.bonaparte.api.media;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.bonaparte.refs;
	requires de.jpaw.jdp;
	requires de.jpaw.jpaw.enums;
	requires de.jpaw.jpaw.json;
	requires de.jpaw.jpaw.util;
	requires de.jpaw.persistence.api;
	requires de.jpaw.persistence.core;
	requires de.jpaw.persistence.refs;
	requires java.persistence;
	requires java.sql;
	requires org.eclipse.xtend.lib;
	requires org.eclipse.xtext.xbase.lib;
	requires org.joda.time;
	requires org.slf4j;
}