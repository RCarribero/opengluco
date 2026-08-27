package com.example.opengluco.auto.screen

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import com.example.opengluco.core.model.ConnectionItem

class PatientListCarScreen(
    carContext: CarContext,
    private val patients: List<ConnectionItem>,
    private val selectedPatientId: String?,
    private val onPatientSelected: (ConnectionItem) -> Unit
) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        val listBuilder = ItemList.Builder()

        if (patients.isEmpty()) {
            listBuilder.setNoItemsMessage("No hay pacientes disponibles")
        } else {
            for (patient in patients) {
                val isSelected = patient.patientId == selectedPatientId
                val measurement = patient.effectiveMeasurement
                val valStr = measurement?.getFormattedValue() ?: "--"
                val trend = measurement?.trendSymbol ?: ""
                val statusTag = if (isSelected) " [Activo]" else ""

                listBuilder.addItem(
                    Row.Builder()
                        .setTitle("${patient.fullName}$statusTag")
                        .addText("Glucosa: $valStr mg/dL $trend")
                        .setOnClickListener {
                            onPatientSelected(patient)
                            screenManager.pop()
                        }
                        .build()
                )
            }
        }

        return ListTemplate.Builder()
            .setTitle("Seleccionar Paciente")
            .setHeaderAction(Action.BACK)
            .setSingleList(listBuilder.build())
            .build()
    }
}
