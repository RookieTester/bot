import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.io.*;
import com.heaton.bot.*;
import java.net.*;

/**
 * Example program from Chapter 12
 * Programming Spiders, Bots and Aggregators in Java
 * Copyright 2001 by Jeff Heaton
 *
 *
 * This example program will download all of the HTML files
 * of a website to a local drive. This shows how a spider can
 * be used to map/download a site. This example is very similar
 * to the example from chapter 8. This program processes the
 * bot exclusion header and does not "spider" the areas that
 * are disallowed.
 *
 * @author Jeff Heaton
 * @version 1.0
 */
public class GetSite extends javax.swing.JFrame implements ISpiderReportable {
  /**
   * The bot exclusion object.
   */
  BotExclusion _exclude = null;

  /**
   * The underlying spider object.
   */
  Spider _spider = null;

  /**
   * The current page count.
   */
  int _pagesCount;

  /**
   * The constructor. Set up the visual Swing
   * components that make up the user interface
   * for this program.
   */
  public GetSite()
  {
    //{{INIT_CONTROLS
    setTitle("A Conscientious Spider");
    getContentPane().setLayout(null);
    setSize(405,268);
    setVisible(false);
    D.setHorizontalTextPosition(
                               javax.swing.SwingConstants.LEFT);
    D.setVerticalTextPosition(
                             javax.swing.SwingConstants.TOP);
    D.setVerticalAlignment(
                          javax.swing.SwingConstants.TOP);
    D.setText("Download pages of:");
    getContentPane().add(D);
    D.setBounds(12,12,384,24);
    JLabel2.setText("URL:");
    getContentPane().add(JLabel2);
    JLabel2.setBounds(12,36,36,24);
    getContentPane().add(_url);
    _url.setBounds(48,36,348,24);
    JLabel3.setText("Select local path to download files");
    getContentPane().add(JLabel3);
    JLabel3.setBounds(12,72,384,24);
    getContentPane().add(_save);
    _save.setBounds(12,96,384,24);
    _go.setText("GO!");
    getContentPane().add(_go);
    _go.setBounds(96,228,216,24);
    getContentPane().add(_current);
    _current.setBounds(12,204,384,12);
    JLabel4.setText("Number of pages:");
    getContentPane().add(JLabel4);
    JLabel4.setBounds(12,180,120,12);
    _pages.setText("0");
    getContentPane().add(_pages);
    _pages.setBounds(120,180,108,12);
    JLabel6.setText(
                   "Select local path(and filename) to write log to(optional):");
    getContentPane().add(JLabel6);
    JLabel6.setBounds(12,120,384,24);
    _logPath.setText("./spider.log");
    getContentPane().add(_logPath);
    _logPath.setBounds(12,144,384,24);
    _go.setActionCommand("jbutton");
    //}}

    //{{INIT_MENUS
    //}}

    //{{REGISTER_LISTENERS
    SymAction lSymAction = new SymAction();
    _go.addActionListener(lSymAction);
    SymWindow aSymWindow = new SymWindow();
    this.addWindowListener(aSymWindow);
    //}}
    setLocation(32,32);
  }

  /**
   * Added by Visual Cafe.
   *
   * @param b
   */
  public void setVisible(boolean b)
  {
    if ( b )
      setLocation(50, 50);
    super.setVisible(b);
  }

  /**
   * Program entry point, causes the main
   * window to be displayed.
   *
   * @param args Command line arguments are not used.
   */
  static public void main(String args[])
  {
    (new GetSite()).setVisible(true);
  }

  /**
   * Added by Visual Cafe.
   */
  public void addNotify()
  {
    // Record the size of the window prior
    // to calling parents addNotify.
    Dimension size = getSize();

    super.addNotify();

    if ( frameSizeAdjusted )
      return;
    frameSizeAdjusted = true;

    // Adjust size of frame according to the insets and menu bar
    Insets insets = getInsets();
    javax.swing.JMenuBar menuBar = getRootPane().getJMenuBar();
    int menuBarHeight = 0;
    if ( menuBar != null )
      menuBarHeight = menuBar.getPreferredSize().height;
    setSize(insets.left +
            insets.right +
            size.width,
            insets.top +
            insets.bottom +
            size.height + menuBarHeight);
  }

  // Used by addNotify
  boolean frameSizeAdjusted = false;

  //{{DECLARE_CONTROLS
  javax.swing.JLabel D = new javax.swing.JLabel();
  javax.swing.JLabel JLabel2 = new javax.swing.JLabel();

  /**
   * The URL to spider.
   */
  javax.swing.JTextField _url = new javax.swing.JTextField();
  javax.swing.JLabel JLabel3 = new javax.swing.JLabel();

  /**
   * The directory to save the files to.
   */
  javax.swing.JTextField _save = new javax.swing.JTextField();

  /**
   * The go button.
   */
  javax.swing.JButton _go = new javax.swing.JButton();

  /**
   * Displays the current page.
   */
  javax.swing.JLabel _current = new javax.swing.JLabel();
  javax.swing.JLabel JLabel4 = new javax.swing.JLabel();

  /**
   * A count of how many pages have been
   * downloaded.
   */
  javax.swing.JLabel _pages = new javax.swing.JLabel();
  javax.swing.JLabel JLabel6 = new javax.swing.JLabel();

  /**
   * Used to specify the path to store the
   * log to.
   */
  javax.swing.JTextField _logPath = new javax.swing.JTextField();
  //}}

  //{{DECLARE_MENUS
  //}}


  /**
   * An event handler class, generated by Visual Cafe.
   *
   * @author Visual Cafe
   */
  class SymAction implements java.awt.event.ActionListener {
    public void actionPerformed(java.awt.event.ActionEvent event)
    {
      Object object = event.getSource();
      if ( object == _go )
        Go_actionPerformed(event);
    }
  }


  /**
   * As the files of the website are located,
   * this method is called to save them to disk.
   *
   * @param file The HTTP object corrisponding to the page
   * just visited.
   */
  protected void processFile(HTTP file)
  {
    try {
      if ( _save.getText().length()>0 ) {

        URL url = new URL(file.getURL());
        String targetPath = url.getPath();
        targetPath = URLUtility.convertFilename(_save.getText(),targetPath);
        FileOutputStream fso =
              new FileOutputStream(
              new File(targetPath) );
        fso.write( file.getBodyBytes() );
        fso.close();

       }
    } catch ( Exception e ) {
      Log.logException("Can't save output file: ",e);
    }
  }

  /**
   * This is where most of the action takes place. This
   * method is called when the GO! button is pressed.
   *
   * @param event The event
   */
  void Go_actionPerformed(java.awt.event.ActionEvent event)
  {
    IWorkloadStorable wl = new SpiderInternalWorkload();
    if ( _spider!=null ) {

      Runnable doLater = new Runnable()
      {
        public void run()
        {
          _go.setText("Canceling...");
        }
      };
      SwingUtilities.invokeLater(doLater);

      _spider.halt();
      return;
    }

    try {
      if ( _url.getText().length()>0 ) {
        HTTPSocket http = new HTTPSocket();
        http.send(_url.getText(),null);
        _exclude = new BotExclusion();
        _exclude.load(new HTTPSocket(),_url.getText());

      } else {
        _current.setText("<<distributed mode>>");
      }
    } catch ( Exception e ) {
      JOptionPane.showMessageDialog(this,
                                    e,
                                    "Error",
                                    JOptionPane.OK_CANCEL_OPTION,
                                    null );

      return;
    }

    Runnable doLater = new Runnable()
    {
      public void run()
      {
        _go.setText("Cancel");
        _current.setText("Loading....");
      }
    };
    SwingUtilities.invokeLater(doLater);

    // Prepare to start the spider
    _pagesCount = 0;
    if ( _logPath.getText().length()>0 ) {
      File file = new File(_logPath.getText());
      file.delete();
      Log.setLevel(Log.LOG_LEVEL_NORMAL);
      Log.setFile(true);
      Log.setConsole(false);
      Log.setPath(_logPath.getText());
    }

// NOTE: To use SQL based workload management,
// uncomment the following lines and include a
// valid data source.
/*
    try
    {
      wl = new SpiderSQLWorkload(
        "sun.jdbc.odbc.JdbcOdbcDriver",
        "jdbc:odbc:WORKLOAD");
    }
    catch(Exception e)
    {
      JOptionPane.showMessageDialog(this,
        e,
        "Error",
        JOptionPane.OK_CANCEL_OPTION,
        null );
    }
*/

    _spider
    = new Spider( this,
                  _url.getText(),
                  new HTTPSocket(),
                  100,
                  wl);
    _spider.setMaxBody(200);
    _spider.start();

  }

  /**
   * This method is called by the spider when an
   * internal link is found.
   *
   * @param url The URL of the link that was found. This
   * link is passed in fully resolved.
   * @return True if the spider should add this link to
   * its visitation list.
   */
  public boolean foundInternalLink(String url)
  {
    return(!_exclude.isExcluded(url));
  }

  /**
   * This method is called by the spider when an
   * external link is found. An external link is
   * one that points to a different host.
   *
   * @param url The URL of the link that was found. This
   * link is passed in fully resolved.
   * @return True if the spider should add this link to
   * its visitation list.
   */
  public boolean foundExternalLink(String url)
  {
    return false;
  }

  /**
   * This method is called by the spider when an
   * other type link is found. Links such as email
   * addresses are sent to this method.
   *
   * @param url The URL of the link that was found. This
   * link is passed in fully resolved.
   * @return True if the spider should add this link to
   * its visitation list.
   */
  public boolean foundOtherLink(String url)
  {
    return false;
  }

  /**
   * A simple class used to update the current
   * URL target. This is necessary, because Swing
   * only allows GUI compoents to be updated by the
   * main thread.
   *
   * @author Jeff Heaton
   * @version 1.0
   */

  class UpdateTarget implements Runnable {
    public String _t;
    public void run()
    {
      _current.setText(_t);
      _pages.setText( "" + _pagesCount );
    }
  }

  /**
   * Called by the spider when a page has been
   * loaded, and should be processed. For the
   * example, this method will save this file
   * to disk.
   *
   * @param page The HTTP object that corrispondeds to the
   * page just visited.
   */
  public void processPage(HTTP page)
  {
    _pagesCount++;
    UpdateTarget ut = new UpdateTarget();

    ut._t = page.getURL();
    SwingUtilities.invokeLater(ut);
    processFile(page);
  }

  /**
   * Not used. This must be implemented because
   * of the interface. Called when a page completes.
   *
   * @param page The page that just completed.
   * @param error True if the completion of this page
   * resulted in an error.
   */
  public void completePage(HTTP page,boolean error)
  {
  }

  /**
   * This method is called to determine if
   * query strings should be stripped.
   *
   * @return Returns true if query strings(the part of
   * the URL after the ?) should be stripped.
   */
  public boolean getRemoveQuery()
  {
    return true;
  }

  /**
   * This method is called once the spider
   * has no more work to do.
   */
  public void spiderComplete()
  {
    if ( _spider.isHalted() ) {
      JOptionPane.showMessageDialog(this,
                                    "Download of site has been canceled. " +
                                    "Check log file for any errors.",
                                    "Done",
                                    JOptionPane.OK_CANCEL_OPTION,
                                    null );
    } else {
      JOptionPane.showMessageDialog(this,
                                    "Download of site is complete. " +
                                    "Check log file for any errors.",
                                    "Done",
                                    JOptionPane.OK_CANCEL_OPTION,
                                    null );
    }
    _spider=null;

    Runnable doLater = new Runnable()
    {
      public void run()
      {
        _go.setText("GO!!");
      }
    };
    SwingUtilities.invokeLater(doLater);
  }


  /**
   * An event handler class, generated by Visual Cafe.
   *
   * @author Visual Cafe
   */
  class SymWindow extends java.awt.event.WindowAdapter {
    public void windowClosed(java.awt.event.WindowEvent event)
    {
      Object object = event.getSource();
      if ( object == GetSite.this )
        GetSite_windowClosed(event);
    }
  }
  /**
   * Called to close the window.
   *
   * @param event The event.
   */

  void GetSite_windowClosed(java.awt.event.WindowEvent event)
  {
    System.exit(0);
  }


}