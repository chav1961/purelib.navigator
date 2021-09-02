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
	requires java.datatransfer;

	exports chav1961.purelibnavigator.admin; 
	exports chav1961.purelibnavigator.admin.entities; 
	exports chav1961.purelibnavigator.interfaces; 
	opens chav1961.purelibnavigator.admin to chav1961.purelib; 
	opens chav1961.purelibnavigator.admin.entities to chav1961.purelib; 
	opens chav1961.purelibnavigator.navigator to chav1961.purelib; 
}
