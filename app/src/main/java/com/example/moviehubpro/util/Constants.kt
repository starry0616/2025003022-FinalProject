package com.example.moviehubpro.util

object Constants {
    const val API_KEY = "c314f31ed2133228a6ba649239abe528"
    const val BASE_URL = "https://api.themoviedb.org/3/"
    const val DEFAULT_LANGUAGE = "zh-CN"
    
    // 使用全球图片代理加速，将原本的图片请求转发到加速服务器
    // 格式：https://images.weserv.nl/?url=image.tmdb.org/t/p/
    const val POSTER_BASE_URL = "https://images.weserv.nl/?url=image.tmdb.org/t/p" 
    
    const val POSTER_W342 = "w342"
    const val POSTER_W780 = "w780"
    const val POSTER_W500 = "w500"
    
    const val DATABASE_NAME = "movie_pro_db"
    
    const val ERROR_NO_INTERNET = "网络未连接，请检查您的设置"
    const val ERROR_TIMEOUT = "请求超时，请稍后重试"
    const val ERROR_SERVER = "服务器开小差了，请重试"
    const val ERROR_AUTH = "API 密钥配置错误"
    const val ERROR_UNKNOWN = "发生未知错误"
}
