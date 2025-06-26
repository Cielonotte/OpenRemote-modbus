document.addEventListener('DOMContentLoaded', function () {
    loadDevices();
    setupEventListeners(); // Deze wordt nu correct gevonden
});

let scannedDevices = [];
let currentSelectedDevices = [];

function loadDevices() {
    fetch('/api/devices')
        .then(response => response.json())
        .then(data => {
            const deviceTableBody = document.querySelector('#device-table tbody');
            deviceTableBody.innerHTML = '';
            if (data.length === 0) {
                const row = deviceTableBody.insertRow();
                const cell = row.insertCell();
                cell.colSpan = 6;
                cell.textContent = "Nog geen apparaten geconfigureerd. Voer een netwerkscan uit!";
                cell.style.textAlign = 'center';
                cell.style.fontStyle = 'italic';
            } else {
                data.forEach(device => {
                    const row = deviceTableBody.insertRow();
                    row.insertCell().textContent = device.id;
                    row.insertCell().textContent = device.name;
                    row.insertCell().textContent = device.address;
                    row.insertCell().textContent = device.connectionLink;
                    row.insertCell().textContent = device.refreshRate;

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
                    registersCell.colSpan = 6;
                    registersCell.innerHTML = `<div id="registers-container-${device.id}"></div>`;
                });
            }
            addRegisterEventListeners();
        })
        .catch(error => console.error('Fout bij het laden van apparaten:', error));
}

// --------------------------------------------------------------------------------------------------
// OPLOSSING: De functie setupEventListeners is hierheen verplaatst, BUITEN de DOMContentLoaded listener
// --------------------------------------------------------------------------------------------------
function setupEventListeners() {
    const scanNetworkBtn = document.getElementById("scan-network-btn");
    const networkScanForm = document.getElementById('network-scan-form');
    const scanFormStep1 = document.getElementById('scan-form-step-1');
    const scanFormStep2 = document.getElementById('scan-form-step-2');
    const startScanBtn = document.getElementById('start-scan-btn');
    const saveSelectedDevicesBtn = document.getElementById('save-selected-devices-btn');
    const backToScanBtn = document.getElementById('back-to-scan-btn');
    const foundDevicesList = document.getElementById('found-devices-list');

    // Controleren of elementen bestaan voordat listeners worden toegevoegd
    if (scanNetworkBtn) {
        scanNetworkBtn.addEventListener('click', () => {
            networkScanForm.style.display = 'flex';
            scanFormStep1.style.display = 'block';
            scanFormStep2.style.display = 'none';
            resetScanForm();
            foundDevicesList.innerHTML = '<p>Voer een IP-bereik in en klik op "Start Scan".</p>';
        });
    } else {
        console.error("Element met ID 'scan-network-btn' niet gevonden.");
    }

    if (startScanBtn) {
        startScanBtn.addEventListener('click', () => {
            const startIp = document.getElementById('startIp').value;
            const endIp = document.getElementById('endIp').value;

            if (!startIp || !endIp) {
                alert('Vul zowel het start- als het eind IP-adres in.');
                return;
            }

            startScanBtn.disabled = true;
            startScanBtn.textContent = 'Scannen...';
            foundDevicesList.innerHTML = '<p>Bezig met scannen, een moment geduld...</p><div class="loader"></div>';

            fetch(`/api/scan-modbus?startIp=${startIp}&endIp=${endIp}`)
                .then(response => {
                    if (!response.ok) {
                        throw new Error(`HTTP error! status: ${response.status}`);
                    }
                    return response.json();
                })
                .then(data => {
                    scannedDevices = data;
                    displayScannedDevices(scannedDevices);
                    scanFormStep1.style.display = 'none';
                    scanFormStep2.style.display = 'block';
                })
                .catch(error => {
                    console.error('Fout bij het scannen van Modbus apparaten:', error);
                    foundDevicesList.innerHTML = `<p style="color: red;">Fout bij scan: ${error.message}</p>`;
                    alert('Er ging iets mis tijdens de scan. Controleer de console voor details.');
                })
                .finally(() => {
                    startScanBtn.disabled = false;
                    startScanBtn.textContent = 'Start Scan';
                });
        });
    } else {
        console.error("Element met ID 'start-scan-btn' niet gevonden.");
    }

    if (saveSelectedDevicesBtn) {
        saveSelectedDevicesBtn.addEventListener('click', () => {
            currentSelectedDevices = [];
            const checkboxes = document.querySelectorAll('#found-devices-list input[type="checkbox"]:checked');

            if (checkboxes.length === 0) {
                alert('Selecteer ten minste één apparaat om op te slaan.');
                return;
            }

            let hasInvalidInput = false;

            checkboxes.forEach(checkbox => {
                const ipAddress = checkbox.value;
                const refreshRateInput = document.getElementById(`refresh-rate-${ipAddress.replace(/\./g, '-')}`);
                const refreshRate = parseInt(refreshRateInput.value);

                if (isNaN(refreshRate) || refreshRate <= 0) {
                    alert(`Ongeldige verversingssnelheid voor ${ipAddress}. Voer een positief getal in.`);
                    hasInvalidInput = true;
                    return;
                }

                currentSelectedDevices.push({
                    name: `Modbus Apparaat op ${ipAddress}`,
                    address: ipAddress,
                    connectionLink: "TCP",
                    refreshRate: refreshRate
                });
            });

            if (hasInvalidInput) {
                return;
            }

            if (currentSelectedDevices.length === 0) {
                alert('Geen geldige apparaten geselecteerd om op te slaan.');
                return;
            }

            saveSelectedDevicesBtn.disabled = true;
            saveSelectedDevicesBtn.textContent = 'Opslaan...';

            const savePromises = currentSelectedDevices.map(device =>
                fetch('/api/devices', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(device)
                })
                .then(response => {
                    if (!response.ok) {
                        if (response.status === 409) {
                            console.warn(`Apparaat met adres ${device.address} bestaat al, overgeslagen.`);
                            return null;
                        }
                        throw new Error(`Fout bij het opslaan van apparaat ${device.address}: ${response.statusText}`);
                    }
                    return response.json();
                })
            );

            Promise.all(savePromises)
                .then(results => {
                    const successfullySavedCount = results.filter(r => r !== null).length;
                    if (successfullySavedCount > 0) {
                        alert(`${successfullySavedCount} geselecteerde apparaten succesvol opgeslagen!`);
                    } else {
                        alert('Alle geselecteerde apparaten bestonden al of konden niet worden opgeslagen.');
                    }
                    networkScanForm.style.display = 'none';
                    loadDevices();
                })
                .catch(error => {
                    console.error('Fout bij het opslaan van apparaten:', error);
                    alert('Er ging iets mis bij het opslaan van de geselecteerde apparaten: ' + error.message);
                })
                .finally(() => {
                    saveSelectedDevicesBtn.disabled = false;
                    saveSelectedDevicesBtn.textContent = 'Geselecteerde Apparaten Opslaan';
                    resetScanForm();
                });
        });
    } else {
        console.error("Element met ID 'save-selected-devices-btn' niet gevonden.");
    }

    if (backToScanBtn) {
        backToScanBtn.addEventListener('click', () => {
            scanFormStep1.style.display = 'block';
            scanFormStep2.style.display = 'none';
            foundDevicesList.innerHTML = '<p>Voer een IP-bereik in en klik op "Start Scan".</p>';
        });
    } else {
        console.error("Element met ID 'back-to-scan-btn' niet gevonden.");
    }

    // Aangenomen dat .close-button een ID heeft, of dat dit een algemene klasse is
    document.querySelectorAll('.close-button').forEach(button => {
        button.addEventListener('click', () => {
            networkScanForm.style.display = 'none';
            resetScanForm();
        });
    });
}
// --------------------------------------------------------------------------------------------------

// Functie om gescande apparaten weer te geven in de UI
function displayScannedDevices(devices) {
    const foundDevicesList = document.getElementById('found-devices-list');
    foundDevicesList.innerHTML = '';

    if (devices.length === 0) {
        foundDevicesList.innerHTML = '<p>Geen Modbus apparaten gevonden in het opgegeven bereik.</p>';
        return;
    }

    devices.forEach(device => {
        const deviceDiv = document.createElement('div');
        deviceDiv.classList.add('scanned-device-item');

        const uniqueId = `refresh-rate-${device.address.replace(/\./g, '-')}`;

        deviceDiv.innerHTML = `
            <input type="checkbox" id="${device.address}" value="${device.address}" checked>
            <label for="${device.address}"><strong>${device.address}</strong> (Nieuw apparaat)</label>
            <label for="${uniqueId}">Verversingssnelheid (s):</label>
            <input type="number" id="${uniqueId}" value="5" min="1">
        `;
        foundDevicesList.appendChild(deviceDiv);
    });
}

// Bestaande functies voor registers (blijven grotendeels hetzelfde)
function loadRegisters(deviceId, container) {
    fetch(`/api/registers/${deviceId}`)
        .then(response => response.json())
        .then(registers => {
            let html = '<h4>Registers:</h4>';
            if (registers.length > 0) {
                html += '<table><thead><tr><th>ID</th><th>Type</th><th>Nummer</th><th>Verversingssnelheid</th><th>Acties</th></tr></thead><tbody>';
                registers.forEach(r => {
                    html += `
                        <tr>
                            <td>${r.id}</td>
                            <td><span class="display-mode">${r.type}</span><input type="text" class="edit-mode" style="display:none;" value="${r.type}"></td>
                            <td><span class="display-mode">${r.number}</span><input type="number" class="edit-mode" style="display:none;" value="${r.number}"></td>
                            <td><span class="display-mode">${r.refreshRate}</span><input type="number" class="edit-mode" style="display:none;" value="${r.refreshRate}"></td>
                            <td>
                                <button class="edit-register-btn" data-register-id="${r.id}">Bewerk</button>
                                <button class="save-register-btn" data-register-id="${r.id}" style="display:none;">Opslaan</button>
                                <button class="delete-register-btn" data-register-id="${r.id}">Verwijder</button>
                            </td>
                        </tr>
                    `;
                });
                html += '</tbody></table>';
            } else {
                html += '<p>Geen registers gevonden voor dit apparaat. Je kunt ze handmatig toevoegen via de API indien nodig.</p>';
            }
            container.innerHTML = html;
            setupRegisterActionListeners(container);
        })
        .catch(error => console.error(`Fout bij laden van registers voor apparaat ${deviceId}:`, error));
}


function setupRegisterActionListeners(container) {
    container.querySelectorAll('.edit-register-btn').forEach(button => {
        button.addEventListener('click', function() {
            const row = this.closest('tr');
            row.querySelectorAll('.display-mode').forEach(span => span.style.display = 'none');
            row.querySelectorAll('.edit-mode').forEach(input => input.style.display = 'inline-block');
            this.style.display = 'none';
            row.querySelector('.save-register-btn').style.display = 'inline-block';
        });
    });

    container.querySelectorAll('.save-register-btn').forEach(button => {
        button.addEventListener('click', function() {
            const registerId = this.dataset.registerId;
            const row = this.closest('tr');
            const type = row.querySelector('input[type="text"]').value;
            const number = parseInt(row.querySelector('input[type="number"]').value);
            const refreshRate = parseInt(row.querySelectorAll('input[type="number"]')[1].value);

            if (isNaN(number) || isNaN(refreshRate) || refreshRate <= 0) {
                alert('Ongeldige invoer voor register. Nummer en Verversingssnelheid moeten positieve getallen zijn.');
                return;
            }

            const updatedRegister = { id: parseInt(registerId), type, number, refreshRate, deviceId: parseInt(row.closest('.registers-row').dataset.deviceId) };

            fetch(`/api/registers/${registerId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(updatedRegister)
            })
            .then(response => {
                if (!response.ok) throw new Error('Fout bij bijwerken register');
                return response.json();
            })
            .then(() => {
                alert('Register succesvol bijgewerkt!');
                row.querySelectorAll('.display-mode').forEach(span => span.style.display = 'inline-block');
                row.querySelectorAll('.edit-mode').forEach(input => input.style.display = 'none');
                row.querySelector('.edit-register-btn').style.display = 'inline-block';
                this.style.display = 'none';
                loadRegisters(updatedRegister.deviceId, row.closest('.registers-row').querySelector(`#registers-container-${updatedRegister.deviceId}`));
            })
            .catch(error => {
                console.error('Fout bij bijwerken register:', error);
                alert('Fout bij bijwerken register: ' + error.message);
            });
        });
    });

    container.querySelectorAll('.delete-register-btn').forEach(button => {
        button.addEventListener('click', function() {
            if (!confirm('Weet u zeker dat u dit register wilt verwijderen?')) {
                return;
            }
            const registerId = this.dataset.registerId;
            const deviceId = this.closest('.registers-row').dataset.deviceId;

            fetch(`/api/registers/${registerId}`, {
                method: 'DELETE'
            })
            .then(response => {
                if (!response.ok) throw new Error('Fout bij verwijderen register');
                alert('Register succesvol verwijderd!');
                loadRegisters(deviceId, this.closest('.registers-row').querySelector(`#registers-container-${deviceId}`));
            })
            .catch(error => {
                console.error('Fout bij verwijderen register:', error);
                alert('Fout bij verwijderen register: ' + error.message);
            });
        });
    });
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

function resetScanForm() {
    document.getElementById('startIp').value = '127.0.0.2';
    document.getElementById('endIp').value = '127.0.0.253';
    document.getElementById('found-devices-list').innerHTML = '<p>Voer een IP-bereik in en klik op "Start Scan".</p>';
}