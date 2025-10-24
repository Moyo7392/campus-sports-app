package edu.uta.campussports.screens

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DatePickerField(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    label: String = "Date",
    placeholder: String = "Select a date"
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val today = Calendar.getInstance()

    // Parse selected date if it exists
    if (selectedDate.isNotEmpty()) {
        try {
            val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
            val date = dateFormat.parse(selectedDate)
            if (date != null) {
                calendar.time = date
            }
        } catch (e: Exception) {
            // If parsing fails, use today
        }
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth)

            // Check if selected date is in the future
            if (selectedCalendar.after(today)) {
                val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
                onDateSelected(dateFormat.format(selectedCalendar.time))
            } else {
                // Show error - date must be in the future
                // This could be handled by the parent composable
                onDateSelected("") // Clear the date to show error
            }
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Set minimum date to today
    datePickerDialog.datePicker.minDate = today.timeInMillis

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { datePickerDialog.show() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (selectedDate.isEmpty()) placeholder else selectedDate,
                style = MaterialTheme.typography.bodyMedium,
                color = if (selectedDate.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )
        }

        Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
    }
}
