document.addEventListener('DOMContentLoaded', function() {
    const startTestButton = document.getElementById('startTest');
    const feedbackDiv = document.getElementById('feedback');

    startTestButton.addEventListener('click', function() {
        feedbackDiv.style.display = 'none'; // Verberg vorig bericht

        fetch('instellingen.json') // Nieuwe naam voor het JSON-bestand
            .then(response => response.json())
            .then(instellingen => {
                if (instellingen.activeerTest) { // Nieuwe naam voor de boolean
                    fetch('/api/trigger-actie') // Nieuwe naam voor de API-endpoint
                        .then(response => response.text())
                        .then(bericht => {
                            feedbackDiv.textContent = bericht;
                            feedbackDiv.className = 'alert-box success';
                            feedbackDiv.style.display = 'block';
                        })
                        .catch(fout => {
                            console.error('Fout bij aanroepen API:', fout);
                            feedbackDiv.textContent = 'Er is een fout opgetreden bij de API.';
                            feedbackDiv.className = 'alert-box error';
                            feedbackDiv.style.display = 'block';
                        });
                } else {
                    feedbackDiv.textContent = 'De test is niet geactiveerd in het configuratiebestand.';
                    feedbackDiv.className = 'alert-box';
                    feedbackDiv.style.display = 'block';
                }
            })
            .catch(fout => {
                console.error('Fout bij het lezen van instellingen.json:', fout);
                feedbackDiv.textContent = 'Kon het instellingenbestand niet lezen.';
                feedbackDiv.className = 'alert-box error';
                feedbackDiv.style.display = 'block';
            });
    });
});