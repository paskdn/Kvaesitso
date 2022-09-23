package de.mm20.launcher2.ui.launcher.search

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.mm20.launcher2.search.data.Searchable
import de.mm20.launcher2.ui.R
import de.mm20.launcher2.ui.component.LauncherCard
import de.mm20.launcher2.ui.component.PartialLauncherCard
import de.mm20.launcher2.ui.launcher.modals.EditFavoritesSheet
import de.mm20.launcher2.ui.launcher.search.calculator.CalculatorItem
import de.mm20.launcher2.ui.launcher.search.common.grid.GridItem
import de.mm20.launcher2.ui.launcher.search.common.list.ListItem
import de.mm20.launcher2.ui.launcher.search.favorites.SearchFavoritesVM
import de.mm20.launcher2.ui.launcher.search.hidden.HiddenResults
import de.mm20.launcher2.ui.launcher.search.unitconverter.UnitConverterItem
import de.mm20.launcher2.ui.launcher.search.website.WebsiteItem
import de.mm20.launcher2.ui.launcher.search.wikipedia.WikipediaItem
import de.mm20.launcher2.ui.locals.LocalGridColumns
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlin.math.ceil

@Composable
fun SearchColumn(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    state: LazyListState = rememberLazyListState(),
    reverse: Boolean = false,
) {

    val columns = LocalGridColumns.current

    val viewModel: SearchVM = viewModel()

    val favoritesVM: SearchFavoritesVM = viewModel()
    val favorites by favoritesVM.favorites.collectAsState(emptyList())

    val showLabels by viewModel.showLabels.observeAsState(true)

    var showWorkProfileApps by remember { mutableStateOf(false) }

    val hideFavs by viewModel.hideFavorites.observeAsState(true)
    val favoritesEnabled by viewModel.favoritesEnabled.collectAsState(false)
    val apps by viewModel.appResults.observeAsState(emptyList())
    val workApps by viewModel.workAppResults.observeAsState(emptyList())
    val appShortcuts by viewModel.appShortcutResults.observeAsState(emptyList())
    val contacts by viewModel.contactResults.observeAsState(emptyList())
    val files by viewModel.fileResults.observeAsState(emptyList())
    val events by viewModel.calendarResults.observeAsState(emptyList())
    val unitConverter by viewModel.unitConverterResult.observeAsState(null)
    val calculator by viewModel.calculatorResult.observeAsState(null)
    val wikipedia by viewModel.wikipediaResult.observeAsState(null)
    val website by viewModel.websiteResult.observeAsState(null)

    var showEditFavoritesDialog by remember { mutableStateOf(false) }

    val pinnedTags by favoritesVM.pinnedTags.collectAsState(emptyList())
    val selectedTag by favoritesVM.selectedTag.collectAsState(null)
    val tagsScrollState = rememberScrollState()

    LazyColumn(
        state = state,
        modifier = modifier,
        contentPadding = paddingValues,
        reverseLayout = reverse,
    ) {
        if (!hideFavs && favoritesEnabled) {
            GridResults(
                items = favorites.toImmutableList(),
                columns = columns,
                showLabels = showLabels,
                key = "favorites",
                reverse = reverse,
                before = if (favorites.isEmpty()) {
                    {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    top = if (!reverse) 28.dp else 16.dp,
                                    start = 16.dp,
                                    end = 16.dp,
                                    bottom = if (reverse) 28.dp else 16.dp,
                                ),
                            text = stringResource(
                                if (selectedTag == null) R.string.favorites_empty else R.string.favorites_empty_tag
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                } else null,
                after = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = if (reverse) 8.dp else 4.dp,
                                bottom = if (reverse) 4.dp else 8.dp,
                                end = 8.dp
                            ),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(tagsScrollState)
                                .padding(end = 12.dp),
                        ) {
                            FilterChip(
                                modifier = Modifier.padding(start = 16.dp),
                                selected = selectedTag == null,
                                onClick = { favoritesVM.selectTag(null) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Star,
                                        contentDescription = null
                                    )
                                },
                                label = { Text(stringResource(R.string.favorites)) }
                            )
                            for (tag in pinnedTags) {
                                FilterChip(
                                    modifier = Modifier.padding(start = 8.dp),
                                    selected = selectedTag == tag.tag,
                                    onClick = { favoritesVM.selectTag(tag.tag) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Rounded.Tag,
                                            contentDescription = null
                                        )
                                    },
                                    label = { Text(tag.label) }
                                )
                            }
                        }
                        SmallFloatingActionButton(
                            elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                            onClick = { showEditFavoritesDialog = true }
                        ) {
                            Icon(imageVector = Icons.Rounded.Edit, contentDescription = null)
                        }
                    }
                }
            )
        }
        GridResults(
            items = if (showWorkProfileApps && workApps.isNotEmpty()) workApps.toImmutableList() else apps.toImmutableList(),
            columns = columns,
            showLabels = showLabels,
            reverse = reverse,
            key = "apps",
            before = if (workApps.isNotEmpty()) {
                {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .padding(
                                top = if (reverse) 4.dp else 8.dp,
                                bottom = if (reverse) 8.dp else 4.dp
                            ),
                    ) {
                        FilterChip(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            selected = !showWorkProfileApps,
                            onClick = { showWorkProfileApps = false },
                            leadingIcon = {
                                Icon(imageVector = Icons.Rounded.Person, contentDescription = null)
                            },
                            label = {
                                Text(
                                    stringResource(R.string.apps_profile_main),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                        FilterChip(
                            selected = showWorkProfileApps,
                            onClick = { showWorkProfileApps = true },
                            leadingIcon = {
                                Icon(imageVector = Icons.Rounded.Work, contentDescription = null)
                            },
                            label = {
                                Text(
                                    stringResource(R.string.apps_profile_work),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }
            } else null
        )
        ListResults(appShortcuts.toImmutableList(), reverse, key = "shortcuts")
        val uc = unitConverter
        if (uc != null) {
            SingleResult {
                UnitConverterItem(unitConverter = uc)
            }
        }
        val calc = calculator
        if (calc != null) {
            SingleResult {
                CalculatorItem(calculator = calc)
            }
        }
        ListResults(events.toImmutableList(), reverse, key = "events")
        ListResults(contacts.toImmutableList(), reverse, key = "contacts")
        val wiki = wikipedia
        if (wiki != null) {
            SingleResult {
                WikipediaItem(wikipedia = wiki)
            }
        }
        val ws = website
        if (ws != null) {
            SingleResult {
                WebsiteItem(website = ws)
            }
        }
        ListResults(files.toImmutableList(), reverse, key = "files")
        item {
            HiddenResults()
        }
    }

    if (showEditFavoritesDialog) {
        EditFavoritesSheet(
            onDismiss = { showEditFavoritesDialog = false }
        )
    }
}

fun LazyListScope.GridResults(
    items: ImmutableList<Searchable>,
    columns: Int,
    reverse: Boolean,
    showLabels: Boolean,
    key: String,
    before: (@Composable () -> Unit)? = null,
    after: (@Composable () -> Unit)? = null,
) {
    if (items.isEmpty() && before == null && after == null) return

    if (before != null) {
        item(key = "$key-before") {
            PartialCardRow(
                isFirst = true,
                isLast = items.isEmpty() && after == null,
                reverse = reverse
            ) {
                before()
            }
        }
    }

    val rows = ceil(items.size / columns.toFloat()).toInt()
    items(
        rows,
        key = {
            "$key-${items[it * columns].key}"
        }
    ) {
        PartialCardRow(
            isFirst = it == 0 && before == null,
            isLast = it == rows - 1 && after == null,
            reverse = reverse
        ) {
            GridRow(
                modifier = Modifier.padding(
                    top = if (if (reverse) it == rows - 1 else it == 0) 4.dp else 0.dp,
                    bottom = if (if (reverse) it == 0 else it == rows - 1) 2.dp else 0.dp,
                ),
                items = items.subList(
                    it * columns,
                    (it * columns + columns).coerceAtMost(items.size)
                ),
                columns = columns,
                showLabels = showLabels,
            )
        }
    }

    if (after != null) {
        item(key = "$key-after") {
            PartialCardRow(
                isFirst = items.isEmpty() && before == null,
                isLast = true,
                reverse = reverse
            ) {
                after()
            }
        }
    }
}

@Composable
fun GridRow(
    modifier: Modifier = Modifier,
    items: ImmutableList<Searchable>,
    columns: Int,
    showLabels: Boolean,
) {

    Row(
        modifier = modifier
    ) {
        for (item in items) {
            GridItem(
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp, 8.dp),
                item = item,
                showLabels = showLabels
            )
        }
        for (i in 0 until columns - items.size) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

fun LazyListScope.ListResults(
    items: ImmutableList<Searchable>,
    reverse: Boolean,
    key: String,
    before: (@Composable () -> Unit)? = null,
    after: (@Composable () -> Unit)? = null,
) {
    if (items.isEmpty()) return
    if (before != null) {
        item(key = "$key-before") {
            PartialCardRow(isFirst = true, isLast = false, reverse = reverse) {
                before()
            }
        }
    }
    items(
        items.size,
        key = {
            "$key-${items[it].key}"
        }
    ) {
        PartialCardRow(
            isFirst = it == 0 && before == null,
            isLast = it == items.lastIndex && after == null,
            reverse = reverse
        ) {
            ListRow(
                modifier = Modifier.padding(
                    top = if (if (reverse) it == items.size - 1 else it == 0) 8.dp else 4.dp,
                    bottom = if (if (reverse) it == 0 else it == items.size - 1) 8.dp else 4.dp,
                ),
                item = items[it],
            )
        }
    }
    if (after != null) {
        item(key = "$key-after") {
            PartialCardRow(isFirst = false, isLast = true, reverse = reverse) {
                after()
            }
        }
    }
}

@Composable
fun ListRow(
    modifier: Modifier = Modifier,
    item: Searchable,
) {
    Box(
        modifier = modifier.padding(
            start = 8.dp,
            end = 8.dp,
        )
    ) {
        ListItem(
            modifier = Modifier
                .fillMaxWidth(),
            item = item
        )
    }
}

fun LazyListScope.SingleResult(content: @Composable (() -> Unit)?) {
    if (content == null) return
    item {
        LauncherCard(
            modifier = Modifier
                .padding(
                    horizontal = 8.dp,
                    vertical = 4.dp,
                )
                .animateItemPlacement()
        ) {
            content()
        }
    }
}

@Composable
fun LazyItemScope.PartialCardRow(
    modifier: Modifier = Modifier,
    isFirst: Boolean,
    isLast: Boolean,
    reverse: Boolean,
    content: @Composable () -> Unit
) {
    val isTop = isFirst && !reverse || isLast && reverse
    val isBottom = isLast && !reverse || isFirst && reverse
    Box(
        modifier = modifier
            .clipToBounds()
            .animateItemPlacement()
    ) {
        PartialLauncherCard(
            modifier = Modifier.padding(
                start = 8.dp,
                end = 8.dp,
                top = if (isTop) 4.dp else 0.dp,
                bottom = if (isBottom) 4.dp else 0.dp,
            ),
            isTop = isTop,
            isBottom = isBottom,
        ) {
            content()
        }
    }
}