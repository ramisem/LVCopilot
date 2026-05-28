/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.RequestDispatcher
 *  javax.servlet.ServletException
 *  javax.servlet.ServletRequest
 *  javax.servlet.ServletResponse
 *  javax.servlet.http.HttpServlet
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package sapphire.servlet;

import com.labvantage.sapphire.Trace;
import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BaseHttpServlet
extends HttpServlet {
    public void doInit() {
    }

    protected void setNameserverlist(String nameserverlist) {
    }

    protected String getNameserverlist() {
        return "";
    }

    protected void logTrace(String tracemsg) {
        Trace.log("SERVLET", tracemsg);
    }

    protected void logError(String errormsg) {
        Trace.log("SERVLET", "ERROR: " + errormsg);
    }

    protected void logError(String errormsg, Exception exception) {
        Trace.log("SERVLET", "ERROR: " + errormsg);
    }

    public void goErrorPage(HttpServletRequest request, HttpServletResponse response, String errorpage, String errormsg) {
        this.logError(errormsg);
        if (errorpage == null || errorpage.length() == 0) {
            errorpage = this.getServletContext().getInitParameter("errorpage");
        }
        if (errorpage != null && errorpage.length() > 0) {
            RequestDispatcher rd = this.getServletContext().getRequestDispatcher("/" + errorpage + "?errormsg=" + errormsg);
            try {
                rd.forward((ServletRequest)request, (ServletResponse)response);
            }
            catch (ServletException e) {
                this.logError("Failed to forward to error page: " + e.getMessage());
            }
            catch (IOException e) {
                this.logError("Failed to forward to error page: " + e.getMessage());
            }
        } else {
            this.logError("No error page specified");
        }
    }
}

