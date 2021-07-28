module com.arvatosystems.t9t.genconf.jpa {
	exports com.arvatosystems.t9t.genconf.jpa.persistence;
	exports com.arvatosystems.t9t.uiprefsv3.jpa.mapping;
	exports com.arvatosystems.t9t.uiprefsv3.jpa.entities;
	exports com.arvatosystems.t9t.uiprefsv3.jpa.mapping.impl;
	exports com.arvatosystems.t9t.uiprefsv3.jpa.persistence.impl;
	exports com.arvatosystems.t9t.genconf.jpa.mapping.impl;
	exports com.arvatosystems.t9t.uiprefsv3.jpa.impl;
	exports com.arvatosystems.t9t.genconf.jpa.entities;
	exports com.arvatosystems.t9t.genconf.jpa.persistence.impl;
	exports com.arvatosystems.t9t.genconf.jpa.request;
	exports com.arvatosystems.t9t.uiprefsv3.jpa.persistence;
	exports com.arvatosystems.t9t.genconf.jpa.mapping;

	requires transitive com.arvatosystems.t9t.annotations.jpa;
	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.arvatosystems.t9t.base.jpa;
	requires transitive com.arvatosystems.t9t.base.sapi;
	requires transitive com.arvatosystems.t9t.genconf.api;
	requires transitive com.arvatosystems.t9t.genconf.sapi;
	requires transitive de.jpaw.annotations;
	requires transitive de.jpaw.bonaparte.api;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.bonaparte.refs;
	requires transitive de.jpaw.jdp;
	requires transitive de.jpaw.jpaw.enums;
	requires transitive de.jpaw.jpaw.util;
	requires transitive de.jpaw.persistence.core;
	requires transitive de.jpaw.persistence.refs;
	requires transitive java.persistence;
	requires transitive java.validation;
	requires transitive org.joda.time;
	requires org.slf4j;
}