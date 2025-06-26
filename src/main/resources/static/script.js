document.addEventListener('DOMContentLoaded', function() {
    const startTestButton = document.getElementById('startTest');
    const feedbackDiv = document.getElementById('feedback');

    startTestButton.addEventListener('click', function() {
        fetch('settings.json') // Nieuwe naam voor het JSON-bestand
            .then(response => response.json())
            .then(instellingen => {
                if (instellingen.activeerTest) { // Nieuwe naam voor de boolean
                    fetch('/api/trigger-actie') // Nieuwe naam voor de API-endpoint
                        .then(response => response.text())
                        .then(message => {
                            feedbackDiv.textContent = message;
                            feedbackDiv.className = 'alert-box success';
                            feedbackDiv.style.display = 'block';
                        })
                        .catch(error => {
                            console.error('Could not connect to the API', );
                            feedbackDiv.textContent = 'An error occured on our side, please try again.';
                            feedbackDiv.className = 'alert-box error';
                            feedbackDiv.style.display = 'block';
                        });
                } else {
                    feedbackDiv.textContent = 'This tekst was not configuerd / activated in the config file';
                    feedbackDiv.className = 'alert-box';
                    feedbackDiv.style.display = 'block';
                }
            })
            .catch(error => {
                console.error('Could not read the settings.json', error);
                feedbackDiv.textContent = 'Unable to read ';
                feedbackDiv.className = 'alert-box error';
                feedbackDiv.style.display = 'block';
            });
    });
});