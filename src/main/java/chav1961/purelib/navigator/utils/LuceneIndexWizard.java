package chav1961.purelib.navigator.utils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.EtchedBorder;

import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.navigator.LocalizationKeys;
import chav1961.purelib.ui.AbstractWizardStep;
import chav1961.purelib.ui.interfaces.ErrorProcessing;
import chav1961.purelib.ui.swing.SimpleWizard;

public class LuceneIndexWizard extends SimpleWizard<LuceneIndexWizard,LoggerFacade.Severity> {
	private static final long 		serialVersionUID = 1L;
	private static final String		FIRST_STEP_CAPTION_ID = "FirstStep.Caption";  
	private static final String		SECOND_STEP_CAPTION_ID = "SecondStep.Caption";  
	private static final String		THIRD_STEP_CAPTION_ID = "ThirdStep.Caption";  

	public LuceneIndexWizard(final Window parent, final Localizer localizer) throws LocalizationException {
		super(parent,LocalizationKeys.WIZARD_LUCENEINDEX_CAPTION_ID,ModalityType.DOCUMENT_MODAL, 
			new HashMap<String,Object>(){private static final long serialVersionUID = 1L; {this.put(SimpleWizard.PROP_LOCALIZER,localizer);}},
			new FirstStep(localizer), new SecondStep(localizer), new ThirdStep(localizer));
	}
	
@LocaleResourceLocation(Localizer.LOCALIZER_SCHEME+":prop:chav1961/purelib/navigator/utils/LuceneIndexWizard")
	private static class FirstStep extends AbstractWizardStep<LuceneIndexWizard,LoggerFacade.Severity,JComponent> implements ActionListener {
		private final Localizer			localizer;
		private final JPanel			content = new JPanel();

@LocaleResource(value="FirstStep.Preamble",tooltip="FirstStep.PreambleTT")		
		private final JLabel			preamble = new JLabel();

@LocaleResource(value="FirstStep.IndexSource",tooltip="FirstStep.IndexSourceTT")		
		private final JCheckBox			indexSources = new JCheckBox();
@LocaleResource(value="FirstStep.SourceLocation",tooltip="FirstStep.SourceLocationTT")		
		private final JTextField		sourceLocation = new JTextField();
		private final JButton			selectSourceLocation = new JButton(new ImageIcon(LuceneIndexWizard.class.getResource("open.png")));

@LocaleResource(value="FirstStep.IndexJavadoc",tooltip="FirstStep.IndexJavadocTT")		
		private final JCheckBox			indexJavadoc = new JCheckBox();
@LocaleResource(value="FirstStep.JavadocLocation",tooltip="FirstStep.JavadocLocationTT")		
		private final JTextField		javadocLocation = new JTextField();
		private final JButton			selectJavadocLocation = new JButton(new ImageIcon(LuceneIndexWizard.class.getResource("open.png")));
		
		private FirstStep(final Localizer localizer) {
			this.localizer = localizer;
			content.setLayout(new BorderLayout(5,5));
			
			content.add(preamble,BorderLayout.NORTH);
			
			final SpringLayout	sourcesLayout = new SpringLayout();
			final JPanel		aboutSources = new JPanel(sourcesLayout);
			final SpringLayout	javadocLayout = new SpringLayout();
			final JPanel		aboutJavaDoc = new JPanel(javadocLayout);
			final JPanel		center = new JPanel(new GridLayout(3,1));
			
			aboutSources.add(indexSources);
			aboutSources.add(sourceLocation);
			aboutSources.add(selectSourceLocation);
			sourcesLayout.putConstraint(SpringLayout.NORTH,indexSources,0,SpringLayout.NORTH,aboutSources);
			sourcesLayout.putConstraint(SpringLayout.WEST,indexSources,0,SpringLayout.WEST,aboutSources);
			sourcesLayout.putConstraint(SpringLayout.EAST,indexSources,0,SpringLayout.EAST,aboutSources);
			sourcesLayout.putConstraint(SpringLayout.NORTH,sourceLocation,5,SpringLayout.SOUTH,indexSources);
			sourcesLayout.putConstraint(SpringLayout.NORTH,selectSourceLocation,0,SpringLayout.SOUTH,indexSources);
			sourcesLayout.putConstraint(SpringLayout.WEST,sourceLocation,25,SpringLayout.WEST,aboutSources);
			sourcesLayout.putConstraint(SpringLayout.EAST,selectSourceLocation,-5,SpringLayout.EAST,aboutSources);
			sourcesLayout.putConstraint(SpringLayout.EAST,sourceLocation,-5,SpringLayout.WEST,selectSourceLocation);
			aboutSources.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
			
			aboutJavaDoc.add(indexJavadoc);
			aboutJavaDoc.add(javadocLocation);
			aboutJavaDoc.add(selectJavadocLocation);
			javadocLayout.putConstraint(SpringLayout.NORTH,indexJavadoc,0,SpringLayout.NORTH,aboutJavaDoc);
			javadocLayout.putConstraint(SpringLayout.WEST,indexJavadoc,0,SpringLayout.WEST,aboutJavaDoc);
			javadocLayout.putConstraint(SpringLayout.EAST,indexJavadoc,0,SpringLayout.EAST,aboutJavaDoc);
			javadocLayout.putConstraint(SpringLayout.NORTH,javadocLocation,5,SpringLayout.SOUTH,indexJavadoc);
			javadocLayout.putConstraint(SpringLayout.NORTH,selectJavadocLocation,0,SpringLayout.SOUTH,indexJavadoc);
			javadocLayout.putConstraint(SpringLayout.WEST,javadocLocation,25,SpringLayout.WEST,aboutJavaDoc);
			javadocLayout.putConstraint(SpringLayout.EAST,selectJavadocLocation,-5,SpringLayout.EAST,aboutJavaDoc);
			javadocLayout.putConstraint(SpringLayout.EAST,javadocLocation,-5,SpringLayout.WEST,selectJavadocLocation);
			aboutJavaDoc.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
			
			center.add(aboutSources);
			center.add(aboutJavaDoc);

			content.add(center,BorderLayout.CENTER);
			
			indexSources.addActionListener(this);			indexSources.setActionCommand("indexSources");
			sourceLocation.addActionListener(this);			sourceLocation.setActionCommand("sourceLocation");
			selectSourceLocation.addActionListener(this);	selectSourceLocation.setActionCommand("selectSourceLocation");
			sourceLocation.setEnabled(false);
			selectSourceLocation.setEnabled(false);

			indexJavadoc.addActionListener(this);			indexJavadoc.setActionCommand("indexJavadoc");
			javadocLocation.addActionListener(this);		javadocLocation.setActionCommand("javadocLocation");
			selectJavadocLocation.addActionListener(this);	selectJavadocLocation.setActionCommand("selectJavadocLocation");
			javadocLocation.setEnabled(false);
			selectJavadocLocation.setEnabled(false);
		}

		@Override 
		public StepType getStepType() {
			return StepType.INITIAL;
		}
		
		@Override 
		public String getCaption() {
			try{return localizer.getValue(FIRST_STEP_CAPTION_ID);
			} catch (LocalizationException | IllegalArgumentException e) {
				return FIRST_STEP_CAPTION_ID;
			}
		}

		@Override
		public JComponent getContent() {
			return content;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {
				case "indexSources"			:
					sourceLocation.setEnabled(indexSources.isSelected());
					selectSourceLocation.setEnabled(indexSources.isSelected());
					break;
				case "sourceLocation"		:
					break;
				case "selectSourceLocation"	:
					break;
				case "indexJavadoc"			:
					javadocLocation.setEnabled(indexJavadoc.isSelected());
					selectJavadocLocation.setEnabled(indexJavadoc.isSelected());
					break;
				case "javadocLocation"		:
					break;
				case "selectJavadocLocation":
					break;
				default : throw new UnsupportedOperationException("Unknown action commandd ["+e.getActionCommand()+"]");
			}
		}

		@Override
		public void beforeShow(LuceneIndexWizard content, Map<String, Object> temporary,
				ErrorProcessing<LuceneIndexWizard, Severity> err) throws FlowException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void afterShow(LuceneIndexWizard content, Map<String, Object> temporary,
				ErrorProcessing<LuceneIndexWizard, Severity> err) throws FlowException {
			// TODO Auto-generated method stub
			
		}
	}

@LocaleResourceLocation(Localizer.LOCALIZER_SCHEME+":prop:chav1961/purelib/navigator/utils/LuceneIndexWizard")
	private static class SecondStep extends AbstractWizardStep<LuceneIndexWizard,LoggerFacade.Severity,JComponent> {
		private final Localizer	localizer;
		private final JPanel	content = new JPanel();
		
		
		private SecondStep(final Localizer localizer) {
			this.localizer = localizer;
			content.setPreferredSize(new Dimension(200,200));
		}
	
		@Override 
		public StepType getStepType() {
			return StepType.INITIAL;
		}
		
		@Override 
		public String getCaption() {
			try{return localizer.getValue(SECOND_STEP_CAPTION_ID);
			} catch (LocalizationException | IllegalArgumentException e) {
				return SECOND_STEP_CAPTION_ID;
			}
		}
	
		@Override
		public JComponent getContent() {
			return content;
		}
	
		@Override
		public void beforeShow(final LuceneIndexWizard content, final Map<String, Object> temporary, final ErrorProcessing<LuceneIndexWizard, Severity> err) throws FlowException {
			// TODO Auto-generated method stub
			
		}
	
		@Override
		public void afterShow(final LuceneIndexWizard content, final Map<String, Object> temporary, final ErrorProcessing<LuceneIndexWizard, Severity> err) throws FlowException {
			// TODO Auto-generated method stub
			
		}
	}

@LocaleResourceLocation(Localizer.LOCALIZER_SCHEME+":prop:chav1961/purelib/navigator/utils/LuceneIndexWizard")
	private static class ThirdStep extends AbstractWizardStep<LuceneIndexWizard,LoggerFacade.Severity,JComponent> {
		private final Localizer	localizer;
		private final JPanel	content = new JPanel();
		
		
		private ThirdStep(final Localizer localizer) {
			this.localizer = localizer;
			content.setPreferredSize(new Dimension(200,200));
		}
	
		@Override 
		public StepType getStepType() {
			return StepType.TERM_SUCCESS;
		}
		
		@Override 
		public String getCaption() {
			try{return localizer.getValue(THIRD_STEP_CAPTION_ID);
			} catch (LocalizationException | IllegalArgumentException e) {
				return THIRD_STEP_CAPTION_ID;
			}
		}
	
		@Override
		public JComponent getContent() {
			return content;
		}
	
		@Override
		public void beforeShow(final LuceneIndexWizard content, final Map<String, Object> temporary, final ErrorProcessing<LuceneIndexWizard, Severity> err) throws FlowException {
			// TODO Auto-generated method stub
			
		}
	
		@Override
		public void afterShow(final LuceneIndexWizard content, final Map<String, Object> temporary, final ErrorProcessing<LuceneIndexWizard, Severity> err) throws FlowException {
			// TODO Auto-generated method stub
			
		}
	}
}
