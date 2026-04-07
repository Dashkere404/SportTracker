package com.example.mysporttracker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExerciseDialog (
    onDismiss: () -> Unit,
    onConfirm: (Exercise) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("")}
    var weight by remember { mutableStateOf("")}
    var number by remember { mutableStateOf("")}
    var repeatNumber by remember { mutableStateOf("")}

    var nameError by remember { mutableStateOf<String?>(null) }
    var weightError by remember { mutableStateOf<String?>(null)}
    var numberError by remember { mutableStateOf<String?>(null)}
    var repeatNumberError by remember {mutableStateOf<String?>(null)}
    val muscleGroups = listOf(
        "Грудь",
        "Спина",
        "Ноги",
        "Плечи",
        "Бицепсы",
        "Трицепсы",
        "Пресс",
        "Предплечья",
        "Икры",
        "Ягодицы",
        "Кардио",
        "Другое"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Новое упражнение") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = if (it.isNotBlank()) null else "Название - обязательное поле"
                    },
                    label = {Text("Название *")},
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    singleLine = true,
                    isError = nameError != null,
                    supportingText = nameError?.let {{Text(it)}}

                )
                Spacer (modifier = Modifier.padding(4.dp))
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Группа мышц") },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        muscleGroups.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group) },
                                onClick = {
                                    category = group
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer (modifier = Modifier.padding(4.dp))

                OutlinedTextField(
                    value = weight,
                    onValueChange = { newValue ->
                        weight = newValue
                        weightError = if (newValue.isEmpty() || newValue.toDoubleOrNull() != null) {
                            null
                        } else {
                            "Введите число"
                        }
                    },
                    label = {Text("Вес")},
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    singleLine = true,
                    isError = weightError != null,
                    supportingText = weightError?.let { { Text(it) } }
                )
                Spacer (modifier = Modifier.padding(4.dp))

                OutlinedTextField(
                    value = number,
                    onValueChange = { newValue ->
                        number = newValue
                        numberError = if (newValue.isEmpty() || newValue.toIntOrNull() != null) {
                            null
                        } else {
                            "Введите целое число"
                        }
                    },
                    label = {Text("Число повторений в одном подходе")},
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    singleLine = true,
                    isError = numberError != null,
                    supportingText = numberError?.let { { Text(it) } }
                )
                Spacer (modifier = Modifier.padding(4.dp))

                OutlinedTextField(
                    value = repeatNumber,
                    onValueChange = { newValue ->
                        repeatNumber = newValue
                        repeatNumberError = if (newValue.isEmpty() || newValue.toIntOrNull() != null) {
                            null
                        } else {
                            "Введите целое число"
                        }
                    },
                    label = {Text("Количество подходов")},
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    singleLine = true,
                    isError = repeatNumberError != null,
                    supportingText = repeatNumberError?.let { { Text(it) } }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank()) {
                        nameError = "Название - обязательное поле"
                        return@TextButton
                    }
                    if (nameError != null || weightError!=null || numberError != null ||repeatNumberError!=null) {
                        return@TextButton
                    }
                    val exercise = Exercise(
                        name = name.trim(),
                        category = category.trim(),
                        weight = weight.toDoubleOrNull() ?: 0.0,
                        number = number.toIntOrNull() ?: 0,
                        repeatNumber = repeatNumber.toIntOrNull() ?: 0
                    )
                    onConfirm(exercise)
                    onDismiss()
                }
            ) {
                Text("Добавить упражнение")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

