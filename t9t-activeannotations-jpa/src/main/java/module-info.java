module com.arvatosystems.t9t.annotations.jpa.active {
	exports com.arvatosystems.t9t.annotations.jpa.active;

	requires transitive com.arvatosystems.t9t.annotations;
	requires transitive com.arvatosystems.t9t.annotations.jpa;
	requires transitive com.google.common;
	requires transitive de.jpaw.bonaparte.api;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.jdp;
	requires transitive de.jpaw.jpaw.util;
	requires transitive de.jpaw.persistence.core;
	requires transitive java.persistence;
	requires transitive org.eclipse.xtend.lib.macro;
	requires transitive org.eclipse.xtext.xbase.lib;
	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.arvatosystems.t9t.base.jpa;
	requires transitive com.arvatosystems.t9t.base.sapi;
}