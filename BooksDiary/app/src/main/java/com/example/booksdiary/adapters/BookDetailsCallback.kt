package com.example.booksdiary.adapters

import com.example.booksdiary.model.Book

interface BookDetailsCallback {
    fun onCallback(value: Book)
}