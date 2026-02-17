package dev.infa.page3.presentation.repositary//package dev.infa.page3.presentation.repositary
//
//import dev.infa.page3.data.model.*
//
//class ECommerceRepository(
//    val categoryRepository: CategoryRepository,
//    val subCategoryRepository: SubCategoryRepository,
//    val productRepository: ProductRepository,
//    val ratingRepository: RatingRepository,
//    val reviewRepository: ReviewRepository
//) {
//
//    // Combined operations for better UX
//    suspend fun getProductWithDetails(productId: String): ProductDetails? {
//        return try {
//            val product = productRepository.getProductById(productId)
//            if (product != null) {
//                val ratings = ratingRepository.getProductRatings(productId)
//                val reviews = reviewRepository.getAllReviews(productId)
//                val averageRating = ratingRepository.getAverageRating(productId)
//                val ratingDistribution = ratingRepository.getRatingDistribution(productId)
//
//                ProductDetails(
//                    product = product,
//                    averageRating = averageRating,
//                    totalRatings = ratings.size,
//                    totalReviews = reviews.size,
//                    ratingDistribution = ratingDistribution,
//                    latestReviews = reviews.take(3)
//                )
//            } else {
//                null
//            }
//        } catch (e: Exception) {
//            null
//        }
//    }
//
//    suspend fun getCategoriesWithSubCategories(): List<CategoryWithSubCategories> {
//        return try {
//            val categories = categoryRepository.getAllCategories()
//            categories.map { category ->
//                val subCategories = category._id?.let {
//                    subCategoryRepository.getSubCategoriesByCategory(it)
//                } ?: emptyList()
//
//                CategoryWithSubCategories(
//                    category = category,
//                    subCategories = subCategories
//                )
//            }
//        } catch (e: Exception) {
//            emptyList()
//        }
//    }
//
//    suspend fun rateAndReviewProduct(
//        productId: String,
//        rating: Int,
//        reviewText: String?
//    ): Pair<Boolean, Boolean> {
//        val ratingSuccess = ratingRepository.createRating(productId, rating)
//        val reviewSuccess = if (!reviewText.isNullOrBlank()) {
//            reviewRepository.createReview(productId, reviewText)
//        } else {
//            true
//        }
//        return Pair(ratingSuccess, reviewSuccess)
//    }
//}
//
//
