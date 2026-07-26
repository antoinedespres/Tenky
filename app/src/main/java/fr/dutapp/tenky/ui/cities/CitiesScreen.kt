package fr.dutapp.tenky.ui.cities

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DragHandle
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt
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
    val snackbarHostState = remember { SnackbarHostState() }

    val removed = state.lastRemoved
    val undoMessage = stringResource(R.string.city_removed, removed?.city?.name.orEmpty())
    val undoLabel = stringResource(R.string.action_undo)
    LaunchedEffect(removed) {
        if (removed == null) return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = undoMessage,
            actionLabel = undoLabel,
            // Long rather than Short: an accidental swipe needs enough time to
            // be noticed and reversed.
            duration = SnackbarDuration.Long,
        )
        if (result == SnackbarResult.ActionPerformed) {
            viewModel.undoRemove()
        } else {
            viewModel.onUndoDismissed()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    onMove = viewModel::moveCity,
                    onMoveFinished = viewModel::onReorderFinished,
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

/**
 * The saved cities, reorderable by the drag handle and removable by swiping.
 *
 * Rows are a fixed height so the drag can work out which row it is over from
 * the offset alone, without measuring each one.
 */
@Composable
private fun SavedCityList(
    rows: List<CityRow>,
    onSelect: (SavedCity) -> Unit,
    onRemove: (SavedCity) -> Unit,
    onMove: (Int, Int) -> Unit,
    onMoveFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val rowHeightPx = with(LocalDensity.current) { ROW_HEIGHT.toPx() }

    LazyColumn(modifier = modifier.fillMaxSize()) {
        itemsIndexed(
            items = rows,
            key = { _, row -> "${row.city.latitude},${row.city.longitude}" },
        ) { index, row ->
            val isDragging = draggedIndex == index

            Box(
                modifier = Modifier
                    // The dragged row floats above its neighbours.
                    .zIndex(if (isDragging) 1f else 0f)
                    .graphicsLayer { translationY = if (isDragging) dragOffset else 0f },
            ) {
                SwipeableCityRow(
                    row = row,
                    // Swiping a row that is being dragged would fight the drag.
                    swipeEnabled = draggedIndex == null,
                    onSelect = { onSelect(row.city) },
                    onRemove = { onRemove(row.city) },
                    dragHandle = {
                        DragHandle(
                            modifier = Modifier.pointerInput(index, rows.size) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        draggedIndex = index
                                        dragOffset = 0f
                                    },
                                    onDragEnd = {
                                        draggedIndex = null
                                        dragOffset = 0f
                                        onMoveFinished()
                                    },
                                    // Also commits: the rows have already been
                                    // reordered on screen by this point, so a
                                    // cancelled gesture must not leave the new
                                    // order unsaved and reverting on restart.
                                    onDragCancel = {
                                        draggedIndex = null
                                        dragOffset = 0f
                                        onMoveFinished()
                                    },
                                    onDrag = { change, delta ->
                                        change.consume()
                                        dragOffset += delta.y
                                        val from = draggedIndex ?: return@detectDragGesturesAfterLongPress
                                        // Swap once the row has travelled a
                                        // whole row height, then rebase the
                                        // offset so it keeps following the finger.
                                        val target = from + (dragOffset / rowHeightPx).roundToInt()
                                        if (target != from && target in rows.indices) {
                                            onMove(from, target)
                                            draggedIndex = target
                                            dragOffset -= (target - from) * rowHeightPx
                                        }
                                    },
                                )
                            },
                        )
                    },
                )
            }
            HorizontalDivider()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableCityRow(
    row: CityRow,
    swipeEnabled: Boolean,
    onSelect: () -> Unit,
    onRemove: () -> Unit,
    dragHandle: @Composable () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            val dismissed = value != SwipeToDismissBoxValue.Settled
            if (dismissed) onRemove()
            dismissed
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        gesturesEnabled = swipeEnabled,
        backgroundContent = { RemoveBackground(dismissState.dismissDirection) },
    ) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    row.temperature?.let { temperature ->
                        Text(
                            text = formatTemperature(temperature),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    dragHandle()
                }
            },
            modifier = Modifier
                .height(ROW_HEIGHT)
                .clickable(onClick = onSelect),
        )
    }
}

@Composable
private fun DragHandle(modifier: Modifier = Modifier) {
    Icon(
        imageVector = Icons.Default.DragHandle,
        contentDescription = stringResource(R.string.action_reorder_city),
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .padding(start = 12.dp)
            // Comfortably bigger than the glyph, so the handle is easy to grab.
            .size(40.dp)
            .padding(8.dp),
    )
}

/** Red wash revealed behind a row as it is swiped away. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RemoveBackground(direction: SwipeToDismissBoxValue) {
    if (direction == SwipeToDismissBoxValue.Settled) return

    val alignment = if (direction == SwipeToDismissBoxValue.StartToEnd) {
        Alignment.CenterStart
    } else {
        Alignment.CenterEnd
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = 24.dp),
        contentAlignment = alignment,
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}

private val ROW_HEIGHT = 88.dp
