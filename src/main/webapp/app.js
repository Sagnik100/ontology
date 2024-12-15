document.addEventListener("DOMContentLoaded", () => {
    const forceForm = document.getElementById("forceForm");
    const energyForm = document.getElementById("energyForm");
    const workPowerForm = document.getElementById("workPowerForm");

    const resultsDiv = document.getElementById("results");
    const resultOutput = document.getElementById("resultOutput");
    const relatedConceptsList = document.getElementById("relatedConcepts");
    const errorDiv = document.getElementById("error");
    const errorMessage = document.getElementById("errorMessage");

    // Hide and Show Results
    const showResults = (message, concepts) => {
        resultOutput.textContent = message;
        relatedConceptsList.innerHTML = "";
        concepts.forEach((concept) => {
            const li = document.createElement("li");
            li.textContent = concept;
            relatedConceptsList.appendChild(li);
        });
        resultsDiv.classList.remove("hidden");
        errorDiv.classList.add("hidden");
    };

    const showError = (message) => {
        errorMessage.textContent = message;
        errorDiv.classList.remove("hidden");
        resultsDiv.classList.add("hidden");
    };

    const fetchCalculation = (url, body) => {
        return fetch(url, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
            },
            body: body,
        }).then((response) => {
            if (!response.ok) {
                return response.json().then((err) => {
                    throw new Error(err.message || "Server error. Please try again.");
                });
            }
            return response.json();
        });
    };

    // Force Calculation
    forceForm.addEventListener("submit", (event) => {
        event.preventDefault();
        const mass = parseFloat(document.getElementById("mass").value);
        const acceleration = parseFloat(document.getElementById("acceleration").value);

        fetchCalculation("http://localhost:8090/OntologyViewer/physics-tutor", `action=calculateForce&mass=${mass}&acceleration=${acceleration}`)
            .then((data) => showResults(`Calculated Force: ${data.force.toFixed(2)} N`, data.relatedConcepts))
            .catch((error) => showError(error.message));
    });

    // Kinetic Energy Calculation
    document.getElementById("calculateKineticEnergy").addEventListener("click", () => {
        const mass = parseFloat(document.getElementById("energyMass").value);
        const velocity = parseFloat(document.getElementById("velocity").value);

        fetchCalculation("http://localhost:8090/OntologyViewer/energy-calculator", `action=calculateKineticEnergy&mass=${mass}&velocity=${velocity}`)
            .then((data) => showResults(`Calculated Kinetic Energy: ${data.energyValue.toFixed(2)} J`, data.relatedConcepts))
            .catch((error) => showError(error.message));
    });

    // Potential Energy Calculation
    document.getElementById("calculatePotentialEnergy").addEventListener("click", () => {
        const mass = parseFloat(document.getElementById("energyMass").value);
        const height = parseFloat(document.getElementById("height").value);

        fetchCalculation("http://localhost:8090/OntologyViewer/energy-calculator", `action=calculatePotentialEnergy&mass=${mass}&height=${height}`)
            .then((data) => showResults(`Calculated Potential Energy: ${data.energyValue.toFixed(2)} J`, data.relatedConcepts))
            .catch((error) => showError(error.message));
    });

    // Work Calculation
    document.getElementById("calculateWork").addEventListener("click", () => {
        const force = parseFloat(document.getElementById("workForce").value);
        const displacement = parseFloat(document.getElementById("displacement").value);
        const angle = parseFloat(document.getElementById("angle").value);

        fetchCalculation("http://localhost:8090/OntologyViewer/work-power-calculator", `action=calculateWork&force=${force}&displacement=${displacement}&angle=${angle}`)
            .then((data) => showResults(`Calculated Work: ${data.resultValue.toFixed(2)} J`, data.relatedConcepts))
            .catch((error) => showError(error.message));
    });

    // Power Calculation
    document.getElementById("calculatePower").addEventListener("click", () => {
        const work = parseFloat(document.getElementById("displacement").value); // Assume Work as input
        const time = parseFloat(document.getElementById("time").value);

        fetchCalculation("http://localhost:8090/OntologyViewer/work-power-calculator", `action=calculatePower&work=${work}&time=${time}`)
            .then((data) => showResults(`Calculated Power: ${data.resultValue.toFixed(2)} W`, data.relatedConcepts))
            .catch((error) => showError(error.message));
    });
});
