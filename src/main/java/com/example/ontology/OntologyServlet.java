package com.example.ontology;

import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.RDFS;

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

@WebServlet("/physics-tutor")
public class OntologyServlet extends HttpServlet {
    private static final String ONTOLOGY_FILE = "/WEB-INF/resources/newtons_laws.owl";
    private static final String NAMESPACE = "http://example.org/ontology#";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        Gson gson = new Gson();

        OntModel model = loadOntology(req);

        try {
            String action = req.getParameter("action");

            if ("calculateForce".equals(action)) {
                double mass = Double.parseDouble(req.getParameter("mass"));
                double acceleration = Double.parseDouble(req.getParameter("acceleration"));
                double force = mass * acceleration;

                List<String> relatedConcepts = getRelatedConcepts(model, "Force");

                PhysicsTutorResponse response = new PhysicsTutorResponse(force, relatedConcepts);
                writeResponse(resp, gson.toJson(response));

            } else if ("getLaws".equals(action)) {
                List<NewtonLawResponse> laws = getNewtonLaws(model);
                writeResponse(resp, gson.toJson(laws));

            } else if ("getEnergyRules".equals(action)) {
                List<RuleResponse> rules = getEnergyRules(model);
                writeResponse(resp, gson.toJson(rules));

            } else {
                throw new IllegalArgumentException("Invalid action");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeResponse(resp, gson.toJson("Invalid input: " + e.getMessage()));
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
            for (OntProperty property : conceptClass.listDeclaredProperties().toList()) {
                relatedConcepts.add(property.getLocalName());
            }
        }
        return relatedConcepts;
    }

    private List<NewtonLawResponse> getNewtonLaws(OntModel model) {
        List<NewtonLawResponse> laws = new ArrayList<>();
        OntClass lawClass = model.getOntClass(NAMESPACE + "NewtonLaw");

        if (lawClass != null) {
            for (OntResource law : lawClass.listInstances().toList()) {
                String name = law.getLocalName();
                String description = law.getProperty(RDFS.comment).getString();
                laws.add(new NewtonLawResponse(name, description));
            }
        }
        return laws;
    }

    private List<RuleResponse> getEnergyRules(OntModel model) {
        List<RuleResponse> rules = new ArrayList<>();
        Resource kineticEnergyRule = model.getResource(NAMESPACE + "KineticEnergyRule");
        Resource workRule = model.getResource(NAMESPACE + "WorkRule");
        Resource powerRule = model.getResource(NAMESPACE + "PowerRule");

        if (kineticEnergyRule != null) {
            rules.add(new RuleResponse("KineticEnergyRule", kineticEnergyRule.getProperty(RDFS.comment).getString()));
        }

        if (workRule != null) {
            rules.add(new RuleResponse("WorkRule", workRule.getProperty(RDFS.comment).getString()));
        }

        if (powerRule != null) {
            rules.add(new RuleResponse("PowerRule", powerRule.getProperty(RDFS.comment).getString()));
        }
        

        return rules;
    }

    private void writeResponse(HttpServletResponse resp, String jsonResponse) throws IOException {
        PrintWriter out = resp.getWriter();
        out.print(jsonResponse);
        out.flush();
    }

    static class PhysicsTutorResponse {
        double force;
        List<String> relatedConcepts;

        PhysicsTutorResponse(double force, List<String> relatedConcepts) {
            this.force = force;
            this.relatedConcepts = relatedConcepts;
        }

        public double getForce() {
            return force;
        }

        public List<String> getRelatedConcepts() {
            return relatedConcepts;
        }
    }

    static class NewtonLawResponse {
        String name;
        String description;

        NewtonLawResponse(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }

    static class RuleResponse {
        String ruleName;
        String ruleDescription;

        RuleResponse(String ruleName, String ruleDescription) {
            this.ruleName = ruleName;
            this.ruleDescription = ruleDescription;
        }

        public String getRuleName() {
            return ruleName;
        }

        public String getRuleDescription() {
            return ruleDescription;
        }
    }
    
    
}
