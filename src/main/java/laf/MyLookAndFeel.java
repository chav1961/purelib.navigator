package laf;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.basic.BasicLookAndFeel;

public class MyLookAndFeel extends BasicLookAndFeel {
	private static final long serialVersionUID = -3849885244234048251L;

	@Override
	public String getName() {
		return "MyLookAndFeel";
	}

	@Override
	public String getID() {
		return getName ();
	}

	@Override
	public String getDescription() {
		return "Cross-platform Java Look and Feel";
	}

	@Override
	public boolean isNativeLookAndFeel() {
		return false;
	}

	@Override
	public boolean isSupportedLookAndFeel() {
		return true;
	}

	@Override
	protected void initClassDefaults ( UIDefaults table ) {
		super.initClassDefaults ( table );

		// Button
		table.put ( "ButtonUI", MyButtonUI.class.getCanonicalName () );

		// Label
		table.put ( "LabelUI", MyLabelUI.class.getCanonicalName () );
		
/*		
		// Label
		table.put ( "ToolTipUI", ... );

		table.put ( "ButtonUI", ... );
		table.put ( "ToggleButtonUI", ... );
		table.put ( "CheckBoxUI", ... );
		table.put ( "RadioButtonUI", ... );

		// Menu
		table.put ( "MenuBarUI", ... );
		table.put ( "MenuUI", ... );
		table.put ( "PopupMenuUI", ... );
		table.put ( "MenuItemUI", ... );
		table.put ( "CheckBoxMenuItemUI", ... );
		table.put ( "RadioButtonMenuItemUI", ... );
		table.put ( "PopupMenuSeparatorUI", ... );

		// Separator
		table.put ( "SeparatorUI", ... );

		// Scroll
		table.put ( "ScrollBarUI", ... );
		table.put ( "ScrollPaneUI", ... );

		// Text
		table.put ( "TextFieldUI", ... );
		table.put ( "PasswordFieldUI", ... );
		table.put ( "FormattedTextFieldUI", ... );
		table.put ( "TextAreaUI", ... );
		table.put ( "EditorPaneUI", ... );
		table.put ( "TextPaneUI", ... );

		// Toolbar
		table.put ( "ToolBarUI", ... );
		table.put ( "ToolBarSeparatorUI", ... );

		// Table
		table.put ( "TableUI", ... );
		table.put ( "TableHeaderUI", ... );

		// Chooser
		table.put ( "ColorChooserUI", ... );
		table.put ( "FileChooserUI", ... );

		// Container
		table.put ( "PanelUI", ... );
		table.put ( "ViewportUI", ... );
		table.put ( "RootPaneUI", ... );
		table.put ( "TabbedPaneUI", ... );
		table.put ( "SplitPaneUI", ... );

		// Complex components
		table.put ( "ProgressBarUI", ... );
		table.put ( "SliderUI", ... );
		table.put ( "SpinnerUI", ... );
		table.put ( "TreeUI", ... );
		table.put ( "ListUI", ... );
		table.put ( "ComboBoxUI", ... );

		// Desktop pane
		table.put ( "DesktopPaneUI", ... );
		table.put ( "DesktopIconUI", ... );
		table.put ( "InternalFrameUI", ... );

		// Option pane
		table.put ( "OptionPaneUI", ... );
*/				
	}	

	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		UIManager.setLookAndFeel(MyLookAndFeel.class.getCanonicalName());
		final JFrame		f = new JFrame();
		final JLabel		label = new JLabel("JLabel");
		final JButton		button = new JButton("JButton");
		final JTextField	text = new JTextField("JTextField");
		
		f.getContentPane().add(label,BorderLayout.WEST);
		f.getContentPane().add(button,BorderLayout.CENTER);
		f.getContentPane().add(text,BorderLayout.EAST);
		f.setSize(200,100);
		f.setVisible(true);
	}
}
