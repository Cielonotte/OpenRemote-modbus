document.addEventListener('DOMContentLoaded', function() {
    fetch('/api/data')
        .then(response => response.json())
        .then(data => {
            const tableBody = document.querySelector('#deviceTable tbody');
            data.forEach(device => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${device.hostname}</td>
                    <td>${device.ipAddress}</td>
                    <td>${device.macAddress}</td>
                    <td>${device.registrationDate}</td>
                `;
                tableBody.appendChild(row);
            });
        });
});