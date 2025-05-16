package com.example.littledragons.ui.model

sealed interface ResendEmailVerifyResult {
    data object Success : ResendEmailVerifyResult
    data object Initial : ResendEmailVerifyResult
    data object Failed : ResendEmailVerifyResult
}