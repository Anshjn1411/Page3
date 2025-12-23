package dev.infa.page3.presentation.viewModel

import dev.infa.page3.data.model.Address
import dev.infa.page3.data.model.AddressDetail
import dev.infa.page3.data.model.AddressRequest
import dev.infa.page3.presentation.repositary.AddressRepository
import dev.infa.page3.presentation.uiSatateClaases.ListUiState
import dev.infa.page3.presentation.uiSatateClaases.OperationUiState
import dev.infa.page3.presentation.uiSatateClaases.SingleUiState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class AddressViewModel(
    private val repository: AddressRepository
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Create Address State
    private val _createAddressState = MutableStateFlow<SingleUiState<AddressDetail>>(SingleUiState.Idle)
    val createAddressState: StateFlow<SingleUiState<AddressDetail>> = _createAddressState.asStateFlow()

    // User Addresses List State
    private val _userAddressesState = MutableStateFlow<ListUiState<AddressDetail>>(ListUiState.Idle)
    val userAddressesState: StateFlow<ListUiState<AddressDetail>> = _userAddressesState.asStateFlow()

    // Default Address State
    private val _defaultAddressState = MutableStateFlow<SingleUiState<AddressDetail>>(SingleUiState.Idle)
    val defaultAddressState: StateFlow<SingleUiState<AddressDetail>> = _defaultAddressState.asStateFlow()

    // Address Details State
    private val _addressDetailsState = MutableStateFlow<SingleUiState<AddressDetail>>(SingleUiState.Idle)
    val addressDetailsState: StateFlow<SingleUiState<AddressDetail>> = _addressDetailsState.asStateFlow()

    // Update Address State
    private val _updateAddressState = MutableStateFlow<SingleUiState<AddressDetail>>(SingleUiState.Idle)
    val updateAddressState: StateFlow<SingleUiState<AddressDetail>> = _updateAddressState.asStateFlow()

    // Delete Address State
    private val _deleteAddressState = MutableStateFlow<OperationUiState>(OperationUiState.Idle)
    val deleteAddressState: StateFlow<OperationUiState> = _deleteAddressState.asStateFlow()

    // Set Default Address State
    private val _setDefaultState = MutableStateFlow<SingleUiState<AddressDetail>>(SingleUiState.Idle)
    val setDefaultState: StateFlow<SingleUiState<AddressDetail>> = _setDefaultState.asStateFlow()

    /**
     * Create a new address
     */
    fun createAddress(
        firstName: String,
        lastName: String,
        streetAddress: String,
        city: String,
        state: String,
        zipCode: String,
        mobile: String,
        isDefault: Boolean = false,
        onSuccess: (AddressDetail) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _createAddressState.value = SingleUiState.Loading

            try {
                val addressRequest = AddressRequest(
                    firstName = firstName,
                    lastName = lastName,
                    streetAddress = streetAddress,
                    city = city,
                    state = state,
                    zipCode = zipCode,
                    mobile = mobile,
                    isDefault = isDefault
                )

                val address = repository.createAddress(addressRequest)

                if (address != null) {
                    _createAddressState.value = SingleUiState.Success(address)
                    onSuccess(address)
                    // Refresh user addresses list
                    loadUserAddresses()
                } else {
                    _createAddressState.value = SingleUiState.Error("Failed to create address")
                    onError("Failed to create address")
                }
            } catch (e: Exception) {
                _createAddressState.value = SingleUiState.Error("Error: ${e.message}")
                onError("Error: ${e.message}")
            }
        }
    }

    /**
     * Load current user's addresses
     */
    fun loadUserAddresses() {
        viewModelScope.launch {
            _userAddressesState.value = ListUiState.Loading

            try {
                val addresses = repository.getUserAddresses()
                _userAddressesState.value = when {
                    addresses.isEmpty() -> ListUiState.Empty
                    else -> ListUiState.Success(addresses)
                }
            } catch (e: Exception) {
                _userAddressesState.value = ListUiState.Error("Failed to load addresses: ${e.message}")
            }
        }
    }

    /**
     * Load default address
     */
    fun loadDefaultAddress() {
        viewModelScope.launch {
            _defaultAddressState.value = SingleUiState.Loading

            try {
                val address = repository.getDefaultAddress()
                _defaultAddressState.value = if (address != null) {
                    SingleUiState.Success(address)
                } else {
                    SingleUiState.Error("No default address found")
                }
            } catch (e: Exception) {
                _defaultAddressState.value = SingleUiState.Error("Error: ${e.message}")
            }
        }
    }

    /**
     * Load address by ID
     */
    fun loadAddressById(addressId: String) {
        viewModelScope.launch {
            _addressDetailsState.value = SingleUiState.Loading

            try {
                val address = repository.getAddressById(addressId)
                _addressDetailsState.value = if (address != null) {
                    SingleUiState.Success(address)
                } else {
                    SingleUiState.Error("Address not found")
                }
            } catch (e: Exception) {
                _addressDetailsState.value = SingleUiState.Error("Error: ${e.message}")
            }
        }
    }

    /**
     * Update an existing address
     */
    fun updateAddress(
        addressId: String,
        firstName: String,
        lastName: String,
        streetAddress: String,
        city: String,
        state: String,
        zipCode: String,
        mobile: String,
        isDefault: Boolean = false,
        onSuccess: (AddressDetail) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _updateAddressState.value = SingleUiState.Loading

            try {
                val addressRequest = AddressRequest(
                    firstName = firstName,
                    lastName = lastName,
                    streetAddress = streetAddress,
                    city = city,
                    state = state,
                    zipCode = zipCode,
                    mobile = mobile,
                    isDefault = isDefault
                )

                val updatedAddress = repository.updateAddress(addressId, addressRequest)

                if (updatedAddress != null) {
                    _updateAddressState.value = SingleUiState.Success(updatedAddress)
                    onSuccess(updatedAddress)
                    // Refresh user addresses list
                    loadUserAddresses()
                } else {
                    _updateAddressState.value = SingleUiState.Error("Failed to update address")
                    onError("Failed to update address")
                }
            } catch (e: Exception) {
                _updateAddressState.value = SingleUiState.Error("Error: ${e.message}")
                onError("Error: ${e.message}")
            }
        }
    }

    /**
     * Delete an address
     */
    fun deleteAddress(
        addressId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _deleteAddressState.value = OperationUiState.Loading

            try {
                val success = repository.deleteAddress(addressId)

                if (success) {
                    _deleteAddressState.value = OperationUiState.Success
                    onSuccess()
                    // Refresh user addresses list
                    loadUserAddresses()
                } else {
                    _deleteAddressState.value = OperationUiState.Error("Failed to delete address")
                    onError("Failed to delete address")
                }
            } catch (e: Exception) {
                _deleteAddressState.value = OperationUiState.Error("Error: ${e.message}")
                onError("Error: ${e.message}")
            }
        }
    }

    /**
     * Set an address as default
     */
    fun setAddressAsDefault(
        addressId: String,
        onSuccess: (AddressDetail) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _setDefaultState.value = SingleUiState.Loading

            try {
                val address = repository.setDefaultAddress(addressId)

                if (address != null) {
                    _setDefaultState.value = SingleUiState.Success(address)
                    onSuccess(address)
                    // Refresh user addresses list and default address
                    loadUserAddresses()
                    loadDefaultAddress()
                } else {
                    _setDefaultState.value = SingleUiState.Error("Failed to set default address")
                    onError("Failed to set default address")
                }
            } catch (e: Exception) {
                _setDefaultState.value = SingleUiState.Error("Error: ${e.message}")
                onError("Error: ${e.message}")
            }
        }
    }

    /**
     * Reset create address state
     */
    fun resetCreateAddressState() {
        _createAddressState.value = SingleUiState.Idle
    }

    /**
     * Reset update address state
     */
    fun resetUpdateAddressState() {
        _updateAddressState.value = SingleUiState.Idle
    }

    /**
     * Reset delete address state
     */
    fun resetDeleteAddressState() {
        _deleteAddressState.value = OperationUiState.Idle
    }

    /**
     * Reset set default state
     */
    fun resetSetDefaultState() {
        _setDefaultState.value = SingleUiState.Idle
    }

    /**
     * Clear all states
     */
    fun clearAllStates() {
        _createAddressState.value = SingleUiState.Idle
        _userAddressesState.value = ListUiState.Idle
        _defaultAddressState.value = SingleUiState.Idle
        _addressDetailsState.value = SingleUiState.Idle
        _updateAddressState.value = SingleUiState.Idle
        _deleteAddressState.value = OperationUiState.Idle
        _setDefaultState.value = SingleUiState.Idle
    }

    /**
     * Cancel all ongoing operations
     */
    fun onCleared() {
        viewModelScope.cancel()
    }
}