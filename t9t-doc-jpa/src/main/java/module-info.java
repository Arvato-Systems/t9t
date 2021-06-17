module com.arvatosystems.t9t.doc.jpa {
	exports com.arvatosystems.t9t.doc.jpa.mapping;
	exports com.arvatosystems.t9t.doc.jpa.api;
	exports com.arvatosystems.t9t.doc.jpa.request;
	exports com.arvatosystems.t9t.doc.jpa.impl;
	exports com.arvatosystems.t9t.doc.jpa.persistence.impl;
	exports com.arvatosystems.t9t.doc.jpa.entities;
	exports com.arvatosystems.t9t.doc.jpa.mapping.impl;
	exports com.arvatosystems.t9t.doc.jpa.persistence;

	requires com.arvatosystems.t9t.annotations.jpa;
	requires com.arvatosystems.t9t.auth.apiext;
	requires com.arvatosystems.t9t.base.api;
	requires com.arvatosystems.t9t.base.jpa;
	requires com.arvatosystems.t9t.base.sapi;
	requires com.arvatosystems.t9t.core.jpa;
	requires com.arvatosystems.t9t.doc.api;
	requires com.arvatosystems.t9t.doc.sapi;
	requires com.arvatosystems.t9t.email.api;
	requires com.arvatosystems.t9t.server;
	requires com.google.common;
	requires de.jpaw.annotations;
	requires de.jpaw.bonaparte.api;
	requires de.jpaw.bonaparte.api.media;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.bonaparte.refs;
	requires de.jpaw.jdp;
	requires de.jpaw.jpaw.enums;
	requires de.jpaw.jpaw.util;
	requires de.jpaw.persistence.core;
	requires de.jpaw.persistence.refs;
	requires java.persistence;
	requires java.validation;
	requires org.eclipse.xtext.xbase.lib;
	requires org.joda.time;
	requires org.slf4j;
}