module chav1961.purelibnavigator {
	requires transitive chav1961.purelib;
	requires java.desktop;
	requires java.scripting;
	requires java.xml;
	requires java.sql;
	requires java.rmi;
	requires java.management;
	requires jdk.httpserver;
	requires java.compiler;
	requires jdk.javadoc;
	requires java.datatransfer;

	exports chav1961.purelibnavigator.admin; 
	opens chav1961.purelibnavigator.admin to chav1961.purelib; 
	opens chav1961.purelibnavigator.navigator to chav1961.purelib; 
	exports chav1961.purelibnavigator.javadoc; 
	exports laf; 
	opens laf; 
}
