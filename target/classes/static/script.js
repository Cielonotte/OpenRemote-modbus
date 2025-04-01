document.addEventListener('DOMContentLoaded', function() {
    loadDevices();
    setupEventListeners(); // Roep een aparte functie aan om de event listeners in te stellen
});

const addDeviceButton = document.getElementById("add-device-btn");
const addDeviceForm = document.getElementById('add-device-form');
const deviceFormStep1 = document.getElementById('device-form-step-1');
const deviceFormStep2 = document.getElementById('device-form-step-2');
const nextButton = document.getElementById('next-btn');
const prevButton = document.getElementById('prev-btn');
const submitDeviceButton = document.getElementById('submit-device-btn');
const deviceInfoRegisters = document.getElementById('device-info-registers');
const deviceTableBody = document.querySelector('#device-table tbody');

let currentDeviceData = {};

function loadDevices() {
    fetch('/api/devices')
        .then(response => response.json())
        .then(data => {
            deviceTableBody.innerHTML = '';
            data.forEach(device => {
                const row = deviceTableBody.insertRow();
                row.insertCell().textContent = device.id;
                row.insertCell().textContent = device.name;
                row.insertCell().textContent = device.address;
                row.insertCell().textContent = device.connectionLink;
                row.insertCell().innerHTML = '<button class="edit-btn">Bewerken</button>';
            });
        })
        .catch(error => console.error('Fout bij het laden van apparaten:', error));
}

function setupEventListeners() {
    addDeviceButton.addEventListener('click', () => {
        addDeviceForm.style.display = 'flex';
        deviceFormStep1.style.display = 'block';
        deviceFormStep2.style.display = 'none';
        deviceFormStep1.reset();
        currentDeviceData = {};
    });

    nextButton.addEventListener('click', () => {
        const deviceName = document.getElementById('deviceName').value;
        const deviceAddress = document.getElementById('deviceAddress').value;
        const connectionLink = document.getElementById('connectionLink').value;

        if (deviceName && deviceAddress && connectionLink) {
            currentDeviceData = { name: deviceName, address: deviceAddress, connectionLink: connectionLink };
            deviceInfoRegisters.textContent = `Voeg registers toe voor: ${deviceName}`;
            deviceFormStep1.style.display = 'none';
            deviceFormStep2.style.display = 'block';
        } else {
            alert('Vul alle velden in de eerste stap in.');
        }
    });

    prevButton.addEventListener('click', () => {
        deviceFormStep1.style.display = 'block';
        deviceFormStep2.style.display = 'none';
    });

    const cancelButtonStep1 = document.getElementById('cancel-btn-step-1');
    if (cancelButtonStep1) {
        cancelButtonStep1.addEventListener('click', () => {
            addDeviceForm.style.display = 'none';
        });
    }

    const cancelButtonStep2 = document.getElementById('cancel-btn-step-2');
    if (cancelButtonStep2) {
        cancelButtonStep2.addEventListener('click', () => {
            addDeviceForm.style.display = 'none';
        });
    }

    submitDeviceButton.addEventListener('click', () => {
        fetch('/api/devices', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(currentDeviceData)
        })
        .then(response => response.json())
        .then(data => {
            console.log('Apparaat succesvol toegevoegd:', data);
            loadDevices();
            addDeviceForm.style.display = 'none';
            deviceFormStep2.style.display = 'none';
            deviceFormStep1.reset();
            currentDeviceData = {};
        })
        .catch(error => console.error('Fout bij het toevoegen van apparaat:', error));
    });

    document.querySelectorAll('.form-content .cancel-button').forEach(button => {
        button.addEventListener('click', () => {
            addDeviceForm.style.display = 'none';
        });
    });

    // Zorg ervoor dat de sluitknoppen in de header ook werken
    const closeButtonStep1 = document.getElementById('cancel-btn-step-1');
    if (closeButtonStep1) {
        closeButtonStep1.addEventListener('click', () => {
            addDeviceForm.style.display = 'none';
        });
    }

    const closeButtonStep2 = document.getElementById('cancel-btn-step-2');
    if (closeButtonStep2) {
        closeButtonStep2.addEventListener('click', () => {
            addDeviceForm.style.display = 'none';
        });
    }
}