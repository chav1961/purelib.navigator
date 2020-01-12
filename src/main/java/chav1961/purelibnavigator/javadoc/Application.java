package chav1961.purelibnavigator.javadoc;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import chav1961.purelib.basic.ArgParser;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;
import jdk.javadoc.doclet.Taglet;

public class Application {
	public enum OutputFormat {
		html, pdf
	}

	public enum Visibility {
		exportedOnly, publicOnly, publicAndProtected
	}
	
	public static void main(final String[] args) {
		final ArgParser			argParser = new ApplicationArgParser();
		
		try {final ArgParser	parsed = argParser.parse(args);
			if (System.getenv("path") != null) {
				if (Utils.checkFileExistence(System.getenv("path"),"javadoc.exe")) {
					System.exit(startJavaDoc("javadoc.exe",parsed));
				}
				else if (Utils.checkFileExistence(System.getenv("path"),"javadoc")) {
					System.exit(startJavaDoc("javadoc",parsed));
				}
				else {
					System.err.println("File javadoc/javadoc.exe not found in the PATH. Check your JDK installed (not JRE!)");
					System.exit(128);
				}
			}
			else {
				System.err.println("File javadoc/javadoc.exe can't be found because PATH environment variable is missing. Check your JDK installed (not JRE!)");
				System.exit(128);
			}
		} catch (CommandLineParametersException e) {
			System.err.println("Command line argument(s) error: "+e.getLocalizedMessage());
			System.err.println(argParser.getUsage("purelib.navigator javadoc"));
			System.exit(128);
		}
	}
	
	private static int startJavaDoc(final  String javadocName, final ArgParser args) throws CommandLineParametersException {
		final List<String>	commands = new ArrayList<>();
		final String		sourcePath = args.getValue("sourcePath",String.class);
		final String		rootPackage = findRootPackage(new File(sourcePath),"");
				
		if (rootPackage == null) {
			throw new CommandLineParametersException("No any java file(s) found in the source path ["+sourcePath+"]!");
		}
		else {
			commands.addAll(Arrays.asList(javadocName
									,rootPackage				
									,"-classpath", "E:/chav1961/workspace/purelib/target/purelib-0.0.4.jar"
									,"-sourcepath", sourcePath
									,"-subpackages", rootPackage
									,"-doclet", PureLibDoclet.class.getCanonicalName()
									,"-docletpath", PureLibDoclet.class.getProtectionDomain().getCodeSource().getLocation().getPath()
									,"-outputFormat",args.getValue("outputFormat",OutputFormat.class).name()
									,"-visibility",args.getValue("visibility",Visibility.class).name()
									,"-targetpath",args.getValue("targetPath",String.class)
									,"-private"
									,"--module-path", "E:/chav1961/workspace/purelib/target/purelib-0.0.4.jar"
									,"--add-modules", "chav1961.purelib"
							)
					
					);
			try{final Process	proc = new ProcessBuilder(commands)
											.redirectError(Redirect.INHERIT)
											.redirectOutput(Redirect.INHERIT)
											.redirectInput(Redirect.INHERIT)
											.start();
			
				proc.waitFor();
				return proc.exitValue();
			} catch (IOException e) {
				e.printStackTrace();
				return 128; 
			} catch (InterruptedException e) {
				e.printStackTrace();
				return 128; 
			}
		}
	}

	private static String findRootPackage(final File current, final String packageName) {
		if (current.exists()) {
			if (current.isDirectory()) {
				for (File item : current.listFiles()) {
					if (item.isFile() && item.getName().endsWith(".java")) {
						return packageName.isEmpty() ? "" : packageName.substring(1);
					}
				}
				for (File item : current.listFiles()) {
					final String	pack = findRootPackage(item,packageName+'.'+item.getName());
					
					if (pack != null) {
						return pack;
					}
				}
				return null;
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
	}

	private static class ApplicationArgParser extends ArgParser {
		public ApplicationArgParser() {
			super(new StringArg("sourcePath", true, false, "source path for classes to build javadoc for"),
				  new StringArg("targetPath", true, false, "target path for classes to build javadoc for"),
				  new EnumArg<OutputFormat>("outputFormat",OutputFormat.class,true,false,"Output format to print. "+Arrays.toString(OutputFormat.values())+" are available"),					
				  new EnumArg<Visibility>("visibility",Visibility.class,true,false,"Vivsibility of entities to print. "+Arrays.toString(Visibility.values())+" are available")					
				);
		}
	}	
}
