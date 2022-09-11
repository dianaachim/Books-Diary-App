package com.example.booksdiary.model

class User (
    var uid: String?,
    var username: String?,
    var email: String?
) {
    constructor() : this("", "", "")
}