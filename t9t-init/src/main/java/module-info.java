module com.arvatosystems.t9t.init {
	exports com.arvatosystems.t9t.init;

	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.google.common;
	requires transitive de.jpaw.bonaparte.api;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.jpaw.enums;
	requires transitive de.jpaw.jpaw.util;
	requires transitive de.jpaw.jpaw.xenuminit;
	requires transitive org.joda.time;
	requires org.slf4j;
	requires transitive reflections;
}