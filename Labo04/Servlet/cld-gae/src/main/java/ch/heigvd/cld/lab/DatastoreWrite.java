package ch.heigvd.cld.lab;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

@WebServlet(name = "DatastoreWrite", value = "/datastorewrite")
public class DatastoreWrite extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String entityKind = request.getParameter("_kind");
        String entityKey = request.getParameter("_key");

        if (entityKind == null) {
            response.getWriter().println("Error: _kind parameter is mandatory");
            return;
        }

        Entity entity;
        if (entityKey != null) {
            entity = new Entity(entityKind, entityKey);
        } else {
            entity = new Entity(entityKind);
        }

        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            if (!paramName.equals("_kind") && !paramName.equals("_key")) {
                String paramValue = request.getParameter(paramName);
                entity.setProperty(paramName, paramValue);
            }
        }

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(entity);

        response.getWriter().println("Entity written to datastore with kind " + entityKind +
                (entityKey != null ? " and key " + entityKey : ""));
    }
}