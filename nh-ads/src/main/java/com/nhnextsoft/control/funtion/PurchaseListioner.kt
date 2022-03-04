package com.nhnextsoft.control.funtion

interface PurchaseListioner {
    fun onProductPurchased(productId: String?, transactionDetails: String?)
    fun displayErrorMessage(errorMsg: String?)
    fun onUserCancelBilling()
}