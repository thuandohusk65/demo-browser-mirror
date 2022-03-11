package com.nhnextsoft.control.funtion

interface PurchaseListener {
    fun onProductPurchased(productId: String?, transactionDetails: String?)
    fun displayErrorMessage(errorMsg: String?)
    fun onUserCancelBilling()
}