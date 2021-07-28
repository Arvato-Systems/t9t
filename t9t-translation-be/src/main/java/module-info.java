module com.arvatosystems.t9t.translation.be {
	exports com.arvatosystems.t9t.translation.be;
	exports com.arvatosystems.t9t.translation.be.importer;

	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.arvatosystems.t9t.init;
	requires transitive com.arvatosystems.t9t.translation.sapi;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.jdp;
	requires transitive de.jpaw.jpaw.enums;
	requires transitive de.jpaw.jpaw.util;
	requires org.slf4j;
}