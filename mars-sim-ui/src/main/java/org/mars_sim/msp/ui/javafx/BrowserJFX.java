/* Mars Simulation Project
 * BrowserJFX.java
 * @version 3.08 2015-12-31
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.javafx;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.scene.web.WebHistory.Entry;
import javafx.scene.web.WebHistory;
//import javafx.scene.text.Text;

import netscape.javascript.JSObject;
//import javax.swing.*;
//import javax.swing.event.HyperlinkEvent;

import org.codefx.libfx.control.webview.WebViewHyperlinkListener;
import org.codefx.libfx.control.webview.WebViews;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.ui.swing.tool.guide.GuideWindow;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import java.awt.*;
import java.awt.event.*;
import java.net.MalformedURLException;
import java.net.URL;
//import java.util.EventListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent.EventType;

import static javafx.concurrent.Worker.State.FAILED;
import static javafx.concurrent.Worker.State;

public class BrowserJFX {

    public static final String EVENT_TYPE_CLICK = "click";
    public static final String EVENT_TYPE_MOUSEOVER = "mouseover";
    public static final String EVENT_TYPE_MOUSEOUT = "mouseclick";
 
    public static final String MSP_HEADER = "msp://";
    public static final String HTTP_HEADER = "http://";
    public static final String HTTPS_HEADER = "https://";
    
    public static final String DEFAULT_JQUERY_MIN_VERSION = "1.7.2";
    public static final String JQUERY_LOCATION = "http://code.jquery.com/jquery-1.7.2.min.js";

    public String textInputCache;
    
    private static final String CSS = 
    		"a, a:link, a:visited, a:hover{color:rgb(184, 134, 11); text-decoration:none;}"
    		
          + "body {"
          + "    background-color: rgb(50, 50, 50); "
          + "    font-family: Arial, Helvetica, san-serif;"
          + "}"
          
          + "body, h3{font-size:14pt;line-height:1.1em; color:white;}"
          + "h2{font-size:14pt; font-weight:700; line-height:1.2em; text-align:center; color:white;}"
          + "h3{font-weight:700; color:white;}"
          + "p{margin-left:13pt; color:white;}"
          + "hr{width:90%;}";
		 
    
	private boolean isLocalHtml = false, isInternal = false;

    private final JFXPanel jfxPanel = new JFXPanel();
    private JPanel panel = new JPanel(new BorderLayout());
    private final JLabel statusBarLbl = new JLabel();
    private final JButton btnGo = new JButton("Go");
    private final JTextField urlTF = new JTextField();
    private final JProgressBar progressBar = new JProgressBar();

    private WebView view;
    
    private WebEngine engine;
    
    private WebHistory history;
    
    //private GuideWindow guideWindow;
    
    public BrowserJFX(){ //GuideWindow gw) {
    	//this.guideWindow = gw;
        Platform.runLater(() -> {       
            view = new WebView();
            engine = view.getEngine();          
            history = engine.getHistory();
        });
        
        initJFX();
        panel = init();
    }

    
    // 2016-04-22 Added ability to interpret internal commands
    public JPanel init() {
        
        ActionListener al = new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
            	
            	highlight();
            	
        		String input = urlTF.getText().trim();        		
            	//System.out.println("BrowserJFX's actionPerformed() : input is [" + input + "]");          	

        		if (input != null && !input.isEmpty()) {
        			// if the address bar is not empty
        			getURLType(input);
        		}
        		
            }
        };
        
        btnGo.addActionListener(al);
        urlTF.addActionListener(al);			

        progressBar.setPreferredSize(new Dimension(150, 18));
        progressBar.setStringPainted(true);

        JPanel topBar = new JPanel(new BorderLayout(5, 0));
        topBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        topBar.add(urlTF, BorderLayout.CENTER);
        topBar.add(btnGo, BorderLayout.EAST);

        JPanel statusBar = new JPanel(new BorderLayout(5, 0));
        statusBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        statusBar.add(statusBarLbl, BorderLayout.CENTER);
        statusBar.add(progressBar, BorderLayout.EAST);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(jfxPanel, BorderLayout.CENTER);
        panel.add(statusBar, BorderLayout.SOUTH);

        return panel;
    }

    
    public void getURLType(String input) {
    	if (input != null && !input.isEmpty()) {
    		textInputCache = input; 

    		if (input.equals("ticket") 
    				|| input.equals("map1")
    				|| input.equals("globe1")) {
    			parseInput(input, 0);
    		}
    		else if (input.contains("file:/")) {
    				parseInput(input, 1); //or 3   			
    		}
    		else {
    			parseInput(input, 2);
        	}
		}
    	else {
			parseInput(input, 1); //or 3 
    	}
	
    }
    
    // 2016-04-22 Added parseInput()
    public void parseInput(String input, int URL_type) {
		
		// Type 0 is internal command
		if (URL_type == 0)  {
			isInternal = true;
			isLocalHtml = true;
			//System.out.println("BrowserJFX : input is " + input);
			updateURL(input + ".html", 0);
			//addCSS();
	    }
		// Type 1 is local html file 
		else if (URL_type == 1) {
			isLocalHtml = true;	
			isInternal = false;
			updateURL(input, 1);
			addCSS();
		}
		
		else {

			isInternal = false;
			
			boolean status = input.toLowerCase().contains(HTTPS_HEADER);          	
			
			// Type 2 is a remote url 
			if (status) {
				isLocalHtml = false;
				updateURL(input, 2);
			}
			else {
				status = input.toLowerCase().contains(HTTP_HEADER);          	
				//pos = input.toLowerCase().indexOf(HTTP_HEADER.toLowerCase());
				
				// Type 2 is a remote url 
				if (status) {
					isLocalHtml = false;
					updateURL(input, 2);
				}
				else {
					
					// Type 3 is a remote url that has no "http://"
					// e.g. type in google.com
					isLocalHtml = false;
					// will need to add http://
					updateURL(input, 3);									
				}				
			}
			
			
		}
    }

    
    @SuppressWarnings("restriction")
	private void initJFX() {

    	java.net.CookieHandler.setDefault(null);
    	
        Platform.runLater(() -> {  
                  
                WebViewHyperlinkListener eventPrintingListener = event -> {
                	
                	if (event.getEventType() == EventType.ACTIVATED) {
	                	String input = event.getURL().toString();
	                     
	                    boolean status0 = input.toLowerCase().contains(HTTP_HEADER.toLowerCase());          	
	                    boolean status1 = input.toLowerCase().contains(HTTPS_HEADER.toLowerCase());          	
	
	                    if (status0 || status1) {    					
	    					isLocalHtml = false;    					
	        			}
	    				else {
	    					isLocalHtml = true;
	    				}
                	}

                    return false;
                };
                
                WebViews.addHyperlinkListener(view, eventPrintingListener);
                
                view.setStyle("-fx-background-color: #656565;"
                		+ " -fx-font-color: white;"
                		+ " -fx-border-color: #00a7c8");


                Button reloadB = new Button("Refresh");
                reloadB.setMaxWidth(110);

                Button backB = new Button("Back");
                backB.setMaxWidth(110);

                Button forwardB = new Button("Forward");
                forwardB.setMaxWidth(110);
                
                reloadB.setOnAction(e -> engine.reload());
                
                backB.setOnAction(e -> {    
                	
                	engine.executeScript("history.back()");              	
                	String input = urlTF.getText().trim();
      
                	getURLType(input);
                	System.out.println("calling history.back()");
 	
                });
                
                forwardB.setOnAction(e -> {  
                	
                	engine.executeScript("history.forward()");
                	String input = urlTF.getText().trim();
                 	
                	getURLType(input);
                	System.out.println("calling history.forward()");
                });
                
/*
                engine.titleProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, final String newValue) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                //SimpleSwingBrowser.this.setTitle(newValue);
                            }
                        });
                    }
                });
*/
                
                
                // show the url address whenever a mouse hovers over a hyperlink
                engine.setOnStatusChanged(new EventHandler<WebEvent<String>>() {
                    @Override
                    public void handle(final WebEvent<String> event) {
                        SwingUtilities.invokeLater(()->{
                        	//System.out.println("BrowserJFX : hovering over a hyperlink, calling setOnStatusChanged() to display its url on the status bar");
                        	// Note: it shows the content of the hyperlink (even before the user clicks on it.
                            String content = event.getData();
                            if (content != null && !content.isEmpty()) {
                            	     
                            	// 2016-06-07 Truncated off the initial portion of the path to look more "user-friendly"/improve viewing comfort.
                            	if (content.contains("docs")) {                            	
                            		int i = content.indexOf("docs")-1;
                            		statusBarLbl.setText(content.substring(i, content.length()-1));          		
                            	}
                            	else
                            		// this is a remote link or internal link
                            		statusBarLbl.setText(content);
                            }
                        });
                    }
                });

                engine.locationProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> ov, String oldValue, final String newValue) {
                        
                    	Platform.runLater(()-> {	
	                        JSObject jsobj = (JSObject) engine.executeScript("window");
	        				jsobj.setMember("JavaBridge", new TicketSubmission());  
                    	});
                    	                   	
                    	SwingUtilities.invokeLater(() ->{
                    		textInputCache = newValue;
                       		setURLText();
                    	});
                    }
                });

                engine.getLoadWorker().workDoneProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, final Number newValue) {
                        SwingUtilities.invokeLater(()->{
                        	//System.out.println("workDoneProperty()");
                                progressBar.setValue(newValue.intValue());
                        });
                    }
                });

                engine.getLoadWorker().exceptionProperty().addListener(new ChangeListener<Throwable>() {
                	@Override
	                public void changed(ObservableValue<? extends Throwable> o, Throwable old, final Throwable value) {
	                	if (engine.getLoadWorker().getState() == FAILED) {
	                		SwingUtilities.invokeLater(()-> {
	                				//System.out.println("BrowserJFX : worker.getState() == FAILED, calling exceptionProperty()");
	/*                				JOptionPane.showMessageDialog(
	                                            panel,
	                                            (value != null) ?
	                                            engine.getLocation() + "\n" + value.getMessage() :
	                                            engine.getLocation() + "\nUnexpected error.",
	                                            "Loading error...",
	                                            JOptionPane.ERROR_MESSAGE);
	*/
		                		});
		                	}	             	
		                }
		            }              
                );
 /*                             
                engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {                  
                    	System.out.println("calling stateProperty");                   	                   	                   	
                    	getURLType(getCurrentURL());                 	
                        JSObject jsobj = (JSObject) engine.executeScript("window");
        				jsobj.setMember("JavaBridge", new TicketSubmission());  	
                    }
                });
*/            
                // process page loading
                engine.getLoadWorker().stateProperty().addListener(
                    new ChangeListener<State>() {
                        @Override
                        public void changed(ObservableValue<? extends State> ov,
                            State oldState, State newState) {                           
                                if (newState == State.SUCCEEDED) {
                                	//System.out.println("BrowserJFX : clicking on a hyperlink, calling stateProperty()");                              	
                                	getURLType(getCurrentURL());                             	
                                	if (!isLocalHtml)                                		
	                                	SwingUtilities.invokeLater(() ->setURLText());
                                }
                            }
                        }
                );
                
                jfxPanel.setScene(new Scene(view));
        });
    }

    /*
     * Set the url text in the address textfield 
     */
    public void setURLText() {
		//System.out.println("isLocalHtml : " + isLocalHtml + "   isInternal : " + isInternal);
    	if (isLocalHtml) {
     		if (isInternal){
        		;//urlTF.setText(urlTF.getText());
     		}
    		else {
    			// if it is a local html file, show blank on the address bar
    			urlTF.setText("");
    			//urlTF.setText(textInputCache);
    		}
     	}
    	else {
     		urlTF.setText(textInputCache);
     	} 	
    }
    
    public void addCSS() { 	
	   	//System.out.println("adding css");
    	Document doc = engine.getDocument() ;
	    Element styleNode = doc.createElement("style");
	    Text styleContent = doc.createTextNode(CSS);
	    styleNode.appendChild(styleContent);
	    doc.getDocumentElement().getElementsByTagName("head").item(0).appendChild(styleNode);
    }
    
	// 2016-04-18 Added updateURL()
    public void updateURL(String href, int URL_type) {
    	if (href != null && !href.isEmpty()) {
	    	if (URL_type == 0) {
		    	URL url = getClass().getResource(Msg.getString("doc.help") + href);
		    	//System.out.println("BrowserJFX : updateHistory(). Type " + URL_type + ". url is " + href);
		    	loadLocalURL(url.toExternalForm());
	    	}
	    	else if (URL_type == 1) {
		    	//URL url = getClass().getResource(Msg.getString("doc.help") + href);
		    	//System.out.println("BrowserJFX : updateHistory(). Type " + URL_type + ". url is " + href);
		    	//guideWindow.updateHistory(url);
		    	//guideWindow.updateButtons(); 
		    	//loadLocalURL(url.toExternalForm());
		    	//loadLocalURL(href);
				//isLocalHtml = true;	
				//isInternal = false;
	    	}
	    	else if (URL_type == 2) {
	    		//e.g. if user types in "hi", url is "http://hi/", it will trip off exceptionProperty()
	    		//try {
	    			//URL url = new URL(href);
	    	    	//System.out.println("BrowserJFX : updateHistory(). Type 2. url is " + href);
					//guideWindow.updateHistory(url);
			    	//guideWindow.updateButtons();
			    	//loadRemoteURL(url.toExternalForm());
			    	//loadRemoteURL(href);
				//} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
				//	e.printStackTrace();
				//}
				//isLocalHtml = true;	
				//isInternal = false;
	    	}
	    	else if (URL_type == 3) {
	    		try {
	    			URL url = new URL(HTTP_HEADER + href);
	    	    	//System.out.println("BrowserJFX : updateHistory(). Type 3. url is [" + url + "]");
					//guideWindow.updateHistory(url);
			    	//guideWindow.updateButtons();
			    	loadRemoteURL(url.toExternalForm());
					//isLocalHtml = true;	
					//isInternal = false;
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}
    	}
    }
   
    @SuppressWarnings("restriction")
	public void loadRemoteURL(final String input) {
    	isLocalHtml = false;
    	isInternal = false;
    	
        Platform.runLater(()-> {    				
			boolean status = input.toLowerCase().contains(HTTPS_HEADER.toLowerCase());          	
		
			if (status) {
				engine.load(input);
			}
			else {
				status = input.toLowerCase().contains(HTTP_HEADER.toLowerCase());          	
			
				if (status) {
					engine.load(input);
				}
				else {
					
					if (input != null && !input.isEmpty()) {
						//System.out.println("BrowserJFX's loadRemoteURL() : input is [" + input +"]");
						engine.load(HTTP_HEADER + input);
						//System.out.println("input is " + HTTP_HEADER + input);
					}
					// TODO: should it try https as well ?					
					// TODO: how to handle the case when the remote url is bad ?
					// should delete this bad url and its history index, instead of saving it
					
				}				
			}		
            // TODO: how to check if the url is valid or if it's loaded successfully?
        });
        
    }

    @SuppressWarnings("restriction")
	public void loadLocalURL(String content) {
       	isLocalHtml = true;      	  	
        Platform.runLater(()-> {
            engine.load(content);
            if (content != null && !content.isEmpty()) {
       	     
            	// 2016-06-07 Truncated off the initial portion of the path to look more "user-friendly"/improve viewing comfort.
            	if (content.contains("docs")) {                            	
            		int i = content.indexOf("docs")-1;
            		statusBarLbl.setText(content.substring(i, content.length()-1));          		
            	}
            	else
            		// this is a remote link or internal link
            		statusBarLbl.setText(content);
            }
        });
        
    }

    private void highlight() {
        //System.out.println("highlight()");
        Platform.runLater(() -> {        
                engine.setJavaScriptEnabled(true);
                //executejQuery(engine, " $(\"a\").css(\"color\", \"red\")");
                //engine.setJavaScriptEnabled(false);
        });
    }
    
/*   
    private static Object executejQuery(final WebEngine engine, String minVersion, String script) {
        return executejQuery(engine, DEFAULT_JQUERY_MIN_VERSION, script);//JQUERY_LOCATION, script);
    }
 
    private Object executejQuery(final WebEngine engine, String script) {
        return executejQuery(engine, DEFAULT_JQUERY_MIN_VERSION, script);
    } 
*/      
    
    @SuppressWarnings("restriction")
	public String getCurrentURL() {    
        //history = engine.getHistory();
        ObservableList<WebHistory.Entry> entryList = history.getEntries();
        int currentIndex = history.getCurrentIndex();

        String txt = entryList.get(currentIndex).getUrl();
        //System.out.println("currentIndex is " + currentIndex + " url is " + txt);       
        //Platform.runLater(() -> { history.go(0);} );           
        return txt;
      }
    
    public JPanel getPanel() {
    	return panel;
    }
    
    public JLabel getStatusBarLabel() {
    	return statusBarLbl;
    }
}

class TicketSubmission {
	public String submit(String name) {
		return "Hi," + name;
	}
}