package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path

@JsonClass(generateAdapter = true)
data class NewsSourceResponse(
    @Json(name = "status") val status: String,
    @Json(name = "totalResults") val totalResults: Int?,
    @Json(name = "articles") val articles: List<NewsArticle>?
)

@JsonClass(generateAdapter = true)
data class NewsArticle(
    @Json(name = "source") val source: ArticleSource?,
    @Json(name = "author") val author: String?,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String?,
    @Json(name = "url") val url: String,
    @Json(name = "urlToImage") val urlToImage: String?,
    @Json(name = "publishedAt") val publishedAt: String,
    @Json(name = "content") val content: String?
)

@JsonClass(generateAdapter = true)
data class ArticleSource(
    @Json(name = "id") val id: String?,
    @Json(name = "name") val name: String
)

interface NewsApiService {
    // Top headlines for general or tech / business
    @GET("top-headlines/category/{category}/us.json")
    suspend fun getTopHeadlines(
        @Path("category") category: String
    ): NewsSourceResponse

    // Everything from specific sources
    @GET("everything/{source}.json")
    suspend fun getSourceNews(
        @Path("source") source: String
    ): NewsSourceResponse
}
