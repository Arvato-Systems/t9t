module com.arvatosystems.t9t.cfg {
	exports com.arvatosystems.t9t.cfg.be;
	opens com.arvatosystems.t9t.cfg.be;   // jaxb.index must be found by reflection

	requires transitive com.google.common;
	requires transitive de.jpaw.annotations;
	requires transitive de.jpaw.bonaparte.api.media;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.jdp;
	requires transitive de.jpaw.jpaw.enums;
	requires transitive de.jpaw.jpaw.util;
	requires transitive de.jpaw.jpaw.xml;
	requires transitive java.xml;
	requires transitive java.xml.bind;
	requires transitive org.eclipse.xtext.xbase.lib;
	requires transitive org.joda.time;
	requires org.slf4j;
}