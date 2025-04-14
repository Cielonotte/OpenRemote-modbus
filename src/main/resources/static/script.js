document.addEventListener('DOMContentLoaded', function () {
    loadDevices();
    setupEventListeners();
});

let currentDeviceData = {
    registers: []
};

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
        resetForm();
        currentDeviceData = { registers: [] };
    });

    nextButton.addEventListener('click', () => {
        const deviceName = document.getElementById('deviceName').value;
        const deviceAddress = document.getElementById('deviceAddress').value;
        const connectionLink = document.getElementById('connectionLink').value;

        if (deviceName && deviceAddress && connectionLink) {
            currentDeviceData.name = deviceName;
            currentDeviceData.address = deviceAddress;
            currentDeviceData.connectionLink = connectionLink;

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
        submitDeviceButton.disabled = true;

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
                currentDeviceData.id = data.id;

                if (!currentDeviceData.id) {
                    throw new Error('Geen ID ontvangen van de server');
                }

                // Direct alle registers toevoegen
                const registerPromises = currentDeviceData.registers.map(register =>
                    fetch('/api/registers', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ ...register, deviceId: data.id })
                    })
                );

                return Promise.all(registerPromises);
            })
            .then(() => {
                console.log('Apparaat & registers succesvol opgeslagen');
                addDeviceForm.style.display = 'none';
                deviceFormStep2.style.display = 'none';
                resetForm();
                loadDevices();
                currentDeviceData = { registers: [] };
            })
            .catch(error => {
                alert('Er ging iets mis bij het opslaan: ' + error.message);
                console.error(error);
            })
            .finally(() => {
                submitDeviceButton.disabled = false;
            });
    });

    addRegisterButton.addEventListener('click', () => {
        const registerType = document.getElementById('registerType').value;
        const registerNumber = parseInt(document.getElementById('registerNumber').value);
        const refreshRate = parseInt(document.getElementById('refreshRate').value);

        if (!isNaN(registerNumber) && !isNaN(refreshRate)) {
            const newRegister = {
                type: registerType,
                number: registerNumber,
                refreshRate: refreshRate
            };

            currentDeviceData.registers.push(newRegister);

            // Voeg visueel toe aan overzicht
            const preview = document.createElement('div');
            preview.textContent = `Type: ${registerType}, Nummer: ${registerNumber}, Verversing: ${refreshRate}s`;
            document.getElementById('device-info-registers').appendChild(preview);

            // Reset inputvelden
            document.getElementById('registerNumber').value = '';
            document.getElementById('refreshRate').value = '';
        } else {
            alert('Ongeldige input voor register.');
        }
    });

    document.querySelectorAll('.cancel-button').forEach(button => {
        button.addEventListener('click', () => {
            addDeviceForm.style.display = 'none';
            deviceFormStep1.style.display = 'block';
            deviceFormStep2.style.display = 'none';
            resetForm();
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

function resetForm() {
    // Reset alle inputvelden
    document.getElementById('deviceName').value = '';
    document.getElementById('deviceAddress').value = '';
    document.getElementById('connectionLink').value = '';
    document.getElementById('registerNumber').value = '';
    document.getElementById('refreshRate').value = '';
    document.getElementById('device-info-registers').innerHTML = '';
}
