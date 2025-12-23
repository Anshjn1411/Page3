package dev.infa.page3.presentation.viewModel

import dev.infa.page3.data.model.WcCategory
import dev.infa.page3.presentation.repositary.CategoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dev.infa.page3.presentation.uiSatateClaases.ListUiState
import dev.infa.page3.presentation.uiSatateClaases.OperationUiState
import dev.infa.page3.presentation.uiSatateClaases.SingleUiState

class CategoryViewModel(
    private val repository: CategoryRepository
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // ====== StateFlows ======
    private val _categoriesState = MutableStateFlow<ListUiState<WcCategory>>(ListUiState.Idle)
    val categoriesState: StateFlow<ListUiState<WcCategory>> = _categoriesState.asStateFlow()

    private val _selectedCategoryState = MutableStateFlow<SingleUiState<WcCategory>>(SingleUiState.Idle)
    val selectedCategoryState: StateFlow<SingleUiState<WcCategory>> = _selectedCategoryState.asStateFlow()

    private val _operationState = MutableStateFlow<OperationUiState>(OperationUiState.Idle)
    val operationState: StateFlow<OperationUiState> = _operationState.asStateFlow()

    // ====== Caching ======
    private var cachedCategories: List<WcCategory>? = null
    private var isCategoriesLoaded = false

    init {
        loadCategories() // initial load
    }

    // Load categories with caching
    fun loadCategories() {
        // If already loaded and cache exists, use it
        if (isCategoriesLoaded && cachedCategories != null) {
            _categoriesState.value = if (cachedCategories!!.isEmpty()) ListUiState.Empty
            else ListUiState.Success(cachedCategories!!)
            return
        }

        viewModelScope.launch {
            _categoriesState.value = ListUiState.Loading
            try {
                val categories = repository.getAllCategories()
                cachedCategories = categories
                isCategoriesLoaded = true
                _categoriesState.value = when {
                    categories.isEmpty() -> ListUiState.Empty
                    else -> ListUiState.Success(categories)
                }
            } catch (e: Exception) {
                _categoriesState.value = ListUiState.Error("Failed to load categories: ${e.message}")
            }
        }
    }

    // Force refresh categories (ignores cache)
    fun refreshCategories() {
        viewModelScope.launch {
            _categoriesState.value = ListUiState.Loading
            try {
                val categories = repository.getAllCategories()
                cachedCategories = categories
                isCategoriesLoaded = true
                _categoriesState.value = if (categories.isEmpty()) ListUiState.Empty
                else ListUiState.Success(categories)
            } catch (e: Exception) {
                _categoriesState.value = ListUiState.Error("Failed to refresh categories: ${e.message}")
            }
        }
    }

    fun getCategoryById(categoryId: String) {
        viewModelScope.launch {
            _selectedCategoryState.value = SingleUiState.Loading
            try {
                val category = repository.getCategoryById(categoryId)
                _selectedCategoryState.value = if (category != null) {
                    SingleUiState.Success(category)
                } else {
                    SingleUiState.Error("Category not found")
                }
            } catch (e: Exception) {
                _selectedCategoryState.value = SingleUiState.Error("Failed to load category: ${e.message}")
            }
        }
    }

    fun createCategory(category: WcCategory) {
        viewModelScope.launch {
            _operationState.value = OperationUiState.Loading
            try {
                val result = repository.createCategory(category)
                _operationState.value = if (result != null) {
                    refreshCategories() // refresh cache
                    OperationUiState.Success
                } else {
                    OperationUiState.Error("Failed to create category")
                }
            } catch (e: Exception) {
                _operationState.value = OperationUiState.Error("Error creating category: ${e.message}")
            }
        }
    }

    fun updateCategory(categoryId: String, category: WcCategory) {
        viewModelScope.launch {
            _operationState.value = OperationUiState.Loading
            try {
                val result = repository.updateCategory(categoryId, category)
                _operationState.value = if (result != null) {
                    refreshCategories() // refresh cache
                    OperationUiState.Success
                } else {
                    OperationUiState.Error("Failed to update category")
                }
            } catch (e: Exception) {
                _operationState.value = OperationUiState.Error("Error updating category: ${e.message}")
            }
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            _operationState.value = OperationUiState.Loading
            try {
                val result = repository.deleteCategory(categoryId)
                _operationState.value = if (result) {
                    refreshCategories() // refresh cache
                    OperationUiState.Success
                } else {
                    OperationUiState.Error("Failed to delete category")
                }
            } catch (e: Exception) {
                _operationState.value = OperationUiState.Error("Error deleting category: ${e.message}")
            }
        }
    }

    fun clearOperationState() {
        _operationState.value = OperationUiState.Idle
    }
}
