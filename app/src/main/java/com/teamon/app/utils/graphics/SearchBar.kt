package com.teamon.app.utils.graphics

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(modifier: Modifier = Modifier, placeholder: String = "Search...", query: String, onQueryChange: (String)->Unit, searchActive: Boolean, onSearchActiveChange: (Boolean)->Unit, content: @Composable ()->Unit) {
    DockedSearchBar(
        modifier = modifier,
        query = query,
        shape = RoundedCornerShape(20.dp),
        colors = SearchBarDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow, dividerColor = MaterialTheme.colorScheme.surfaceContainer),
        trailingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search tasks") },
        leadingIcon = {
            //if(query != "")
                IconButton(onClick = { onSearchActiveChange(false) }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back from search")
                }
        },
        onQueryChange = onQueryChange,
        onSearch = { },
        active = searchActive,
        onActiveChange = onSearchActiveChange,
        placeholder = { Text(placeholder) }) {

            content()

    }
}