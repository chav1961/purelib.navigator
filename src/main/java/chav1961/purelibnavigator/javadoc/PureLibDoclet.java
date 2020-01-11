package chav1961.purelibnavigator.javadoc;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;

import org.w3c.dom.Document;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelibnavigator.javadoc.Application.OutputFormat;
import chav1961.purelibnavigator.javadoc.Application.Visibility;
import chav1961.purelibnavigator.javadoc.upload.html.HtmlUploader;
import chav1961.purelibnavigator.javadoc.upload.pdf.PdfUploader;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
import jdk.javadoc.doclet.Taglet;
 
public class PureLibDoclet implements Doclet {
    private static final boolean OK = true;
    private static final boolean FAILURE = false;
	
    private Locale			currentLocale = null;
    private Reporter		currentReporter = null;
    private OutputFormat	format = OutputFormat.pdf;
    private Visibility		visibility = Visibility.exportedOnly;
    private String			targetPath = "./";
    
    @Override
    public void init(final Locale locale, final Reporter reporter) {  
    	this.currentLocale = locale;
    	this.currentReporter = reporter;
    }
 
    @Override
    public String getName() {
        return getClass().getSimpleName();
    }
 
    @Override
    public Set<? extends Option> getSupportedOptions() {
    	return Set.of(new Option("-outputFormat",true,"Output format for the doclet","pdf",(opt,args)->{
    					try{format = OutputFormat.valueOf(args.get(0));
    						return OK;    						
    					} catch (IllegalArgumentException exc) {
    						error("Illegal value ["+args.get(0)+"] for output format. Only "+Arrays.toString(OutputFormat.values())+" are available");
        					return FAILURE;
    					}
    				})
    	    		,new Option("-visibility",true,"What kind of content must be included","exportedOnly",(opt,args)->{
    	    			try{visibility = Visibility.valueOf(args.get(0));
							return OK;    						
						} catch (IllegalArgumentException exc) {
							error("Illegal value ["+args.get(0)+"] for visibility. Only "+Arrays.toString(Visibility.values())+" are available");
	    					return FAILURE;
						}
    	    		})
    	    		,new Option("-targetPath",true,"Directory to store content was built","./",(opt,args)->{
    					targetPath = args.get(0);
						return OK;    						
    	    		})
    			);
    }
 
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
 
 
    @Override
    public boolean run(final DocletEnvironment environment) {
    	final LoggerFacade	lf = currentReporter == null ? PureLibSettings.NULL_LOGGER : new WrappedLoggerFacade(currentReporter);
    	final Document		doc = buildXMLDocument(environment.getSpecifiedElements(),visibility,lf);
    	final File			target = new File(targetPath);
    	
		try{switch (format) {
				case html	:
					HtmlUploader.upload(doc, target);
			        return OK;
				case pdf	:
					PdfUploader.upload(doc, target);
			        return OK;
				default		:
					lf.message(Severity.severe,"Output format ["+format+"] is not supported yet");
			        return FAILURE;
	    	}
		} catch (IOException e) {
			lf.message(Severity.severe,e.getLocalizedMessage(),e);
	        return FAILURE;
		}
    }

    private Document buildXMLDocument(final Set<? extends Element> specifiedElements, final Visibility visibility, final LoggerFacade lf) {
		// TODO Auto-generated method stub
    	specifiedElements.forEach(System.out::println);
		return null;
	}

	private void error(final String msg) {
    	if (currentReporter != null) {
    		currentReporter.print(javax.tools.Diagnostic.Kind.ERROR, msg);
    	}
    }

    @FunctionalInterface
    private interface OptionProcessor {
    	boolean process(final String option, final List<String> arguments);    	
    }
    
    class Option implements Doclet.Option {
        private final String name;
        private final boolean hasArg;
        private final String description;
        private final String parameters;
        private final OptionProcessor proc;
 
        Option(final String name, final boolean hasArg, final String description, final String parameters, final OptionProcessor proc) {
            this.name = name;
            this.hasArg = hasArg;
            this.description = description;
            this.parameters = parameters;
            this.proc = proc;
        }
 
        @Override
        public int getArgumentCount() {
            return hasArg ? 1 : 0;
        }
 
        @Override
        public String getDescription() {
            return description;
        }
 
        @Override
        public Kind getKind() {
            return Kind.STANDARD;
        }
 
        @Override
        public List<String> getNames() {
            return List.of(name);
        }
 
        @Override
        public String getParameters() {
            return hasArg ? parameters : null;
        }

		@Override
		public boolean process(final String option, final List<String> arguments) {
			return proc.process(option, arguments);
		}
		
		@Override
		public String toString() {
			return "Option [name=" + name + ", hasArg=" + hasArg + ", description=" + description + ", parameters=" + parameters + "]";
		}
    }
}