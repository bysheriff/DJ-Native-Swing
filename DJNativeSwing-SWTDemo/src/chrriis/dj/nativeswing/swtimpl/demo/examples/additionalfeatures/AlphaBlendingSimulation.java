/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.demo.examples.additionalfeatures;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import chrriis.common.Disposable;
import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;

/**
 * @author Christopher Deckers
 */
public class AlphaBlendingSimulation extends JPanel implements Disposable {

  private JWebBrowser webBrowser;
  
  @Override
  public boolean isOptimizedDrawingEnabled() {
    // This indicates that the component allows layering of children.
    return false;
  }
  
  public AlphaBlendingSimulation() {
    super(null);
    webBrowser = new JWebBrowser(JWebBrowser.constrainVisibility());
    webBrowser.setBarsVisible(false);
    webBrowser.navigate("http://www.google.com");
    webBrowser.setBounds(50, 50, 500, 400);
    add(webBrowser);
    JLabel descriptionLabel = new JLabel("Grab and move that image over the native component: ");
    descriptionLabel.setSize(descriptionLabel.getPreferredSize());
    descriptionLabel.setLocation(5, 15);
    add(descriptionLabel);
    ImageIcon icon = new ImageIcon(getClass().getResource("resource/DJIcon48x48.png"));
    final JLabel imageLabel = new JLabel(icon);
    MouseInputAdapter mouseInputAdapter = new MouseInputAdapter() {
      private Point originalMouseLocation;
      private Point originalLocation;
      @Override
      public void mousePressed(MouseEvent e) {
        originalMouseLocation = SwingUtilities.convertPoint(imageLabel, e.getPoint(), imageLabel.getParent());
        originalLocation = imageLabel.getLocation();
      }
      @Override
      public void mouseDragged(MouseEvent e) {
        Point newMouseLocation = SwingUtilities.convertPoint(imageLabel, e.getPoint(), imageLabel.getParent());
        imageLabel.setLocation(originalLocation.x - originalMouseLocation.x + newMouseLocation.x, originalLocation.y - originalMouseLocation.y + newMouseLocation.y);
        imageLabel.repaint();
      }
    };
    imageLabel.addMouseMotionListener(mouseInputAdapter);
    imageLabel.addMouseListener(mouseInputAdapter);
    imageLabel.setSize(imageLabel.getPreferredSize());
    imageLabel.setLocation(descriptionLabel.getWidth(), 0);
    imageLabel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    add(imageLabel);
    // Force label to be logically on top.
    setComponentZOrder(imageLabel, 0);
    updateBackgroundBuffer();
  }
  
  private volatile boolean isDisposed;
  
  private void updateBackgroundBuffer() {
    // we refresh the background buffer outside the UI thread, to minimize the overhead.
    new Thread() {
      @Override
      public void run() {
        int i = 0;
        while(!isDisposed) {
          if(i == 0) {
            // Every now and then we refresh the full buffer.
            webBrowser.getNativeComponent().createBackBuffer();
          } else {
            // The rest of the time we only update the areas that is covered with our overlay.
            webBrowser.getNativeComponent().updateBackBufferOnVisibleTranslucentAreas();
          }
          i = (i + 1) % 4;
          try {
            sleep(500);
          } catch(Exception e) {
            e.printStackTrace();
          }
        }
      }
    }.start();
  }
  
  public void dispose() {
    isDisposed = true;
  }
  
  /* Standard main method to try that test as a standalone application. */
  public static void main(String[] args) {
    UIUtils.setPreferredLookAndFeel();
    NativeInterface.open();
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JFrame frame = new JFrame("DJ Native Swing Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new AlphaBlendingSimulation(), BorderLayout.CENTER);
        frame.setSize(800, 600);
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
      }
    });
    NativeInterface.runEventPump();
  }
  
}
