module com.arvatosystems.t9t.cfg {
	exports com.arvatosystems.t9t.cfg.be;
	opens com.arvatosystems.t9t.cfg.be;   // jaxb.index must be found by reflection

	requires com.google.common;
	requires de.jpaw.annotations;
	requires de.jpaw.bonaparte.api.media;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.jdp;
	requires de.jpaw.jpaw.enums;
	requires de.jpaw.jpaw.util;
	requires de.jpaw.jpaw.xml;
	requires java.xml;
	requires java.xml.bind;
	requires org.eclipse.xtext.xbase.lib;
	requires org.joda.time;
	requires org.slf4j;
}