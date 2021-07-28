module com.arvatosystems.t9t.doc.be {
	exports com.arvatosystems.t9t.doc.be.api;
	exports com.arvatosystems.t9t.doc.be.impl;
	exports com.arvatosystems.t9t.barcode.be.api;
	exports com.arvatosystems.t9t.barcode.be.impl;
	exports com.arvatosystems.t9t.doc.be.converters.impl;
	exports com.arvatosystems.t9t.image.be.impl;
	exports com.arvatosystems.t9t.doc.be.request;

	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.arvatosystems.t9t.base.be;
	requires transitive com.arvatosystems.t9t.base.sapi;
	requires transitive com.arvatosystems.t9t.doc.api;
	requires transitive com.arvatosystems.t9t.doc.sapi;
	requires transitive com.arvatosystems.t9t.email.api;
	requires transitive com.arvatosystems.t9t.server;
	requires transitive com.arvatosystems.t9t.translation.sapi;
	requires transitive com.google.common;
	requires com.google.zxing;
	requires transitive de.jpaw.annotations;
	requires transitive de.jpaw.bonaparte.api;
	requires transitive de.jpaw.bonaparte.api.media;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.bonaparte.refs;
	requires transitive de.jpaw.jdp;
	requires transitive de.jpaw.jpaw.enums;
	requires transitive de.jpaw.jpaw.util;
	requires freemarker;
	requires java.desktop;
	requires transitive org.eclipse.xtend.lib;
	requires transitive org.eclipse.xtext.xbase.lib;
	requires transitive org.joda.time;
	requires org.slf4j;
}
