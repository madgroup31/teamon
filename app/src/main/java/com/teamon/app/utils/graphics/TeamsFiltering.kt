package com.teamon.app.utils.graphics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp


@Composable
fun TeamsFilteringOptionsDropdownMenu(
    filterExpanded: Boolean, onFilterExpandedChange: (Boolean) -> Unit,
    categoryQuery: String, onCategoryQueryChange: (String) -> Unit,
    memberQuery: String, onMemberQueryChange: (String) -> Unit,
    adminQuery: String, onAdminQueryChange: (String) -> Unit
) {
    DropdownMenu(
        expanded = filterExpanded,
        onDismissRequest = { onFilterExpandedChange(false) }
    ) {
        if (categoryQuery.isNotBlank() || memberQuery.isNotBlank() || adminQuery.isNotBlank()) {
            DropdownMenuItem(text = {
                Text(
                    "Reset filters",
                    color = MaterialTheme.colorScheme.error
                )
            },
                leadingIcon = {},
                onClick = {
                    onCategoryQueryChange("")
                    onAdminQueryChange("")
                    onMemberQueryChange("")
                })
            HorizontalDivider(modifier = Modifier.padding(10.dp))
        }
        Column {
            OutlinedTextField(
                label = { Text("Category")},
                modifier = Modifier.padding(start = 10.dp, top = 5.dp, end = 10.dp, bottom = 5.dp).width(150.dp),
                placeholder = { Text("All", fontStyle = FontStyle.Italic)},
                value = categoryQuery, onValueChange = {onCategoryQueryChange(it)},
                maxLines = 1)

        }
        HorizontalDivider(modifier = Modifier.padding(10.dp))
        Column {
            OutlinedTextField(
                label = { Text("Admins") },
                modifier = Modifier.padding(start = 10.dp, top = 5.dp, end = 10.dp, bottom = 5.dp).width(150.dp),
                placeholder = { Text("All", fontStyle = FontStyle.Italic)},
                value = adminQuery, onValueChange = {onAdminQueryChange(it)},
                maxLines = 1)
        }
        HorizontalDivider(modifier = Modifier.padding(10.dp))
        Column {
            OutlinedTextField(
                label = { Text("Members") },
                modifier = Modifier.padding(start = 10.dp, top = 5.dp, end = 10.dp, bottom = 5.dp).width(150.dp),
                placeholder = { Text("All", fontStyle = FontStyle.Italic)},
                value = memberQuery, onValueChange = {onMemberQueryChange(it)},
                maxLines = 1)
        }
    }
}