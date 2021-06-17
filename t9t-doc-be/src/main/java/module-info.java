module com.arvatosystems.t9t.doc.be {
	exports com.arvatosystems.t9t.doc.be.api;
	exports com.arvatosystems.t9t.doc.be.impl;
	exports com.arvatosystems.t9t.barcode.be.api;
	exports com.arvatosystems.t9t.barcode.be.impl;
	exports com.arvatosystems.t9t.doc.be.converters.impl;
	exports com.arvatosystems.t9t.image.be.impl;
	exports com.arvatosystems.t9t.doc.be.request;

	requires com.arvatosystems.t9t.base.api;
	requires com.arvatosystems.t9t.base.be;
	requires com.arvatosystems.t9t.base.sapi;
	requires com.arvatosystems.t9t.doc.api;
	requires com.arvatosystems.t9t.doc.sapi;
	requires com.arvatosystems.t9t.email.api;
	requires com.arvatosystems.t9t.server;
	requires com.arvatosystems.t9t.translation.sapi;
	requires com.google.common;
	requires com.google.zxing;
	requires de.jpaw.annotations;
	requires de.jpaw.bonaparte.api;
	requires de.jpaw.bonaparte.api.media;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.bonaparte.refs;
	requires de.jpaw.jdp;
	requires de.jpaw.jpaw.enums;
	requires de.jpaw.jpaw.util;
	requires freemarker;
	requires java.desktop;
	requires org.eclipse.xtend.lib;
	requires org.eclipse.xtext.xbase.lib;
	requires org.joda.time;
	requires org.slf4j;
}