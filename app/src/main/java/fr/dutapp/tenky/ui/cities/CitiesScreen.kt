package fr.dutapp.tenky.ui.cities

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.dutapp.tenky.R
import fr.dutapp.tenky.domain.model.SavedCity
import fr.dutapp.tenky.ui.components.MessageState
import fr.dutapp.tenky.ui.formatTemperature
import fr.dutapp.tenky.ui.messageRes
import fr.dutapp.tenky.util.WeatherIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitiesScreen(
    onNavigateBack: () -> Unit,
    onCitySelected: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CitiesViewModel = viewModel(factory = CitiesViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_all_cities)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
                actions = {
                    if (state.rows.isNotEmpty()) {
                        IconButton(onClick = viewModel::clearAll) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = stringResource(R.string.action_clear_all),
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            SearchField(
                query = state.query,
                isSearching = state.isSearching,
                onQueryChange = viewModel::onQueryChange,
            )

            when {
                state.error != null -> MessageState(
                    title = stringResource(state.error!!.messageRes()),
                )

                state.searchResults.isNotEmpty() -> SearchResults(
                    results = state.searchResults,
                    onAdd = viewModel::addCity,
                )

                state.query.isNotBlank() && state.searchPerformed && !state.isSearching ->
                    MessageState(title = stringResource(R.string.cities_search_empty))

                state.rows.isEmpty() -> MessageState(
                    title = stringResource(R.string.cities_empty_title),
                    body = stringResource(R.string.cities_empty_body),
                )

                else -> SavedCityList(
                    rows = state.rows,
                    onSelect = { city ->
                        viewModel.selectCity(city)
                        onCitySelected()
                    },
                    onRemove = viewModel::removeCity,
                )
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    isSearching: Boolean,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        singleLine = true,
        label = { Text(stringResource(R.string.cities_search_hint)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.action_search),
            )
        },
        trailingIcon = {
            when {
                isSearching -> CircularProgressIndicator(modifier = Modifier.size(20.dp))
                query.isNotEmpty() -> IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.action_close),
                    )
                }
            }
        },
    )
}

@Composable
private fun SearchResults(
    results: List<SavedCity>,
    onAdd: (SavedCity) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(results) { city ->
            ListItem(
                headlineContent = { Text(city.name) },
                supportingContent = { Text(city.qualifiedName) },
                modifier = Modifier.clickable { onAdd(city) },
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun SavedCityList(
    rows: List<CityRow>,
    onSelect: (SavedCity) -> Unit,
    onRemove: (SavedCity) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(rows, key = { "${it.city.latitude},${it.city.longitude}" }) { row ->
            ListItem(
                headlineContent = { Text(row.city.name) },
                supportingContent = { Text(row.city.qualifiedName) },
                leadingContent = {
                    row.iconCode?.let { code ->
                        Image(
                            painter = painterResource(WeatherIcons.forCode(code)),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                        )
                    }
                },
                trailingContent = {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        row.temperature?.let { temperature ->
                            Text(
                                text = formatTemperature(temperature),
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                        IconButton(onClick = { onRemove(row.city) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(
                                    R.string.action_remove_city,
                                    row.city.name,
                                ),
                            )
                        }
                    }
                },
                modifier = Modifier.clickable { onSelect(row.city) },
            )
            HorizontalDivider()
        }
    }
}
