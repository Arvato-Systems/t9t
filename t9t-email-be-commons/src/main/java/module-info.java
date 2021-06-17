module com.arvatosystems.t9t.email.be.commons {
	exports com.arvatosystems.t9t.email.be.commons.impl;

	requires com.arvatosystems.t9t.base.api;
	requires com.arvatosystems.t9t.email.api;
	requires com.arvatosystems.t9t.email.sapi;
	requires com.arvatosystems.t9t.email.be.smtp;
	requires com.google.common;
	requires commons.email;
	requires de.jpaw.annotations;
	requires de.jpaw.bonaparte.api;
	requires de.jpaw.bonaparte.api.media;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.jdp;
	requires jakarta.activation;
	requires jakarta.mail;
	requires org.eclipse.xtext.xbase.lib;
	requires org.slf4j;
}