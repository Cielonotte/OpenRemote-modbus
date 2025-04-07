document.addEventListener('DOMContentLoaded', function () {
    loadDevices();
    setupEventListeners();
});

let currentDeviceData = {};

function loadDevices() {
    fetch('/api/devices')
        .then(response => response.json())
        .then(data => {
            const deviceTableBody = document.querySelector('#device-table tbody');
            deviceTableBody.innerHTML = '';
            data.forEach(device => {
                const row = deviceTableBody.insertRow();
                row.insertCell().textContent = device.id;
                row.insertCell().textContent = device.name;
                row.insertCell().textContent = device.address;
                row.insertCell().textContent = device.connectionLink;

                const detailsCell = row.insertCell();
                const detailsButton = document.createElement('button');
                detailsButton.textContent = 'Bekijk Registers';
                detailsButton.classList.add('view-registers-btn');
                detailsButton.dataset.deviceId = device.id;
                detailsCell.appendChild(detailsButton);

                const registersRow = deviceTableBody.insertRow();
                registersRow.classList.add('registers-row');
                registersRow.dataset.deviceId = device.id;
                registersRow.style.display = 'none';
                const registersCell = registersRow.insertCell();
                registersCell.colSpan = 5;
                registersCell.innerHTML = `<div id="registers-container-${device.id}"></div>`;
            });

            addRegisterEventListeners();
        })
        .catch(error => console.error('Fout bij het laden van apparaten:', error));
}

function setupEventListeners() {
    const addDeviceButton = document.getElementById("add-device-btn");
    const addDeviceForm = document.getElementById('add-device-form');
    const deviceFormStep1 = document.getElementById('device-form-step-1');
    const deviceFormStep2 = document.getElementById('device-form-step-2');
    const nextButton = document.getElementById('next-btn');
    const prevButton = document.getElementById('prev-btn');
    const submitDeviceButton = document.getElementById('submit-device-btn');
    const addRegisterButton = document.getElementById('add-register-btn');
    const deviceInfoRegisters = document.getElementById('device-info-registers');

    addDeviceButton.addEventListener('click', () => {
        addDeviceForm.style.display = 'flex';
        deviceFormStep1.style.display = 'block';
        deviceFormStep2.style.display = 'none';
        document.getElementById('device-form-step-1').reset();
        currentDeviceData = {};
    });

    nextButton.addEventListener('click', () => {
        const deviceName = document.getElementById('deviceName').value;
        const deviceAddress = document.getElementById('deviceAddress').value;
        const connectionLink = document.getElementById('connectionLink').value;

        if (deviceName && deviceAddress && connectionLink) {
            currentDeviceData = {
                name: deviceName,
                address: deviceAddress,
                connectionLink: connectionLink
            };
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

    submitDeviceButton.addEventListener('click', () => {
        fetch('/api/devices', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(currentDeviceData)
        })
        .then(response => {
            if (!response.ok) throw new Error('Fout bij toevoegen apparaat');
            return response.json();
        })
        .then(data => {
            console.log('Apparaat succesvol toegevoegd:', data);
            currentDeviceData.id = data.id;

            if (currentDeviceData.id) {
                deviceInfoRegisters.textContent = `Voeg registers toe voor: ${data.name}`;
                deviceFormStep1.style.display = 'none';
                deviceFormStep2.style.display = 'block';
            } else {
                alert('Geen apparaat-ID ontvangen van de server.');
            }
        })
        .catch(error => console.error('Fout bij het toevoegen van apparaat:', error));
    });

    addRegisterButton.addEventListener('click', () => {
        const registerType = document.getElementById('registerType').value;
        const registerNumber = parseInt(document.getElementById('registerNumber').value);
        const refreshRate = parseInt(document.getElementById('refreshRate').value);

        if (currentDeviceData.id && registerType && !isNaN(registerNumber) && !isNaN(refreshRate)) {
            const newRegister = {
                deviceId: currentDeviceData.id,
                type: registerType,
                number: registerNumber,
                refreshRate: refreshRate
            };

            fetch('/api/registers', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(newRegister)
            })
            .then(response => {
                if (!response.ok) throw new Error('Fout bij toevoegen register');
                return response.json();
            })
            .then(data => {
                console.log('Register succesvol toegevoegd:', data);
                addDeviceForm.style.display = 'none';
                deviceFormStep2.style.display = 'none';
                document.getElementById('device-form-step-1').reset();
                loadDevices();
                currentDeviceData = {};
            })
            .catch(error => console.error('Fout bij het toevoegen van register:', error));
        } else {
            alert('Zorg ervoor dat je eerst een apparaat hebt toegevoegd en alle registervelden hebt ingevuld.');
        }
    });

    document.querySelectorAll('.cancel-button').forEach(button => {
        button.addEventListener('click', () => {
            addDeviceForm.style.display = 'none';
            deviceFormStep1.style.display = 'block';
            deviceFormStep2.style.display = 'none';
        });
    });
}

function loadRegisters(deviceId, container) {
    fetch(`/api/registers/${deviceId}`)
        .then(response => response.json())
        .then(registers => {
            let html = '<h4>Registers:</h4>';
            if (registers.length > 0) {
                html += '<table><thead><tr><th>ID</th><th>Type</th><th>Nummer</th><th>Verversingssnelheid</th></tr></thead><tbody>';
                registers.forEach(r => {
                    html += `<tr><td>${r.id}</td><td>${r.type}</td><td>${r.number}</td><td>${r.refreshRate}</td></tr>`;
                });
                html += '</tbody></table>';
            } else {
                html += '<p>Geen registers gevonden voor dit apparaat.</p>';
            }
            container.innerHTML = html;
        })
        .catch(error => console.error(`Fout bij laden van registers voor apparaat ${deviceId}:`, error));
}

function addRegisterEventListeners() {
    document.querySelectorAll('.view-registers-btn').forEach(button => {
        button.addEventListener('click', function () {
            const deviceId = this.dataset.deviceId;
            const registersRow = this.parentNode.parentNode.nextElementSibling;
            const container = registersRow.querySelector(`#registers-container-${deviceId}`);

            if (registersRow.style.display === 'none') {
                registersRow.style.display = 'table-row';
                loadRegisters(deviceId, container);
                this.textContent = 'Verberg Registers';
            } else {
                registersRow.style.display = 'none';
                this.textContent = 'Bekijk Registers';
            }
        });
    });
}
