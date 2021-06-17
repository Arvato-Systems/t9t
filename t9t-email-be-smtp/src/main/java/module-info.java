module com.arvatosystems.t9t.email.be.smtp {
	exports com.arvatosystems.t9t.email.be.smtp.impl;

	requires com.arvatosystems.t9t.base.api;
	requires com.arvatosystems.t9t.email.api;
	requires com.arvatosystems.t9t.email.sapi;
	requires com.google.common;
	requires de.jpaw.annotations;
	requires de.jpaw.bonaparte.api;
	requires de.jpaw.bonaparte.api.media;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.jdp;
	requires de.jpaw.jpaw.enums;
	requires de.jpaw.jpaw.util;
	requires jakarta.activation;
	requires jakarta.mail;
	requires org.eclipse.xtext.xbase.lib;
	requires org.slf4j;
}