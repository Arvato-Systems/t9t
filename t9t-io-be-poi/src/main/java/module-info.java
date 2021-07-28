module com.arvatosystems.t9t.io.be.poi {
	exports com.arvatosystems.t9t.out.be.impl.poi.formatgenerator;

	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.arvatosystems.t9t.io.api;
	requires transitive com.arvatosystems.t9t.io.be;
	requires transitive com.arvatosystems.t9t.io.sapi;
	requires transitive de.jpaw.bonaparte.api.media;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.bonaparte.poi;
	requires transitive de.jpaw.jdp;
	requires transitive de.jpaw.jpaw.util;
	requires org.slf4j;
}