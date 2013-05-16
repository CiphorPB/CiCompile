package ciphor;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;

/**
 * @author Ciphor
 *
 */
@SuppressWarnings("serial")
public class HyperLinkLabel extends JLabel {
	public HyperLinkLabel(final String url, final String anchor) {
		this.setText("<html>" + anchor + "</html>");
		this.setCursor(new Cursor(Cursor.HAND_CURSOR));
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				try {
					Desktop.getDesktop().browse(new URI(url));
				} catch (URISyntaxException ex) {
					Logger.getLogger(HyperLinkLabel.class.getName()).log(
							Level.SEVERE, null, ex);
				} catch (IOException ex) {
					Logger.getLogger(HyperLinkLabel.class.getName()).log(
							Level.SEVERE, null, ex);
				}
			}
		});
	}
}