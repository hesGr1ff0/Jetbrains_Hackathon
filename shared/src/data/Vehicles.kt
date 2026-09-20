package wastetrack.data

import wastetrack.engine.Vehicle
import wastetrack.engine.VehicleType

private const val TRICYCLE_TARE_KG = 250.0
private const val TRUCK_TARE_KG = 2500.0

val FLEET_VEHICLES = listOf(
    Vehicle(id = "M-24-GT-1842", driverName = "Kwame Mensah", type = VehicleType.TRICYCLE, tareKg = TRICYCLE_TARE_KG),
    Vehicle(id = "M-23-GT-0977", driverName = "Yaw Boateng", type = VehicleType.TRICYCLE, tareKg = TRICYCLE_TARE_KG),
    Vehicle(id = "M-24-GT-3310", driverName = "Abena Owusu", type = VehicleType.TRICYCLE, tareKg = TRICYCLE_TARE_KG),
    Vehicle(id = "M-22-GT-7165", driverName = "Kofi Asare", type = VehicleType.TRICYCLE, tareKg = TRICYCLE_TARE_KG),
    Vehicle(id = "GT-5109-23", driverName = "Samuel Tetteh", type = VehicleType.TRUCK, tareKg = TRUCK_TARE_KG),
    Vehicle(id = "GT-2740-24", driverName = "Ibrahim Alhassan", type = VehicleType.TRUCK, tareKg = TRUCK_TARE_KG),
)
