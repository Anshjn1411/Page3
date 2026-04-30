@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package dev.infa.page3.ui.components



import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.infa.page3.data.model.SearchFilters

@Composable
fun SearchBarWithFilters(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    hasActiveFilters: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(52.dp)
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)
                    )
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 12.dp, end = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        "Search products…",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                singleLine = true
            )
            IconButton(onClick = onFilterClick) {
                BadgedBox(
                    badge = {
                        if (hasActiveFilters) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(8.dp)
                            )
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filters",
                        tint = if (hasActiveFilters) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveFiltersChips(
    filters: SearchFilters,
    onClearAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = false,
            onClick = onClearAll,
            label = { Text("Clear all", fontSize = 12.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        )
        Text(
            text = filters.toQueryString(),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}

@Composable
fun SortDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    currentSort: String?,
    onSortSelected: (String) -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        val sortOptions = listOf(
            "price_low" to "Price: Low to High",
            "price_high" to "Price: High to Low",
            "newest" to "Newest First",
            "oldest" to "Oldest First",
            "name_asc" to "Name: A to Z",
            "name_desc" to "Name: Z to A"
        )
        sortOptions.forEach { (value, label) ->
            DropdownMenuItem(
                text = { Text(label) },
                onClick = { onSortSelected(value) },
                leadingIcon = if (currentSort == value) {
                    { Icon(Icons.Default.Check, contentDescription = null) }
                } else null
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFilterBottomSheet(
    filters: SearchFilters,
    onDismiss: () -> Unit,
    onApplyFilters: (SearchFilters) -> Unit
) {
    var tempFilters by remember(filters) { mutableStateOf(filters) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.fillMaxHeight(0.92f),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    )
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Refine results",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { tempFilters = SearchFilters() }) {
                    Text("Reset", color = MaterialTheme.colorScheme.primary)
                }
            }
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    PriceRangeFilterWithSlider(
                        minPrice = tempFilters.minPrice?.toInt() ?: 0,
                        maxPrice = tempFilters.maxPrice?.toInt() ?: 1000,
                        onPriceChange = { min, max ->
                            tempFilters = tempFilters.copy(minPrice = min, maxPrice = max)
                        }
                    )
                }
                item {
                    SizeFilter(
                        selectedSizes = tempFilters.sizes,
                        onSizesChange = { tempFilters = tempFilters.copy(sizes = it) }
                    )
                }
                item {
                    StockStatusFilter(
                        selectedStock = tempFilters.stock,
                        onStockChange = { tempFilters = tempFilters.copy(stock = it) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { onApplyFilters(tempFilters) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    "Apply filters",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PriceRangeFilterWithSlider(
    minPrice: Int,
    maxPrice: Int,
    onPriceChange: (Int, Int) -> Unit
) {
    var localMinPrice by remember(minPrice, maxPrice) { mutableStateOf(minPrice.toFloat()) }
    var localMaxPrice by remember(minPrice, maxPrice) { mutableStateOf(maxPrice.toFloat()) }

    Column {
        Text(
            text = "Price range",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))
        RangeSlider(
            value = localMinPrice..localMaxPrice,
            onValueChange = { range ->
                localMinPrice = range.start
                localMaxPrice = range.endInclusive
            },
            valueRange = 0f..1000f,
            onValueChangeFinished = {
                onPriceChange(localMinPrice.toInt(), localMaxPrice.toInt())
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            steps = 99
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "₹${localMinPrice.toInt()} — ₹${localMaxPrice.toInt()}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Button(
                onClick = { onPriceChange(localMinPrice.toInt(), localMaxPrice.toInt()) },
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text("Set", fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun SizeFilter(
    selectedSizes: List<String>,
    onSizesChange: (List<String>) -> Unit
) {
    val sizes = listOf("XS", "S", "M", "L", "XL", "XXL")
    Column {
        Text(
            text = "Size",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            sizes.forEach { size ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            val newSizes = if (selectedSizes.contains(size)) {
                                selectedSizes.filter { it != size }
                            } else {
                                selectedSizes + size
                            }
                            onSizesChange(newSizes)
                        }
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = size,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (selectedSizes.contains(size)) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .background(
                                color = if (selectedSizes.contains(size)) MaterialTheme.colorScheme.primary
                                else Color.Transparent,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(6.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedSizes.contains(size)) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StockStatusFilter(
    selectedStock: String?,
    onStockChange: (String?) -> Unit
) {
    Column {
        Text(
            text = "Availability",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("in_stock" to "In stock", "out_of_stock" to "Out of stock").forEach { (value, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            onStockChange(if (selectedStock == value) null else value)
                        }
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (selectedStock == value) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .background(
                                color = if (selectedStock == value) MaterialTheme.colorScheme.primary
                                else Color.Transparent,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(6.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedStock == value) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
