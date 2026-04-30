package dev.infa.page3.data.model



import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean = true,
    val message: String? = null,
    val data: T? = null,
    val error: String? = null
)

@Serializable
data class Address(
    val _id: String?  = null,
    val firstName: String?  = null,
    val lastName: String?  = null,
    val streetAddress: String?  = null,
    val zipCode: String?  = null,
    val user: String?  = null,
    val mobile: String?  = null,
    val isDefault: Boolean?  = null,
    val createdAt: String?  = null,
    val updatedAt: String?  = null,
    val __v: Int?  = null,
    val company: String? = null,
    val address_1: String? = null,
    val address_2: String? = null,
    val city: String? = null,
    val state: String? = null,
    val postcode: String? = null,
    val country: String? = null,
    val email: String? = null,
    val phone: String? = null
)

@Serializable
data class AddressDetail(
    val _id: String,
    val firstName: String,
    val lastName: String,
    val streetAddress: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val user: User,
    val mobile: String,
    val isDefault: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int
)
@Serializable
data class User(
    val id: String? = null,
    val first_name: String,
    val last_name: String,
    val email: String? = null,
    val phone: String? = null,
    val addresses: List<Address> = emptyList(),
    val paymentInformation: List<String> = emptyList(),
    val refreshToken:String? = null,
    val ratings: List<String> = emptyList(),
    val reviews: List<String> = emptyList(),
    val username: String? = null,
    val __v: Int?=null,
    val isProfileComplete: Boolean = false,
    val isPhoneVerified: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val billing: WcAddress? = null,
    val shipping: WcAddress? = null
)
@Serializable
data class UserShort(
    val _id: String? = null,
    val firstName: String,
    val lastName: String,
    val email: String? = null,
    val password: String?=null,
    val role: String = "CUSTOMER",
    val dob: String? = null,
    val mobile: String,
    val addresses: List<String> = emptyList(),
    val paymentInformation: List<String> = emptyList(),
    val refreshToken:String? = null,
    val ratings: List<String> = emptyList(),
    val reviews: List<String> = emptyList(),
    val createdAt: String? = null,
    val __v: Int?=null
)

@Serializable
data class Size(
    val _id: String? = null,
    val name: String? = null,
    val quantity: Int? = null
)

// ======================= CATEGORY DATA CLASSES =======================

// Category Request (for create/update)
@Serializable
data class CategoryRequest(
    val name: String? = null,
    val image: String? = null
)

// Category Response
@Serializable
data class Category(
    val _id: String? = null,
    val name: String? = null,
    val image: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val __v: Int?= null
)

// Category API Responses
typealias CreateCategoryResponse = ApiResponse<Category>
typealias GetAllCategoriesResponse = ApiResponse<List<Category>>
typealias GetCategoryByIdResponse = ApiResponse<Category>
typealias UpdateCategoryResponse = ApiResponse<Category>
typealias DeleteCategoryResponse = ApiResponse<Any>

// ======================= SUBCATEGORY DATA CLASSES =======================

// SubCategory Request (for create/update)
@Serializable
data class SubCategoryRequest(
    val name: String? = null,
    val image: String? = null,
    val category: List<String>? = null // Array of category IDs
)

// SubCategory Response
@Serializable
data class SubCategory(
    val _id: String? = null,
    val name: String? = null,
    val image: String? = null,
    val category: List<String>? = null, // Array of category IDs
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val __v: Int?= null
)

// SubCategory API Responses
typealias CreateSubCategoryResponse = ApiResponse<SubCategory>
typealias GetAllSubCategoriesResponse = ApiResponse<List<SubCategory>>
typealias GetSubCategoryByIdResponse = ApiResponse<SubCategory>
typealias UpdateSubCategoryResponse = ApiResponse<SubCategory>
typealias DeleteSubCategoryResponse = ApiResponse<Any>

// ======================= PRODUCT DATA CLASSES =======================

// Product Request (legacy backend) - retained for backward compatibility
@Serializable
data class ProductRequest(
    val title: String? = null,
    val description: String? = null,
    val price: Double? = null,
    val discountedPrice: Double? = null,
    val discountPersent: Int? = null,
    val quantity: Int? = null,
    val brand: String? = null,
    val color: String? = null,
    val sizes: List<Size>? = null,
    val imageUrl: String? = null,
    val category: Category = Category(),
    val subCategory: SubCategory = SubCategory()
)



@Serializable
data class CartItemWithAttributes(
    val id: Int,
    val name: String,
    val price: String? = null,
    val regularPrice: String? = null,
    val salePrice: String? = null,
    val images: List<WcImage> = emptyList(),
    val shortDescription: String? = null,
    val stockQuantity: Int? = null,
    val selectedAttributes: Map<String, String> = emptyMap(), // size -> "M", color -> "Blue"
    val quantity: Int = 1
)

// Helper function to create CartItemWithAttributes from Product
fun Product.toCartItem(selectedAttributes: Map<String, String> = emptyMap()): CartItemWithAttributes {
    return CartItemWithAttributes(
        id = this.id,
        name = this.name,
        price = this.price,
        regularPrice = this.regularPrice,
        salePrice = this.salePrice,
        images = this.images,
        shortDescription = this.shortDescription,
        stockQuantity = this.stockQuantity,
        selectedAttributes = selectedAttributes,
        quantity = 1
    )
}
