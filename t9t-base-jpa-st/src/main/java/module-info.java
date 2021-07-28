module com.arvatosystems.t9t.base.jpa.st {
	exports com.arvatosystems.t9t.base.jpa.st;
	exports com.arvatosystems.t9t.base.jpa.st.util;
	exports com.arvatosystems.t9t.base.jpa.st.impl;

	requires c3p0;
	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.arvatosystems.t9t.base.jpa;
	requires transitive com.arvatosystems.t9t.base.sapi;
	requires transitive com.arvatosystems.t9t.cfg;
	requires transitive de.jpaw.bonaparte.api;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.bonaparte.refs;
	requires transitive de.jpaw.jdp;
	requires transitive de.jpaw.persistence.refs;
	requires java.desktop;
	requires java.naming;
	requires java.sql;
	requires java.activation;
	requires transitive java.persistence;
	requires org.hibernate.orm.core;
	requires org.slf4j;
	requires spring.beans;
	requires spring.context;
	requires spring.jdbc;
	requires spring.orm;
	requires spring.tx;
}
