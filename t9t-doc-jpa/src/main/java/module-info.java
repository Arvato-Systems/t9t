module com.arvatosystems.t9t.doc.jpa {
	exports com.arvatosystems.t9t.doc.jpa.mapping;
	exports com.arvatosystems.t9t.doc.jpa.api;
	exports com.arvatosystems.t9t.doc.jpa.request;
	exports com.arvatosystems.t9t.doc.jpa.impl;
	exports com.arvatosystems.t9t.doc.jpa.persistence.impl;
	exports com.arvatosystems.t9t.doc.jpa.entities;
	exports com.arvatosystems.t9t.doc.jpa.mapping.impl;
	exports com.arvatosystems.t9t.doc.jpa.persistence;

	requires transitive com.arvatosystems.t9t.annotations.jpa;
	requires transitive com.arvatosystems.t9t.auth.apiext;
	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.arvatosystems.t9t.base.jpa;
	requires transitive com.arvatosystems.t9t.base.sapi;
	requires transitive com.arvatosystems.t9t.core.jpa;
	requires transitive com.arvatosystems.t9t.doc.api;
	requires transitive com.arvatosystems.t9t.doc.sapi;
	requires transitive com.arvatosystems.t9t.email.api;
	requires transitive com.arvatosystems.t9t.server;
	requires transitive com.google.common;
	requires transitive de.jpaw.annotations;
	requires transitive de.jpaw.bonaparte.api;
	requires transitive de.jpaw.bonaparte.api.media;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.bonaparte.refs;
	requires transitive de.jpaw.jdp;
	requires transitive de.jpaw.jpaw.enums;
	requires transitive de.jpaw.jpaw.util;
	requires transitive de.jpaw.persistence.core;
	requires transitive de.jpaw.persistence.refs;
	requires transitive java.persistence;
	requires transitive java.validation;
	requires transitive org.eclipse.xtext.xbase.lib;
	requires transitive org.joda.time;
	requires org.slf4j;
}