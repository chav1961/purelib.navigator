package chav1961.purelibnavigator.javadoc;

import javax.tools.Diagnostic.Kind;

import chav1961.purelib.basic.AbstractLoggerFacade;
import jdk.javadoc.doclet.Reporter;

class WrappedLoggerFacade extends AbstractLoggerFacade {
	private final Reporter	reporter;
	
	WrappedLoggerFacade(final Reporter reporter) {
		this.reporter = reporter;
	}

	@Override
	protected AbstractLoggerFacade getAbstractLoggerFacade(final String mark, final Class<?> root) {
		return new WrappedLoggerFacade(reporter);
	}

	@Override
	protected void toLogger(final Severity level, final String text, final Throwable t) {
		switch (level) {
			case trace	:
			case debug	:
				reporter.print(Kind.OTHER,text);
				break;
			case severe	:
			case error	:
				reporter.print(Kind.ERROR,text);
				break;
			case warning:
				reporter.print(Kind.MANDATORY_WARNING,text);
				break;
			case info	:
				reporter.print(Kind.NOTE,text);
			default		:
				break;
		}
		if (t != null) {
			toLogger(level,t.toString(),null);
		}
	}
}
