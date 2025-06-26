from pymodbus.server import StartTcpServer
from pymodbus.datastore import ModbusSequentialDataBlock
from pymodbus.datastore import ModbusSlaveContext, ModbusServerContext
import random

def create_random_mock_data_store_with_info():
    slaves = {}
    num_devices = random.randint(3, 5)  # Willekeurig aantal apparaten (kleiner voor overzicht)

    print("\n--- Gesimuleerde Modbus TCP Apparaten ---")

    for i in range(1, num_devices + 1):
        slave_id = i
        virtual_ip = f"127.0.0.{random.randint(1, 253)}"
        device_name = f"Apparaat {i} (TCP)"
        connection_link = "TCP"

        print(f"\nApparaat ID: {slave_id}")
        print(f"  Naam: {device_name}")
        print(f"  Link: {connection_link}")
        print(f"  Adres: {virtual_ip}")

        num_holding_registers = random.randint(5, 10)  # Kleinere aantallen voor overzicht
        holding_registers = [random.randint(0, 100) for _ in range(num_holding_registers)]
        print(f"  Holding Registers (0-{num_holding_registers-1}): {holding_registers}")

        num_coils = random.randint(5, 10)
        coils = [random.choice([True, False]) for _ in range(num_coils)]
        print(f"  Coils (0-{num_coils-1}): {coils}")

        slave_context = ModbusSlaveContext(
            hr=ModbusSequentialDataBlock(0, holding_registers),
            co=ModbusSequentialDataBlock(0, coils)
        )
        slaves[slave_id] = slave_context

    print("\n--- Modbus TCP Server Start ---")
    return ModbusServerContext(slaves=slaves, single=False)

def run_random_mock_server_with_info():
    store = create_random_mock_data_store_with_info()
    try:
        StartTcpServer(store, address=("localhost", 502))
    except Exception as e:
        print(f"Error starting the server: {e}")

if __name__ == "__main__":
    run_random_mock_server_with_info()