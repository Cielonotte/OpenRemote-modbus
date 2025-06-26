# Handleiding DataDisplay-DataLogger

## Inleiding
De DataDisplay-DataLogger is een applicatie ontwikkeld voor het monitoren en loggen van Modbus apparaten. Deze handleiding is bedoeld voor IT-professionals die de applicatie willen implementeren en gebruiken. De applicatie combineert een Spring Boot backend met een web-based frontend voor een efficiënte manier van device management.

## 1. Systeemvereisten

### 1.1 Software Vereisten
- Java 8 of hoger
- Python 3.x (voor Modbus simulator)
- Maven
- Moderne web browser (Chrome, Firefox, Edge)

### 1.2 Netwerk Vereisten
- Toegang tot Modbus apparaten op het netwerk
- Poort 502 moet beschikbaar zijn voor Modbus TCP
- Firewall configuratie voor Modbus communicatie

## 2. Installatie

### 2.1 Backend Installatie
1. Vraag de broncode aan bij het development team
2. Plaats de ontvangen bestanden in een werkmap
3. Open een terminal in de werkmap

4. Build de applicatie:
   ```bash
   mvn clean install
   ```

5. Start de applicatie:
   ```bash
   java -jar target/datadisplay-datalogger.jar
   ```

### 2.2 Modbus Simulator (Optioneel)
Voor testdoeleinden:
1. Start de Python simulator:
   ```bash
   python mock_modbus_server.py
   ```

## 3. Gebruik

### 3.1 Apparaten Scannen
1. Open de webinterface (standaard: http://localhost:8080)
2. Voer het IP-bereik in voor de scan
3. Klik op "Start Scan"
4. Wacht op de scan resultaten
5. Selecteer de gewenste apparaten voor monitoring

### 3.2 Device Monitoring
1. Selecteer apparaten via de checkboxes
2. Configureer de refresh rates per device
3. Bekijk real-time data in de interface
4. Exporteer data indien nodig

### 3.3 Register Configuratie
1. Selecteer een device
2. Voeg registers toe met de juiste parameters:
   - Type (COIL, Register, Holder)
   - Register nummer
   - Refresh rate

## 4. Probleemoplossing

### 4.1 Veel Voorkomende Problemen
1. **Geen verbinding met Modbus apparaat**
   - Controleer IP-adres en poort
   - Verifieer netwerkverbinding
   - Controleer firewall instellingen

2. **Geen data updates**
   - Controleer refresh rates
   - Verifieer register configuratie
   - Controleer device verbinding

3. **Scan vindt geen apparaten**
   - Controleer IP-bereik
   - Verifieer netwerk toegang
   - Controleer Modbus poort (502)

### 4.2 Logging
- Logs zijn beschikbaar in de console output
- Gebruik log level INFO voor normale operatie
- DEBUG level voor gedetailleerde informatie

## 5. Best Practices

### 5.1 Netwerk Configuratie
- Gebruik vaste IP-adressen voor Modbus apparaten
- Configureer firewall regels voor Modbus TCP
- Implementeer netwerk segmentatie indien nodig

### 5.2 Performance
- Pas refresh rates aan op basis van behoefte
- Monitor systeem resources
- Gebruik efficiënte IP-bereiken voor scans

### 5.3 Veiligheid
- Beperk netwerk toegang tot Modbus apparaten
- Gebruik netwerk segmentatie
- Implementeer logging voor security events

## Conclusie
De DataDisplay-DataLogger biedt een efficiënte oplossing voor het monitoren en loggen van Modbus apparaten. Met de juiste configuratie en onderhoud kan de applicatie een waardevol hulpmiddel zijn voor het beheren van industriële netwerken. Voor verdere ondersteuning of vragen, raadpleeg de documentatie of neem contact op met het development team.

## Appendix

### A. Modbus Protocol Overzicht
- Modbus TCP gebruikt poort 502
- Ondersteunde functiecodes: 1-4, 5-6, 15-16
- Standaard unit ID: 1

### B. API Endpoints
- GET /api/scan-modbus - Start Modbus scan
- GET /api/registers/{deviceId} - Haal registers op
- POST /api/registers - Voeg register toe

### C. Configuratie Bestanden
- application.properties - Spring Boot configuratie
- device.json - Device en register configuratie 