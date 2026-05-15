package com.example.hotelapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hotelapp.data.Booking
import com.example.hotelapp.data.BookingDatabase
import java.text.SimpleDateFormat
import java.util.Locale

enum class SortOrder {
    NONE, ASCENDING, DESCENDING
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingListScreen(navController: NavController) {
    val context = LocalContext.current
    val dao = remember { BookingDatabase.getDatabase(context).bookingDao() }
    
    var selectedStatus by remember { mutableStateOf("Всі") }
    var sortOrder by remember { mutableStateOf(SortOrder.NONE) }
    
    // Стан пагінації
    var currentPage by remember { mutableIntStateOf(0) }
    val pageSize = 5

    // Стан для списку бронювань та загальної статистики
    var displayedBookings by remember { mutableStateOf(emptyList<Booking>()) }
    var totalFilteredCount by remember { mutableIntStateOf(0) }
    var totalAmountByFilter by remember { mutableStateOf(0.0) }

    val listState = rememberLazyListState()

    // Скидання сторінки при зміні фільтрів
    LaunchedEffect(selectedStatus, sortOrder) {
        currentPage = 0
    }

    // Завантаження даних при зміні сторінки, фільтрів або сортування
    LaunchedEffect(selectedStatus, sortOrder, currentPage) {
        val sortParam = when (sortOrder) {
            SortOrder.ASCENDING -> "ASC"
            SortOrder.DESCENDING -> "DESC"
            else -> null
        }
        
        // Отримуємо статистику (по всьому відфільтрованому набору)
        totalFilteredCount = dao.getTotalCount(selectedStatus)
        totalAmountByFilter = dao.getTotalAmount(selectedStatus) ?: 0.0
        
        // Отримуємо тільки поточну сторінку
        displayedBookings = dao.getAll(
            status = selectedStatus, 
            sortByDate = sortParam,
            limit = pageSize,
            offset = currentPage * pageSize
        )
        
        listState.scrollToItem(0)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Бронювання") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("form/0")
            }) {
                Icon(Icons.Default.Add, contentDescription = "Додати")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            
            // Міні статистика (тепер через прямі SQL запити)
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatItem("Всього (${selectedStatus})", totalFilteredCount.toString())
                    StatItem("Дохід (без скас.)", "${totalAmountByFilter.toInt()} грн")
                }
            }

            // Фільтри та сортування
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                var expandedStatus by remember { mutableStateOf(false) }
                
                Box {
                    AssistChip(
                        onClick = { expandedStatus = true },
                        label = { Text("Статус: $selectedStatus") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }
                    )
                    DropdownMenu(expanded = expandedStatus, onDismissRequest = { expandedStatus = false }) {
                        listOf("Всі", "Очікується", "Завершено", "Скасовано").forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status) },
                                onClick = {
                                    selectedStatus = status
                                    expandedStatus = false
                                }
                            )
                        }
                    }
                }

                IconButton(onClick = {
                    sortOrder = when (sortOrder) {
                        SortOrder.NONE -> SortOrder.ASCENDING
                        SortOrder.ASCENDING -> SortOrder.DESCENDING
                        SortOrder.DESCENDING -> SortOrder.NONE
                    }
                }) {
                    val icon = when (sortOrder) {
                        SortOrder.NONE -> Icons.AutoMirrored.Filled.Sort
                        SortOrder.ASCENDING -> Icons.Default.ArrowUpward
                        SortOrder.DESCENDING -> Icons.Default.ArrowDownward
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = "Сортування",
                        tint = if (sortOrder != SortOrder.NONE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (displayedBookings.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Нічого не знайдено")
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayedBookings, key = { it.id }) { booking ->
                        BookingCard(
                            booking = booking,
                            onEdit = { navController.navigate("form/${booking.id}") },
                            onDelete = {
                                dao.deleteById(booking.id)
                                // Тригеримо оновлення
                                totalFilteredCount = dao.getTotalCount(selectedStatus)
                                totalAmountByFilter = dao.getTotalAmount(selectedStatus) ?: 0.0
                                displayedBookings = dao.getAll(
                                    status = selectedStatus,
                                    sortByDate = if (sortOrder == SortOrder.ASCENDING) "ASC" else if (sortOrder == SortOrder.DESCENDING) "DESC" else null,
                                    limit = pageSize,
                                    offset = currentPage * pageSize
                                )
                            }
                        )
                    }
                }
            }

            // Навігація по сторінках
            val totalPages = kotlin.math.ceil(totalFilteredCount.toDouble() / pageSize).toInt()
            if (totalPages > 1) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (currentPage > 0) currentPage-- },
                        enabled = currentPage > 0
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                    
                    Text(
                        "Сторінка ${currentPage + 1} з $totalPages",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    IconButton(
                        onClick = { if (currentPage < totalPages - 1) currentPage++ },
                        enabled = currentPage < totalPages - 1
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Вперед")
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun BookingCard(
    booking: Booking,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Видалити?") },
            text = { Text("Видалити бронювання #${booking.id} (${booking.guestName})?") },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    onDelete()
                }) { Text("Так") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Ні") }
            }
        )
    }

    val statusColor = when (booking.status) {
        "Завершено" -> Color(0xFF4CAF50)
        "Скасовано" -> Color(0xFFF44336)
        else -> Color(0xFFFF9800)
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("#${booking.id} ${booking.guestName}", style = MaterialTheme.typography.titleMedium)
                Surface(
                    modifier = Modifier.size(12.dp),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = statusColor
                ) {}
            }
            Text("Кімната: ${booking.roomNumber}")
            Text("Заїзд: ${dateFormat.format(booking.checkInDate)} — Виїзд: ${dateFormat.format(booking.checkOutDate)}")
            Text("Сума: ${booking.totalPrice} грн")
            Text("Статус: ${booking.status}")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Редагувати")
                }
                IconButton(onClick = { showDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Видалити")
                }
            }
        }
    }
}
