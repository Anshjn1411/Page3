package dev.infa.page3.data.model



import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.collections.isNotEmpty

@Serializable
data class ApiResponse<T>(
    val success: Boolean = true,
    val message: String? = null,
    val data: T? = null,
    val error: String? = null
)


// Update User Request Model
@Serializable
data class UpdateUserRequest(
    val firstName: String,
    val lastName: String,
    val email: String? = null,
    val mobile: String,
    val dob: String? = null
)


@Serializable
data class Address(
    val _id: String,
    val firstName: String,
    val lastName: String,
    val streetAddress: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val user: String,
    val mobile: String,
    val isDefault: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int
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
    val _id: String? = null,
    val firstName: String,
    val lastName: String,
    val email: String? = null,
    val password: String?=null,
    val role: String = "CUSTOMER",
    val dob: String? = null,
    val mobile: String,
    val addresses: List<Address> = emptyList(),
    val paymentInformation: List<String> = emptyList(),
    val refreshToken:String? = null,
    val ratings: List<String> = emptyList(),
    val reviews: List<String> = emptyList(),
    val createdAt: String? = null,
    val __v: Int?=null
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

// Auth Request/Response Models
@Serializable
data class SendOtpRequest(val mobile: String)

@Serializable
data class SendOtpResponse(val message: String)

@Serializable
data class VerifyOtpRequest(val mobile: String, val otp: String)

@Serializable
data class VerifyOtpResponse(
    val newUser: Boolean? = null,
    val message: String,
    val mobile: String? = null,
    val jwt: String? = null,
    val refreshToken: String? = null
)

@Serializable
data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val mobile: String,
    val email: String? = null,
    val dob: String? = null,
    val password: String? = null
)

@Serializable
data class RegisterResponse(val jwt: String, val message: String)

@Serializable
data class RefreshTokenRequest(val refreshToken: String)

@Serializable
data class RefreshTokenResponse(val jwt: String, val message: String)

@Serializable
data class LogoutResponse(val message: String)


@Serializable
data class UpdateUserResponse(val message: String, val user: User)

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
// ======================= WooCommerce DATA CLASSES =======================

@Serializable
data class WcImage(
    val id: Int? = null,
    val src: String? = null,
    val name: String? = null,
    val alt: String? = null
)

@Serializable
data class WcTermRef(
    val id: Int? = null,
    val name: String? = null,
    val slug: String? = null
)

@Serializable
data class Product(
    val id: Int,
    val name: String,
    val slug: String? = null,
    val permalink: String? = null,
    @SerialName("date_created") val dateCreated: String? = null,
    val type: String? = null,
    val status: String? = null,
    val description: String? = null,
    @SerialName("short_description") val shortDescription: String? = null,
    val sku: String? = null,
    // WooCommerce returns prices as strings
    val price: String? = null,
    @SerialName("regular_price") val regularPrice: String? = null,
    @SerialName("sale_price") val salePrice: String? = null,
    @SerialName("stock_status") val stockStatus: String? = null,
    @SerialName("stock_quantity") val stockQuantity: Int? = null,
    val images: List<WcImage> = emptyList(),
    val categories: List<WcTermRef> = emptyList(),
    val attributes: List<WcAttributes> = emptyList()
)


@Serializable
data class WcAttributes(
    val id: Int,
    val name: String,
    val slug: String? = null,
    val parent: Int? = null,
    val visible: String? = null,
    val variation: String? = null,
    val options: List<String>? = null,
)


@Serializable
data class WcCategory(
    val id: Int,
    val name: String,
    val slug: String? = null,
    val parent: Int? = null,
    val description: String? = null,
    val display: String? = null,
    val image: WcImage? = null,
    @SerialName("menu_order") val menuOrder: Int? = null,
    val count: Int? = null
)

// WooCommerce Customer
@Serializable
data class WcAddress(
    val first_name: String? = null,
    val last_name: String? = null,
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
data class WcCustomer(
    val id: Int? = null,
    val email: String? = null,
    val first_name: String? = null,
    val last_name: String? = null,
    val username: String? = null,
    val billing: WcAddress? = null,
    val shipping: WcAddress? = null
)

// WooCommerce Orders
@Serializable
data class WcOrderLineItem(
    val id: Int? = null,
    @SerialName("product_id") val productId: Int? = null,
    @SerialName("variation_id") val variationId: Int? = null,
    val quantity: Int? = null,
    val subtotal: String? = null,
    val total: String? = null
)

@Serializable
data class WcOrder(
    val id: Int? = null,
    val number: String? = null,
    val status: String? = null,
    val currency: String? = null,
    @SerialName("date_created") val dateCreated: String? = null,
    @SerialName("total") val total: String? = null,
    @SerialName("shipping_total") val shippingTotal: String? = null,
    val billing: WcAddress? = null,
    val shipping: WcAddress? = null,
    @SerialName("line_items") val lineItems: List<WcOrderLineItem> = emptyList()
)

@Serializable
data class WcCreateOrderRequest(
    val payment_method: String,
    val payment_method_title: String,
    val set_paid: Boolean = false,
    val billing: WcAddress? = null,
    val shipping: WcAddress? = null,
    @SerialName("line_items") val lineItems: List<WcOrderLineItem> = emptyList()
)

// WooCommerce Variations
@Serializable
data class WcVariation(
    val id: Int,
    val sku: String? = null,
    val price: String? = null,
    @SerialName("regular_price") val regularPrice: String? = null,
    @SerialName("sale_price") val salePrice: String? = null,
    @SerialName("stock_status") val stockStatus: String? = null,
    val image: WcImage? = null
)

// WooCommerce Reviews
@Serializable
data class WcReview(
    val id: Int,
    @SerialName("product_id") val productId: Int,
    val status: String? = null,
    val reviewer: String? = null,
    val review: String? = null,
    val rating: Int? = null
)

// WooCommerce Coupons
@Serializable
data class WcCoupon(
    val id: Int,
    val code: String,
    @SerialName("discount_type") val discountType: String? = null,
    val amount: String? = null,
    val description: String? = null
)

// WooCommerce Currencies
@Serializable
data class WcCurrency(
    val code: String,
    val name: String,
    val symbol: String
)

// ---------------------- Requests for CRUD ----------------------

@Serializable
data class WcProductCreateRequest(
    val name: String,
    val type: String = "simple",
    val regular_price: String? = null,
    val sale_price: String? = null,
    val description: String? = null,
    val short_description: String? = null,
    val categories: List<WcTermRef> = emptyList(),
    val images: List<WcImage> = emptyList()
)

@Serializable
data class WcProductUpdateRequest(
    val name: String? = null,
    val regular_price: String? = null,
    val sale_price: String? = null,
    val description: String? = null,
    val short_description: String? = null,
    val categories: List<WcTermRef>? = null,
    val images: List<WcImage>? = null
)

@Serializable
data class WcReviewCreateRequest(
    @SerialName("product_id") val productId: Int,
    val review: String,
    val reviewer: String,
    val reviewer_email: String,
    val rating: Int? = null
)

@Serializable
data class WcCustomerCreateRequest(
    val email: String,
    val first_name: String? = null,
    val last_name: String? = null,
    val username: String? = null,
    val billing: WcAddress? = null,
    val shipping: WcAddress? = null
)

@Serializable
data class WcCustomerUpdateRequest(
    val email: String? = null,
    val first_name: String? = null,
    val last_name: String? = null,
    val username: String? = null,
    val billing: WcAddress? = null,
    val shipping: WcAddress? = null
)

@Serializable
data class WcRefund(
    val id: Int,
    val amount: String? = null,
    val reason: String? = null
)

@Serializable
data class WcRefundCreateRequest(
    val amount: String,
    val reason: String? = null,
    val refund_payment: Boolean = false
)

@Serializable
data class WcShippingZone(
    val id: Int,
    val name: String
)

@Serializable
data class WcShippingMethod(
    val id: Int? = null,
    val method_id: String? = null,
    val method_title: String? = null,
    val enabled: Boolean? = null
)

@Serializable
data class WcTaxRate(
    val id: Int,
    val country: String? = null,
    val state: String? = null,
    val rate: String? = null,
    val name: String? = null,
    val shipping: Boolean? = null,
    val compound: Boolean? = null
)

@Serializable
data class WcTaxClass(
    val slug: String,
    val name: String? = null
)

@Serializable
data class WcWebhook(
    val id: Int,
    val name: String? = null,
    val status: String? = null,
    val topic: String? = null,
    val delivery_url: String? = null
)

@Serializable
data class WcWebhookRequest(
    val name: String,
    val topic: String,
    val delivery_url: String
)

@Serializable
data class WcSettingGroup(
    val id: String,
    val name: String? = null
)

@Serializable
data class WcSetting(
    val id: String,
    val label: String? = null,
    val description: String? = null,
    val value: String? = null
)

@Serializable
data class WcSettingUpdateRequest(
    val id: String,
    val value: String
)

@Serializable
data class WcPaymentGateway(
    val id: String,
    val title: String? = null,
    val description: String? = null,
    val enabled: String? = null
)

@Serializable
data class WcCountry(
    val code: String,
    val name: String,
    val states: List<WcState> = emptyList()
)

@Serializable
data class WcState(
    val code: String,
    val name: String
)



// Product with populated data (for detailed views)
@Serializable
data class ProductDetailed(
    val _id: String? = null,
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
    val ratings: List<Rating>? = null, // Populated ratings
    val reviews: List<Review>? = null, // Populated reviews
    val numRatings: Int? = null,
    val category: Category? = null, // Populated category
    val subCategory: SubCategory? = null, // Populated subcategory
    val createdAt: String? = null,
    val isWishlisted: Boolean = false,
    val __v: Int?= null
)

// Multiple Products Request
@Serializable
data class MultipleProductsRequest(
    val products: List<ProductRequest>
)

// Product API Responses
typealias CreateProductResponse = ApiResponse<Product>
typealias CreateMultipleProductsResponse = ApiResponse<List<Product>>
typealias GetAllProductsResponse = ApiResponse<List<Product>>
typealias GetProductByIdResponse = ApiResponse<ProductDetailed>
typealias UpdateProductResponse = ApiResponse<Product>
typealias DeleteProductResponse = ApiResponse<Any>
typealias GetProductsByCategoryResponse = ApiResponse<List<Product>>
typealias GetProductsBySubCategoryResponse = ApiResponse<List<Product>>

// WooCommerce Collections (used directly without ApiResponse wrapper)
typealias WcProducts = List<Product>
typealias WcCategories = List<WcCategory>
typealias WcCustomers = List<WcCustomer>
typealias WcOrders = List<WcOrder>
typealias WcVariations = List<WcVariation>
typealias WcReviews = List<WcReview>
typealias WcCoupons = List<WcCoupon>
typealias WcCurrencies = List<WcCurrency>
typealias WcRefunds = List<WcRefund>
typealias WcShippingZones = List<WcShippingZone>
typealias WcShippingMethods = List<WcShippingMethod>
typealias WcTaxRates = List<WcTaxRate>
typealias WcTaxClasses = List<WcTaxClass>
typealias WcWebhooks = List<WcWebhook>
typealias WcSettingGroups = List<WcSettingGroup>
typealias WcSettings = List<WcSetting>
typealias WcPaymentGateways = List<WcPaymentGateway>
typealias WcCountries = List<WcCountry>

// ======================= RATING DATA CLASSES =======================

// Rating Request
@Serializable
data class RatingRequest(
    val productId: String,
    val rating: Int // 1-5 rating
)

@Serializable
data class Rating(
    val _id: String? = null,
    val user: String? = null, // User ID
    val product: String? = null, // Product ID
    val rating: Int? = null,
    val createdAt: String? = null,
    val __v: Int?=null
)

// Rating with populated user data
@Serializable
data class RatingDetailed(
    val _id: String? = null,
    val user: User? = null, // Populated user object
    val product: String? = null, // Product ID
    val rating: Int? = null,
    val createdAt: String? = null,
    val __v: Int?=null
)

// Rating API Responses
typealias CreateRatingResponse = Rating
typealias GetProductRatingsResponse = List<Rating>

// ======================= REVIEW DATA CLASSES =======================

// Review Request
@Serializable
data class ReviewRequest(
    val productId: String,
    val review: String
)

// Review Response
@Serializable
data class Review(
    val _id: String? = null,
    val review: String? = null,
    val product: String? = null, // Product ID
    val user: UserShort? = null, // User ID
    val createdAt: String? = null,
    val __v: Int?=null
)

// Review with populated user data
@Serializable
data class ReviewDetailed(
    val _id: String? = null,
    val review: String? = null,
    val product: String? = null, // Product ID
    val user: UserShort? = null, // Populated user object
    val createdAt: String? = null,
    val __v: Int?=null
)

// Review API Responses
typealias CreateReviewResponse = Review
typealias GetAllReviewsResponse = List<ReviewDetailed>


// ======================= QUERY PARAMETERS =======================

// Product Query Parameters (for filtering and pagination)
@Serializable
data class ProductQueryParams(
    val page: Int? = null,
    val limit: Int? = null,
    val category: String? = null,
    val subCategory: String? = null,
    val brand: String? = null,
    val color: String? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val search: String? = null,
    val sortBy: String? = null, // price, rating, createdAt
    val sortOrder: String? = null // asc, desc
)

// Search Query Parameters
// REPLACE your current SearchQueryParams with this:
@Serializable
data class SearchQueryParams(
    val search: String,
    val color: String? = null,
    val sizes: String? = null,  // comma-separated
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val minDiscount: Double? = null,
    val sort: String? = null,  // price_high, price_low, newest, oldest, rating, name_asc, name_desc
    val stock: String? = null,  // in_stock, out_of_stock
    val pageNumber: Int? = null,  // Changed from 'page'
    val pageSize: Int? = null,    // Changed from 'limit'
    val brand: String? = null
)

// ADD this helper data class for filters (optional):

// ADD this new data class for the search response structure:
@Serializable
data class SearchProductsData(
    val content: List<Product> = emptyList(),
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val totalProducts: Int = 0,
    val pageSize: Int = 10,
    val hasNextPage: Boolean = false,
    val hasPrevPage: Boolean = false
)

// MODIFY SearchProductsResponse to:
@Serializable
data class SearchProductsResponse(
    val success: Boolean,
    val data: SearchProductsData? = null,  // Changed from List<Product>
    val message: String? = null
)
// ============= CART DATA CLASSES =============

@Serializable
data class AddToCartRequest(
    val productId: String,
    val size: String? = null,
    val quantity: Int = 1
)

@Serializable
data class CartItem(
    val _id: String? = null,
    val cart: String? = null,
    val product: Product? = null,
    val size: String? = null,
    val quantity: Int = 1,
    val price: Double = 0.0,
    val discountedPrice: Double = 0.0,
    val userId: String? = null,
    val __v: Int?=null
)

@Serializable
data class Cart(
    val _id: String? = null,
    val user: String? = null,
    val cartItems: List<CartItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val totalItem: Int = 0,
    val totalDiscountedPrice: Double = 0.0,
    val discounte: Double = 0.0,
    val __v: Int?=null
)

@Serializable
data class AddToCartResponse(
    val message: String,
    val status: Boolean
)

// ============= WISHLIST DATA CLASSES =============

@Serializable
data class WishlistItem(
    val id: String? = null,
    val product: Product? = null,
    val addedAt: String? = null
)

@Serializable
data class Wishlist(
    val _id: String? = null,
    val user: String? = null,
    val items: List<WishlistItem> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val __v: Int? = null
)

@Serializable
data class WishlistData(
    val wishlist: Wishlist? = null,
    val updated: Boolean = false
)

@Serializable
data class WishlistResponse(
    val success: Boolean,
    val updated: Boolean,
    val message: String? = null,
    val data: WishlistData? = null
)




// ======================= ORDER DATA CLASSES =======================



@Serializable
data class OrderItem(
    val _id: String? = null,
    val product: ProductDetailed1? = null,
    val size: String? = null,
    val quantity: Int,
    val price: Int,
    val discountedPrice: Int,
    val userId: String,
    val deliveryDate: String? = null,
    val __v: Int?=null
)

@Serializable
data class PaymentDetails(
    val paymentMethod: String? = null,
    val transactionId: String? = null,
    val paymentId: String? = null,
    val paymentStatus: String? = null
)

@Serializable
data class Order(
    val id: String? = null,
    val user: String? = null,
    val orderItems: List<String>? = null, // List of OrderItem IDs
    val orderDate: String? = null,
    val deliveryDate: String? = null,
    val orderNumber: String? = null,
    val status: String? = null,
    val total: String? = null,
    val dateCreated: String? = null,

    )

@Serializable
data class OrderDetailed(
    val _id: String,
    val user: String? = null,
    val orderItems: List<OrderItem>,
    val orderDate: String,
    val deliveryDate: String? = null,
    val orderNumber: String,
    val shippingAddress: String?=null,
    val paymentDetails: PaymentDetails,
    val totalPrice: Int,
    val totalDiscountedPrice: Int,
    val discounte: Int,
    val orderStatus: String,
    val totalItem: Int,
    val createdAt: String,
    val __v: Int?=null
)

// Request Models
@Serializable
data class CreateOrderRequest(
    val firstName: String,
    val lastName: String,
    val streetAddress: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val mobile: String,
    val _id: String? = null // Include if using existing address
)

@Serializable
data class PaymentLinkResponse(
    val paymentLinkId: String,
    val payment_link_url: String
)

// Response Type Aliases
typealias CreateOrderResponse = ApiResponse<Order>
typealias GetOrderByIdResponse = ApiResponse<OrderDetailed>
typealias GetOrderHistoryResponse = ApiResponse<List<OrderDetailed>>
typealias CreatePaymentLinkResponse = ApiResponse<PaymentLinkResponse>
typealias UpdatePaymentResponse = ApiResponse<Map<String, Any>>


data class ProductDetails(
    val product: ProductDetailed,
    val averageRating: Double,
    val totalRatings: Int,
    val totalReviews: Int,
    val ratingDistribution: Map<Int, Int>,
    val latestReviews: List<ReviewDetailed>
)

data class CategoryWithSubCategories(
    val category: Category,
    val subCategories: List<SubCategory>
)


/**
 * Request model for creating/updating address
 */
@Serializable
data class AddressRequest(
    val firstName: String,
    val lastName: String,
    val streetAddress: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val mobile: String,
    val isDefault: Boolean = false
)

/**
 * Response wrapper for address operations
 */
@Serializable
data class AddressResponse(
    val message: String,
    val address: AddressDetail
)

@Serializable
data class AddressDeleteResponse(
    val message: String,
    val address: Address
)


@Serializable
data class ProductDetailed1(
    val _id: String? = null,
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
    val ratings: List<Rating>? = null, // Populated ratings
    val reviews: List<Review>? = null, // Populated reviews
    val numRatings: Int? = null,
    val category: String? = null, // Populated category
    val subCategory: String? = null, // Populated subcategory
    val createdAt: String? = null,
    val isWishlisted: Boolean = false,
    val __v: Int?= null
)



// ADD these data classes at the top of your file or in a separate file:
data class SearchState(
    val query: String = "",
    val filters: SearchFilters = SearchFilters(),
    val isFilterSheetOpen: Boolean = false
)

data class SearchFilters(
    val color: String? = null,
    val sizes: List<String> = emptyList(),
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    val minDiscount: Double? = null,
    val sort: String? = null,  // price_high, price_low, newest, oldest, rating, name_asc, name_desc
    val stock: String? = null,  // in_stock, out_of_stock
    val brand: String? = null
) {
    fun toQueryString(): String {
        val parts = mutableListOf<String>()
        color?.let { parts.add("Color: $it") }
        if (sizes.isNotEmpty()) parts.add("Sizes: ${sizes.joinToString(", ")}")
        minPrice?.let { parts.add("Min: ₹$it") }
        maxPrice?.let { parts.add("Max: ₹$it") }
        minDiscount?.let { parts.add("Discount: $it%+") }
        sort?.let { parts.add("Sort: ${getSortLabel(it)}") }
        stock?.let { parts.add("Stock: ${if (it == "in_stock") "In Stock" else "Out of Stock"}") }
        brand?.let { parts.add("Brand: $it") }
        return parts.joinToString(" • ")
    }

    private fun getSortLabel(sort: String): String = when(sort) {
        "price_high" -> "Price High"
        "price_low" -> "Price Low"
        "newest" -> "Newest"
        "oldest" -> "Oldest"
        "rating" -> "Rating"
        "name_asc" -> "Name A-Z"
        "name_desc" -> "Name Z-A"
        else -> sort
    }
}