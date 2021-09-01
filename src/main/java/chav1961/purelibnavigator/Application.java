package chav1961.purelibnavigator;

import java.util.Arrays;

import chav1961.purelib.basic.ArgParser;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;

public class Application {
	public  enum ApplicationMode {
		admin, javadoc, navigator
	}
	
	public static void main(final String[] args) {
		final ArgParser			argParser = new ApplicationArgParser();
		
		try{final ArgParser		parsed = argParser.parse(true,true,args);
		
			switch (parsed.getValue("mode",ApplicationMode.class)) {
				case admin		:
					chav1961.purelibnavigator.admin.Application.main(Arrays.copyOfRange(args,1,args.length));
					break;
				case navigator	:
				default:
					System.err.println("Command line argument(s) error: unsupported mode ["+parsed.getValue("mode",ApplicationMode.class)+"]");
					System.exit(128);
			}
		} catch (CommandLineParametersException e) {
			System.err.println("Command line argument(s) error: "+e.getLocalizedMessage());
			System.err.println(argParser.getUsage("purelib.navigator"));
			System.exit(128);
		}
	}

	static class ApplicationArgParser extends ArgParser {
		public ApplicationArgParser() {
			super(new EnumArg<ApplicationMode>("mode",ApplicationMode.class,true,true,"Mode to start application. "+Arrays.toString(ApplicationMode.values())+" are available"));
		}
	}
}
