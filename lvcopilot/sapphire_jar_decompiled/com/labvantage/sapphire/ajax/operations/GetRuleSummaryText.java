/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.ajax.operations;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.TreeSet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class GetRuleSummaryText
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        TranslationProcessor tp = this.getTranslationProcessor();
        String ruledef = ajaxResponse.getRequestParameter("ruledef");
        String rule = ajaxResponse.getRequestParameter("rule");
        String rowid = ajaxResponse.getRequestParameter("rowid");
        StringBuffer summary = new StringBuffer();
        QueryProcessor qp = this.getQueryProcessor();
        try {
            JSONObject ruleobj = new JSONObject(ruledef);
            JSONObject rules = ruleobj.getJSONObject("rules");
            TreeSet ts = new TreeSet();
            Iterator it = rules.keys();
            while (it.hasNext()) {
                ts.add(it.next());
            }
            it = ts.iterator();
            int counter = 0;
            while (it.hasNext()) {
                String key = it.next().toString();
                JSONObject ruledefObj = rules.getJSONObject(key);
                String ruletype = ruledefObj.getString("ruletype");
                String actionflag = ruledefObj.getString("actionflag");
                if (actionflag.equals("D")) continue;
                ++counter;
                String className = ruletype + rule + "RuleProcessor";
                Class<?> processorclass = null;
                try {
                    processorclass = Class.forName("com.labvantage.sapphire.pages.prodvariantruleeditor." + className);
                }
                catch (ClassNotFoundException e) {
                    try {
                        processorclass = Class.forName(this.getCustomRuleClassName(rule, ruletype));
                    }
                    catch (ClassNotFoundException e1) {
                        throw new ServletException((Throwable)e1);
                    }
                    catch (SapphireException e1) {
                        throw new ServletException((Throwable)e1);
                    }
                }
                Constructor<?> c = processorclass.getConstructor(new Class[0]);
                Object o = c.newInstance(new Object[0]);
                Class[] partypes = new Class[]{String.class, QueryProcessor.class, TranslationProcessor.class};
                Method m = processorclass.getMethod("getSummary", partypes);
                summary.append(counter + (String)m.invoke(o, ruledefObj.toString(), qp, tp)).append("<br>");
            }
        }
        catch (JSONException e) {
            throw new ServletException((Throwable)e);
        }
        catch (InvocationTargetException e) {
            throw new ServletException((Throwable)e);
        }
        catch (NoSuchMethodException e) {
            throw new ServletException((Throwable)e);
        }
        catch (IllegalAccessException e) {
            throw new ServletException((Throwable)e);
        }
        catch (InstantiationException e) {
            throw new ServletException((Throwable)e);
        }
        ajaxResponse.addCallbackArgument("summary", summary);
        ajaxResponse.addCallbackArgument("rule", rule);
        ajaxResponse.addCallbackArgument("rowid", rowid);
        ajaxResponse.print();
    }

    private String getCustomRuleClassName(String rule, String ruletype) throws SapphireException {
        String className = "";
        PropertyList policy = this.getConfigurationProcessor().getPolicy("SamplingPlanPolicy", "Sapphire Custom");
        if (policy != null) {
            PropertyListCollection customRules = policy.getCollectionNotNull("customrules");
            for (int i = 0; i < customRules.size(); ++i) {
                PropertyList ruleDef = customRules.getPropertyList(i);
                String ruleTypeValueFromPolicy = ruleDef.getProperty("ruletype");
                String ruleFromPolicy = ruleDef.getProperty("rule");
                if (!ruleTypeValueFromPolicy.equals(ruletype) || ruleFromPolicy.indexOf(rule) == -1) continue;
                className = ruleDef.getProperty("classname");
                break;
            }
        }
        return className;
    }
}

