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

	requires com.arvatosystems.t9t.annotations.jpa;
	requires com.arvatosystems.t9t.base.api;
	requires com.arvatosystems.t9t.base.jpa;
	requires com.arvatosystems.t9t.base.sapi;
	requires com.arvatosystems.t9t.genconf.api;
	requires com.arvatosystems.t9t.genconf.sapi;
	requires de.jpaw.annotations;
	requires de.jpaw.bonaparte.api;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.bonaparte.refs;
	requires de.jpaw.jdp;
	requires de.jpaw.jpaw.enums;
	requires de.jpaw.jpaw.util;
	requires de.jpaw.persistence.core;
	requires de.jpaw.persistence.refs;
	requires java.persistence;
	requires java.validation;
	requires org.joda.time;
	requires org.slf4j;
}