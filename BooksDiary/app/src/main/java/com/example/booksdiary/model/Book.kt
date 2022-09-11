package com.example.booksdiary.model

class Book (
    var bid: String?,
    var uid: String?,
    var title: String?,
    var author: String?,
    var description: String?,
    var notes: String?,
    var favorites: Boolean?,
    var wishlist: Boolean?,
    var thumbnail: String?,
    var pdf: String?,
    var pdfFileName: String?
) {
    constructor() : this("", "", "", "", "", "", false, false, "", "", "")
}