package com.example.hotelapp.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hotelapp.data.Booking
import com.example.hotelapp.data.BookingDatabase
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingFormScreen(navController: NavController, bookingId: Long) {
    val context = LocalContext.current
    val dao = remember { BookingDatabase.getDatabase(context).bookingDao() }
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }

    var guestName by remember { mutableStateOf("") }
    var guestCount by remember { mutableIntStateOf(1) }
    var roomNumber by remember { mutableStateOf("") }
    var checkInDate by remember { mutableStateOf(Date()) }
    var checkOutDate by remember { mutableStateOf(Date(System.currentTimeMillis() + 86400000)) }
    var totalPrice by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Очікується") }

    LaunchedEffect(bookingId) {
        if (bookingId > 0) {
            dao.getById(bookingId)?.let { booking ->
                guestName = booking.guestName
                guestCount = booking.guestCount
                roomNumber = booking.roomNumber
                checkInDate = booking.checkInDate
                checkOutDate = booking.checkOutDate
                totalPrice = booking.totalPrice.toString()
                status = booking.status
            }
        }
    }

    fun showDatePicker(currentDate: Date, minDate: Long? = null, onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance().apply { time = currentDate }
        val dialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                onDateSelected(selectedCalendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        if (minDate != null) {
            dialog.datePicker.minDate = minDate
        }
        dialog.show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (bookingId == 0L) "Нове бронювання" else "Редагування") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = guestName,
                onValueChange = { if (it.length <= 255) guestName = it },
                label = { Text("Ім'я гостя (макс 255)") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Кількість гостей", style = MaterialTheme.typography.bodyLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedIconButton(
                    onClick = { if (guestCount > 1) guestCount-- },
                    enabled = guestCount > 1
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Зменшити")
                }
                
                Text(
                    guestCount.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                OutlinedIconButton(
                    onClick = { if (guestCount < 10) guestCount++ },
                    enabled = guestCount < 10
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Збільшити")
                }
            }

            OutlinedTextField(
                value = roomNumber,
                onValueChange = { if (it.length <= 255) roomNumber = it },
                label = { Text("Номер кімнати (макс 255)") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        showDatePicker(checkInDate, System.currentTimeMillis() - 1000) { selectedDate ->
                            checkInDate = selectedDate
                            if (checkOutDate.before(selectedDate) || checkOutDate == selectedDate) {
                                checkOutDate = Date(selectedDate.time + 86400000)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Заїзд: ${dateFormat.format(checkInDate)}")
                }
                OutlinedButton(
                    onClick = {
                        showDatePicker(checkOutDate, checkInDate.time + 86400000) { selectedDate ->
                            checkOutDate = selectedDate
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Виїзд: ${dateFormat.format(checkOutDate)}")
                }
            }

            OutlinedTextField(
                value = totalPrice,
                onValueChange = { input ->
                    // Дозволяємо цифри, крапку та кому для зручності (особливо для укр. локалі)
                    val sanitized = input.replace(',', '.')
                    if (sanitized.isEmpty() || sanitized.toDoubleOrNull() != null) {
                        totalPrice = input
                    }
                },
                label = { Text("Загальна вартість") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth()
            )

            var expanded by remember { mutableStateOf(false) }
            val statuses = listOf("Очікується", "Завершено", "Скасовано")
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                val statusColor = when (status) {
                    "Завершено" -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
                    "Скасовано" -> androidx.compose.ui.graphics.Color(0xFFF44336)
                    else -> androidx.compose.ui.graphics.Color(0xFFFF9800)
                }

                OutlinedTextField(
                    value = status,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Статус (виберіть зі списку)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    leadingIcon = {
                        Surface(
                            modifier = Modifier.size(12.dp),
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = statusColor
                        ) {}
                    },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    statuses.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                status = selectionOption
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val booking = Booking(
                        id = bookingId,
                        guestName = guestName,
                        guestCount = guestCount,
                        roomNumber = roomNumber,
                        checkInDate = checkInDate,
                        checkOutDate = checkOutDate,
                        totalPrice = totalPrice.replace(',', '.').toDoubleOrNull() ?: 0.0,
                        status = status
                    )
                    if (bookingId == 0L) dao.insert(booking) else dao.update(booking)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = guestName.isNotBlank() && roomNumber.isNotBlank()
            ) {
                Text("Зберегти")
            }
        }
    }
}
