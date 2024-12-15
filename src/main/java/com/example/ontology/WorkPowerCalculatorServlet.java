package com.example.ontology;

import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.iterator.ExtendedIterator;

import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/work-power-calculator")
public class WorkPowerCalculatorServlet extends HttpServlet {
    private static final String ONTOLOGY_FILE = "/WEB-INF/resources/newtons_laws.owl";
    private static final String NAMESPACE = "http://example.org/ontology#";
    private static final String CONCEPT_WORK = "Work";
    private static final String CONCEPT_POWER = "Power";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        Gson gson = new Gson();
        OntModel model = loadOntology(req);

        try {
            String action = req.getParameter("action");

            if ("calculateWork".equals(action)) {
                double force = parseDoubleParameter(req, "force");
                double displacement = parseDoubleParameter(req, "displacement");
                double angle = Math.toRadians(parseDoubleParameter(req, "angle"));
                double work = force * displacement * Math.cos(angle);

                List<String> relatedConcepts = getRelatedConcepts(model, CONCEPT_WORK);
                PhysicsResponse response = new PhysicsResponse("Work", work, relatedConcepts);
                writeResponse(resp, gson.toJson(response));

            } else if ("calculatePower".equals(action)) {
                double work = parseDoubleParameter(req, "work");
                double time = parseDoubleParameter(req, "time");
                double power = work / time;

                List<String> relatedConcepts = getRelatedConcepts(model, CONCEPT_POWER);
                PhysicsResponse response = new PhysicsResponse("Power", power, relatedConcepts);
                writeResponse(resp, gson.toJson(response));

            } else {
                throw new IllegalArgumentException("Invalid action");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ErrorResponse errorResponse = new ErrorResponse("Invalid Request", e.getMessage());
            writeResponse(resp, gson.toJson(errorResponse));
        }
    }

    private double parseDoubleParameter(HttpServletRequest req, String paramName) throws IllegalArgumentException {
        String paramValue = req.getParameter(paramName);
        if (paramValue == null || paramValue.isEmpty()) {
            throw new IllegalArgumentException(paramName + " parameter is missing or empty.");
        }
        try {
            return Double.parseDouble(paramValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(paramName + " parameter must be a valid number.");
        }
    }

    private OntModel loadOntology(HttpServletRequest req) {
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        String ontologyPath = getServletContext().getRealPath(ONTOLOGY_FILE);
        FileManager.get().readModel(model, ontologyPath);
        return model;
    }

    private List<String> getRelatedConcepts(OntModel model, String conceptName) {
        List<String> relatedConcepts = new ArrayList<>();
        OntClass conceptClass = model.getOntClass(NAMESPACE + conceptName);
        if (conceptClass != null) {
            for (ExtendedIterator<? extends OntResource> it = conceptClass.listInstances(); it.hasNext(); ) {
                OntResource instance = it.next();
                if (instance.isURIResource()) {
                    relatedConcepts.add(instance.getLocalName());
                }
            }
        } else {
            relatedConcepts.add("No related concepts found for " + conceptName);
        }
        return relatedConcepts;
    }

    private void writeResponse(HttpServletResponse resp, String jsonResponse) throws IOException {
        PrintWriter out = resp.getWriter();
        out.print(jsonResponse);
        out.flush();
    }

    static class PhysicsResponse {
        String calculationType;
        double resultValue;
        List<String> relatedConcepts;

        PhysicsResponse(String calculationType, double resultValue, List<String> relatedConcepts) {
            this.calculationType = calculationType;
            this.resultValue = resultValue;
            this.relatedConcepts = relatedConcepts;
        }
    }

    static class ErrorResponse {
        String error;
        String message;

        ErrorResponse(String error, String message) {
            this.error = error;
            this.message = message;
        }
    }
}
