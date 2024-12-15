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

@WebServlet("/energy-calculator")
public class EnergyCalculatorServlet extends HttpServlet {
    private static final String ONTOLOGY_FILE = "/WEB-INF/resources/newtons_laws.owl";
    private static final String NAMESPACE = "http://example.org/ontology#";
    private static final String CONCEPT_KINETIC_ENERGY = "KineticEnergy";
    private static final String CONCEPT_POTENTIAL_ENERGY = "PotentialEnergy";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        Gson gson = new Gson();
        OntModel model = loadOntology(req);

        try {
            String action = req.getParameter("action");

            if ("calculateKineticEnergy".equals(action)) {
                double mass = parseDoubleParameter(req, "mass");
                double velocity = parseDoubleParameter(req, "velocity");
                double kineticEnergy = 0.5 * mass * Math.pow(velocity, 2);

                List<String> relatedConcepts = getRelatedConcepts(model, CONCEPT_KINETIC_ENERGY);
                EnergyResponse response = new EnergyResponse("Kinetic Energy", kineticEnergy, relatedConcepts);
                writeResponse(resp, gson.toJson(response));

            } else if ("calculatePotentialEnergy".equals(action)) {
                double mass = parseDoubleParameter(req, "mass");
                double height = parseDoubleParameter(req, "height");
                double gravity = 9.8; // Constant for Earth's gravity
                double potentialEnergy = mass * gravity * height;

                List<String> relatedConcepts = getRelatedConcepts(model, CONCEPT_POTENTIAL_ENERGY);
                EnergyResponse response = new EnergyResponse("Potential Energy", potentialEnergy, relatedConcepts);
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

    static class EnergyResponse {
        String energyType;
        double energyValue;
        List<String> relatedConcepts;

        EnergyResponse(String energyType, double energyValue, List<String> relatedConcepts) {
            this.energyType = energyType;
            this.energyValue = energyValue;
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
