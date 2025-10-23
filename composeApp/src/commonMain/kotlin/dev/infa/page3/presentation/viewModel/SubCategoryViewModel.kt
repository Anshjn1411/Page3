package dev.infa.page3.presentation.viewModel

import dev.infa.page3.data.model.SubCategory
import dev.infa.page3.data.model.SubCategoryRequest
import dev.infa.page3.presentation.repositary.SubCategoryRepository
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

class SubCategoryViewModel(
    private val repository: SubCategoryRepository
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _subCategoriesState = MutableStateFlow<ListUiState<SubCategory>>(ListUiState.Idle)
    val subCategoriesState: StateFlow<ListUiState<SubCategory>> = _subCategoriesState.asStateFlow()

    private val _selectedSubCategoryState = MutableStateFlow<SingleUiState<SubCategory>>(SingleUiState.Idle)
    val selectedSubCategoryState: StateFlow<SingleUiState<SubCategory>> = _selectedSubCategoryState.asStateFlow()

    private val _operationState = MutableStateFlow<OperationUiState>(OperationUiState.Idle)
    val operationState: StateFlow<OperationUiState> = _operationState.asStateFlow()

    fun loadAllSubCategories() {
        viewModelScope.launch {
            _subCategoriesState.value = ListUiState.Loading
            try {
                val subCategories = repository.getAllSubCategories()
                _subCategoriesState.value = when {
                    subCategories.isEmpty() -> ListUiState.Empty
                    else -> ListUiState.Success(subCategories)
                }
            } catch (e: Exception) {
                _subCategoriesState.value = ListUiState.Error("Failed to load subcategories: ${e.message}")
            }
        }
    }

    fun loadSubCategoriesByCategory(categoryId: String) {
        viewModelScope.launch {
            _subCategoriesState.value = ListUiState.Loading
            try {
                val subCategories = repository.getSubCategoriesByCategory(categoryId)
                _subCategoriesState.value = when {
                    subCategories.isEmpty() -> ListUiState.Empty
                    else -> ListUiState.Success(subCategories)
                }
            } catch (e: Exception) {
                _subCategoriesState.value = ListUiState.Error("Failed to load subcategories: ${e.message}")
            }
        }
    }

    fun getSubCategoryById(subCategoryId: String) {
        viewModelScope.launch {
            _selectedSubCategoryState.value = SingleUiState.Loading
            try {
                val subCategory = repository.getSubCategoryById(subCategoryId)
                _selectedSubCategoryState.value = if (subCategory != null) {
                    SingleUiState.Success(subCategory)
                } else {
                    SingleUiState.Error("SubCategory not found")
                }
            } catch (e: Exception) {
                _selectedSubCategoryState.value = SingleUiState.Error("Failed to load subcategory: ${e.message}")
            }
        }
    }

    fun createSubCategory(name: String, image: String, categoryIds: List<String>) {
        viewModelScope.launch {
            _operationState.value = OperationUiState.Loading
            try {
                val request = SubCategoryRequest(name, image, categoryIds)
                val result = repository.createSubCategory(request)
                _operationState.value = if (result != null) {
                    loadAllSubCategories() // Refresh the list
                    OperationUiState.Success
                } else {
                    OperationUiState.Error("Failed to create subcategory")
                }
            } catch (e: Exception) {
                _operationState.value = OperationUiState.Error("Error creating subcategory: ${e.message}")
            }
        }
    }

    fun updateSubCategory(subCategoryId: String, name: String, image: String, categoryIds: List<String>) {
        viewModelScope.launch {
            _operationState.value = OperationUiState.Loading
            try {
                val request = SubCategoryRequest(name, image, categoryIds)
                val result = repository.updateSubCategory(subCategoryId, request)
                _operationState.value = if (result != null) {
                    loadAllSubCategories() // Refresh the list
                    OperationUiState.Success
                } else {
                    OperationUiState.Error("Failed to update subcategory")
                }
            } catch (e: Exception) {
                _operationState.value = OperationUiState.Error("Error updating subcategory: ${e.message}")
            }
        }
    }

    fun deleteSubCategory(subCategoryId: String) {
        viewModelScope.launch {
            _operationState.value = OperationUiState.Loading
            try {
                val result = repository.deleteSubCategory(subCategoryId)
                _operationState.value = if (result) {
                    loadAllSubCategories() // Refresh the list
                    OperationUiState.Success
                } else {
                    OperationUiState.Error("Failed to delete subcategory")
                }
            } catch (e: Exception) {
                _operationState.value = OperationUiState.Error("Error deleting subcategory: ${e.message}")
            }
        }
    }

    fun clearOperationState() {
        _operationState.value = OperationUiState.Idle
    }
}