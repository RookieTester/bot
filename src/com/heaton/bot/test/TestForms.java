/*
 * TestForms.java
 *
 * Created on August 18, 2003, 9:03 PM
 */

package com.heaton.bot.test;
import com.heaton.bot.*;
import java.util.*;

/**
 *
 * @author  jheaton
 */
public class TestForms {
    
    /** Creates a new instance of TestForms */
    public TestForms() {
    }
    
    public static void test() {
        try {
            System.out.print("Forms test:");
            //setup connection
            HTTP http=new HTTPSocket();
            http.setTimeout(60000);
            http.setUseCookies(true,true);
            
            HTMLPage page=new HTMLPage(http);
            page.open("http://www.jeffheaton.com/test/testform.asp",null);
            
            //let's get the forms
            Vector vec = page.getForms();
            if( vec.size()!=1 )
            {
                System.out.println("Invalid number of forms");
                return;
            }
            
            HTMLForm form = (HTMLForm)page.getForms().elementAt(0);
            
            if( !form.getMethod().equals("post") )
            {
                System.out.println("Invalid form method");
                return;                
            }
            
            if( !form.getAction().equals("http://www.jeffheaton.com/test/testform.asp") )
            {
                System.out.println("Invalid form action:" + form.getAction() );
                return;                
            }
            
            Attribute field1 = form.get("field1");
            Attribute field2 = form.get("field2");
            
            if( field1==null )
            {
                System.out.println("Missing field1");
                return;
            }
            
            if( field2==null )
            {
                System.out.println("Missing field2");
                return;
            }
            
            if( !field1.getValue().equals("default1") )
            {
                System.out.println("Failed to get default value for field1:" + form.get("field1") );
                return;                                
            }
            
            if( !field2.getValue().equals("default2") )
            {
                System.out.println("Failed to get default value for field2");
                return;                                
            }            
            
            field1.setValue("new1");
            field2.setValue("new2");
            
            page.post(form);
            
            String str = page.getHTTP().getBody();
            if( str.indexOf("new1")==-1 || str.indexOf("new2")==-1 )
            {
                System.out.println("Failed to get response on post");
                return;                                                
            }
            
            System.out.println("Success");
            

        }
        
        catch (Exception e) {
            System.out.println("error " + e);
        }
        
    }
    
}
