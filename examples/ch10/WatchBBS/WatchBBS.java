import java.awt.*;
import java.util.*;
import javax.swing.*;
import com.heaton.bot.*;

/**
 * Example from Chapter 10
 *
 * This is a simple bot program that is designed
 * to work with one specific site. Later we will
 * see CatBots, which are designed to function across
 * an entire category of sites.
 *
 * This Bot is designed to scan the bulletin board hosted
 * at my site, http://www.jeffheaton.com, and look for new
 * messages. When a new message is located, the user is
 * informed that there are new messages waiting.
 *
 * @author Jeff Heaton
 * @version 1.0
 */
public class WatchBBS extends javax.swing.JFrame implements Runnable {

  /**
   * The time of the latest message posted.
   */
  Date _latest = null;

  /**
   * The time of the next poll.
   */
  Date _nextPoll;

  /**
   * The background thread.
   */
  Thread _thread;

  /**
   * The constructor. Used to setup all of the Swing
   * controls.
   */
  public WatchBBS()
  {
    //{{INIT_CONTROLS
    setTitle("Watch BBS");
    getContentPane().setLayout(null);
    setSize(398,210);
    setVisible(false);
    JLabel1.setText("Message board URL to watch:");
    getContentPane().add(JLabel1);
    JLabel1.setBounds(12,12,384,12);
    _url.setText(
    "http://www.jeffheaton.com/cgi-bin/ubb/ultimatebb.cgi");
    getContentPane().add(_url);
    _url.setBounds(12,36,372,24);
    JLabel2.setText("Polling Frequency(how often should we check):");
    getContentPane().add(JLabel2);
    JLabel2.setBounds(12,72,384,12);
    _minutes.setText("5");
    getContentPane().add(_minutes);
    _minutes.setBounds(12,96,96,24);
    JLabel3.setText("minutes");
    getContentPane().add(JLabel3);
    JLabel3.setBounds(120,108,240,12);
    _start.setText("Start");
    _start.setActionCommand("Start");
    getContentPane().add(_start);
    _start.setBounds(60,168,84,24);
    _stop.setText("Stop");
    _stop.setActionCommand("Stop");
    _stop.setEnabled(false);
    getContentPane().add(_stop);
    _stop.setBounds(156,168,84,24);
    _go.setText("Poll Now");
    _go.setActionCommand("Poll Now");
    _go.setEnabled(false);
    getContentPane().add(_go);
    _go.setBounds(252,168,84,24);
    _status.setText("Not started");
    getContentPane().add(_status);
    _status.setBounds(12,132,384,12);
    //}}

    //{{INIT_MENUS
    //}}

    //{{REGISTER_LISTENERS
    SymAction lSymAction = new SymAction();
    _start.addActionListener(lSymAction);
    _stop.addActionListener(lSymAction);
    _go.addActionListener(lSymAction);
    SymWindow aSymWindow = new SymWindow();
    this.addWindowListener(aSymWindow);
    //}}
    setLocation(32,32);
  }

  /**
   * Added by Visual Cafe.
   *
   * @param b True if the window is visible.
   */
  public void setVisible(boolean b)
  {
    if ( b )
      setLocation(50, 50);
    super.setVisible(b);
  }

  /**
   * The program entry point.
   *
   * @param args Command line arguments are not used.
   */
  static public void main(String args[])
  {
    (new WatchBBS()).setVisible(true);
  }

  /**
   * Added by Visual Cafe.
   */
  public void addNotify()
  {
    // Record the size of the window prior to calling parents addNotify.
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
    setSize(insets.left
            + insets.right
            + size.width,
            insets.top + insets.bottom
            + size.height + menuBarHeight);
  }

  // Used by addNotify
  boolean frameSizeAdjusted = false;

  //{{DECLARE_CONTROLS
  javax.swing.JLabel JLabel1 =
    new javax.swing.JLabel();

  /**
   * The URL of the BBS to scan.
   */
  javax.swing.JTextField _url =
    new javax.swing.JTextField();
  javax.swing.JLabel JLabel2 =
    new javax.swing.JLabel();

  /**
   * The polling frequency.
   */
  javax.swing.JTextField _minutes =
    new javax.swing.JTextField();
  javax.swing.JLabel JLabel3 =
    new javax.swing.JLabel();

  /**
   * The Start button.
   */
  javax.swing.JButton _start =
    new javax.swing.JButton();

  /**
   * The Stop button.
   */
  javax.swing.JButton _stop =
    new javax.swing.JButton();

  /**
   * The "Poll Now" button.
   */
  javax.swing.JButton _go =
    new javax.swing.JButton();

  /**
   * The displayed status.
   */
  javax.swing.JLabel _status =
    new javax.swing.JLabel();
  //}}

  //{{DECLARE_MENUS
  //}}


  /**
   * Added by Visual Cafe.
   *
   * @author Visual Cafe
   */
  class SymAction implements java.awt.event.ActionListener {
    public void actionPerformed(java.awt.event.ActionEvent event)
    {
      Object object = event.getSource();
      if ( object == _start )
        Start_actionPerformed(event);
      else if ( object == _stop )
        Stop_actionPerformed(event);
      else if ( object == _go )
        Go_actionPerformed(event);
    }
  }

  /**
   * Called when the start button is clicked.
   * This method starts up the background thread
   * and determines the date of the latest post,
   * at this time. This time will later be used
   * as a refference to determine if there are any
   * new messages.
   *
   * @param event The event.
   */
  void Start_actionPerformed(java.awt.event.ActionEvent event)
  {
    if ( _latest==null ) {
      _latest = getLatestDate();
      if ( _latest==null )
        return;
    }
    if ( !setNextPoll() )
      return;
    _thread = new Thread(this);
    _thread.start();
    _start.setEnabled(false);
    _stop.setEnabled(true);
    _go.setEnabled(true);
  }

  /**
   * Called when the stop button is clicked.
   * This method stops the background thread.
   *
   * @param event
   */
  void Stop_actionPerformed(java.awt.event.ActionEvent event)
  {
    _thread.stop();
    _start.setEnabled(true);
    _stop.setEnabled(false);
    _go.setEnabled(false);
  }

  /**
   * Called when the Poll Now button is cliked. Also
   * called when the background thread determines that
   * it is time to poll again.
   *
   * @param event The event.
   */
  void Go_actionPerformed(java.awt.event.ActionEvent event)
  {
    setNextPoll();
    Date update = getLatestDate();
    if ( !update.toString().equalsIgnoreCase(_latest.toString()) ) {
      _latest = update;
      Runnable doit = new Runnable()
      {
        public void run()
        {
          JOptionPane.showMessageDialog(null,
            "There are new messages at:" + _url.getText(),
            "New Messages",
            JOptionPane.OK_CANCEL_OPTION,
            null );
        }
      };

      SwingUtilities.invokeLater(doit);

    }
  }

  /**
   * Added by Visual Cafe
   *
   * @author Visual Cafe
   */
  class SymWindow extends java.awt.event.WindowAdapter {
    public void windowClosed(java.awt.event.WindowEvent event)
    {
      Object object = event.getSource();
      if ( object == WatchBBS.this )
        WatchBBS_windowClosed(event);
    }
  }

  /**
   * Called when the window is closed.
   *
   * @param event The event.
   */
  void WatchBBS_windowClosed(java.awt.event.WindowEvent event)
  {
    System.exit(0);

  }

  /**
   * Called to get the latest date that
   * a message was posted at the specified
   * BBS.
   *
   * @return A Data class of the last message date.
   */
  protected Date getLatestDate()
  {
    HTTPSocket http;
    Date latest = new Date(0,0,0);
    try {
      http = new HTTPSocket();
      http.send(_url.getText(),null);
    } catch ( Exception e ) {
      JOptionPane.showMessageDialog(this,
        e,
        "Error",
        JOptionPane.OK_CANCEL_OPTION,
        null );
      return null;
    }
    HTMLParser parse = new HTMLParser();
    parse.source = new StringBuffer(http.getBody());

    int foundTag = 0;
    String date = "";


    // find all the links
    while ( !parse.eof() ) {
      char ch = parse.get();
      if ( ch==0 ) {
        HTMLTag tag = parse.getTag();
        if ( tag.getName().equalsIgnoreCase("B") ) {
          foundTag = 2;
          date="";
        } else if ( tag.getName().equalsIgnoreCase("/FONT") ) {
          foundTag--;
          if ( foundTag==0 ) {
            Date d = parseDate(date);
            if ( d!=null ) {
              if ( d.after(latest) )
                latest = d;
            }
          }
        }
      } else {
        if ( (ch=='\r') || (ch=='\n') )
          ch=' ';
        date+=ch;
      }
    }
    return latest;
  }

  /**
   * Parse a date of the form:
   *
   * September 2, 2001 5:30 PM
   *
   * @param str The string form of the date.
   * @return A Date object that was parsed.
   */
  Date parseDate(String str)
  {
    String months[] = {"jan","feb","mar","apr","may",
      "jun","jul","aug","sep","oct","nov","dec"};
    Date rtn;
    try {
      rtn = new Date();
      // month
      String mth = str.substring(0,str.indexOf(' '));
      for ( int i=0;i<months.length;i++ ) {
        if ( mth.toLowerCase().startsWith(months[i]) ) {
          rtn.setMonth(i);
          break;
        }
      }

      // day

      str = str.substring(str.indexOf(' ')+1);
      String day = str.substring(0,str.indexOf(','));
      rtn.setDate(Integer.parseInt(day));

      // Year

      str = str.substring(str.indexOf(',')+1).trim();
      String year = str.substring(0,str.indexOf(' '));
      rtn.setYear(Integer.parseInt(year)-1900);

      // Hour

      str = str.substring(str.indexOf(' ')+1).trim();
      String hour = str.substring(0,str.indexOf(':'));
      rtn.setHours(Integer.parseInt(hour));

      // Minute

      str = str.substring(str.indexOf(':')+1).trim();
      String minutes = str.substring(0,str.indexOf(' '));
      rtn.setMinutes(Integer.parseInt(minutes));
      rtn.setSeconds(0);

      // AM or PM
      str = str.substring(str.indexOf(' ')+1).trim();
      if ( str.toUpperCase().charAt(0)=='P' )
        rtn.setHours(rtn.getHours()+12);

      return rtn;
    } catch ( Exception e ) {
      return null;
    }
  }

  /**
   * This run method is called to execute
   * the background thread.
   */
  public void run()
  {
    while ( true ) {
      Runnable doit = new Runnable()
      {
        public void run()
        {
          Date d = new Date();
          long milli = (_nextPoll.getTime()-d.getTime())/1000;
          _status.setText("Will poll in " + milli + " seconds.");
        }
      };

      if ( _nextPoll.before(new Date()) ) {
        Go_actionPerformed(null);
      }

      SwingUtilities.invokeLater(doit);
      try {
        Thread.sleep(1000);
      } catch ( InterruptedException e ) {
      }
    }
  }

  /**
   * Called to determine the next time
   * that a poll will take place.
   *
   * @return True on success, false on failure.
   */
  public boolean setNextPoll()
  {
    try {
      int minutes = Integer.parseInt(_minutes.getText());
      Date d = new Date();
      d.setMinutes(d.getMinutes()+minutes);
      _nextPoll = d;
      return true;
    } catch ( Exception e ) {
      JOptionPane.showMessageDialog(this,
        e,
        "Invalid Polling Time",
        JOptionPane.OK_CANCEL_OPTION,
        null );
      return false;
    }
  }
}