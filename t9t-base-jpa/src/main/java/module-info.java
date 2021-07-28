module com.arvatosystems.t9t.base.jpa {
	exports com.arvatosystems.t9t.base.jpa.entityListeners;
	exports com.arvatosystems.t9t.base.jpa.entities;
	exports com.arvatosystems.t9t.base.jpa.ormspecific;
	exports com.arvatosystems.t9t.base.jpa;
	exports com.arvatosystems.t9t.base.jpa.impl;
	exports com.arvatosystems.t9t.base.jpa.impl.idgenerators;

	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.arvatosystems.t9t.base.sapi;
	requires transitive com.arvatosystems.t9t.cfg;
	requires transitive com.google.common;
	requires transitive de.jpaw.annotations;
	requires transitive de.jpaw.bonaparte.api;
	requires transitive de.jpaw.bonaparte.api.media;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.bonaparte.refs;
	requires transitive de.jpaw.jdp;
	requires transitive de.jpaw.jpaw.enums;
	requires transitive de.jpaw.jpaw.json;
	requires transitive de.jpaw.jpaw.util;
	requires transitive de.jpaw.persistence.api;
	requires transitive de.jpaw.persistence.core;
	requires transitive de.jpaw.persistence.refs;
	requires transitive java.persistence;
	requires transitive java.sql;
	requires transitive org.eclipse.xtend.lib;
	requires transitive org.eclipse.xtext.xbase.lib;
	requires transitive org.joda.time;
	requires org.slf4j;
}