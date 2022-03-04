package com.nhnextsoft.control.billing

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.android.billingclient.api.*
import com.nhnextsoft.control.application.AppGlobal
import com.nhnextsoft.control.funtion.BillingListener
import com.nhnextsoft.control.funtion.PurchaseListioner
import timber.log.Timber
import java.text.NumberFormat
import java.util.*

class AppPurchase private constructor() {

    var price = "1.49$"
        get() = getPrice(productId)
    private var oldPrice = "2.99$"
    private var productId: String? = null
    private var listSubcriptionId: MutableList<String> = mutableListOf()
    private var listINAPId: MutableList<String> = mutableListOf()
    private var purchaseListioner: PurchaseListioner? = null
    private var billingListener: BillingListener? = null
    var initBillingFinish = false
        private set
    private var billingClient: BillingClient? = null
    private var skuListINAPFromStore: List<SkuDetails>? = null
    private var skuListSubsFromStore: List<SkuDetails>? = null
    private val skuDetailsINAPMap: MutableMap<String?, SkuDetails> = HashMap()
    private val skuDetailsSubsMap: MutableMap<String, SkuDetails> = HashMap()
    var isAvailable = false
        private set
    private var isListGot = false
    private var isConsumePurchase = false

    //tracking purchase adjust
    private var idPurchaseCurrent = ""
    private var typeIap = 0

    fun setPurchaseListener(purchaseListener: PurchaseListioner) {
        this.purchaseListioner = purchaseListener
    }

    /**
     * listener init billing app
     *
     * @param billingListener
     */
    fun setBillingListener(billingListener: BillingListener) {
        this.billingListener = billingListener
        if (isAvailable) {
            billingListener.onInitBillingListener(0)
            initBillingFinish = true
        }
    }

    /**
     * listener init billing app with timeout
     *
     * @param billingListener
     * @param timeout
     */
    fun setBillingListener(billingListener: BillingListener, timeout: Int) {
        this.billingListener = billingListener
        if (isAvailable) {
            billingListener.onInitBillingListener(0)
            initBillingFinish = true
            return
        }
        Handler(Looper.getMainLooper()).postDelayed({
            if (!initBillingFinish) {
                Timber.e("setBillingListener: timeout ")
                initBillingFinish = true
                billingListener.onInitBillingListener(BillingClient.BillingResponseCode.ERROR)
            }
        }, timeout.toLong())
    }

    fun setConsumePurchase(consumePurchase: Boolean) {
        isConsumePurchase = consumePurchase
    }

    fun setOldPrice(oldPrice: String) {
        this.oldPrice = oldPrice
    }

    var purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, list ->
        Timber.e("onPurchasesUpdated code: " + billingResult.responseCode)
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && list != null) {
            for (purchase in list) {
                purchase.skus
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            if (purchaseListioner != null) purchaseListioner?.onUserCancelBilling()
            Timber.d("onPurchasesUpdated:USER_CANCELED ")
        } else {
            Timber.d("onPurchasesUpdated:... ")
        }
    }

    var purchaseClientStateListener: BillingClientStateListener =
        object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                isAvailable = false
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                Timber.d("onBillingSetupFinished:  " + billingResult.responseCode)
                if (billingListener != null && !initBillingFinish) billingListener?.onInitBillingListener(
                    billingResult.responseCode)
                initBillingFinish = true
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    isAvailable = true
                    val params = SkuDetailsParams.newBuilder()
                    params.setSkusList(listINAPId).setType(BillingClient.SkuType.INAPP)
                    billingClient?.querySkuDetailsAsync(params.build()) { _, list ->
                        if (list != null) {
                            Timber.d("onSkuINAPDetailsResponse: " + list.size)
                            skuListINAPFromStore = list
                            isListGot = true
                            addSkuINAPToMap(list)
                        }
                    }
                    params.setSkusList(listSubcriptionId).setType(BillingClient.SkuType.SUBS)
                    billingClient?.querySkuDetailsAsync(params.build()) { _, list ->
                        if (list != null) {
                            Timber.d("onSkuSubsDetailsResponse: " + list.size)
                            skuListSubsFromStore = list
                            isListGot = true
                            addSkuSubsToMap(list)
                        }
                    }
                } else if (billingResult.responseCode == BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE || billingResult.responseCode == BillingClient.BillingResponseCode.ERROR) {
                    Timber.e("onBillingSetupFinished:ERROR ")
                }
            }
        }

    fun setProductId(productId: String?) {
        this.productId = productId
    }

    fun addSubcriptionId(id: String) {
        listSubcriptionId.add(id)
    }

    fun addProductId(id: String) {
        listINAPId.add(id)
    }

    fun initBilling(application: Application) {
        listSubcriptionId = ArrayList()
        listINAPId = ArrayList()
        if (AppGlobal.BUILD_DEBUG) {
            listINAPId.add(PRODUCT_ID_TEST)
        }
        billingClient = BillingClient.newBuilder(application)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
        billingClient?.startConnection(purchaseClientStateListener)
    }

    fun initBilling(
        application: Application,
        listINAPId: MutableList<String>,
        listSubsId: MutableList<String>,
    ) {
        listSubcriptionId = listSubsId
        this.listINAPId = listINAPId
        if (AppGlobal.BUILD_DEBUG) {
            listINAPId.add(PRODUCT_ID_TEST)
        }
        billingClient = BillingClient.newBuilder(application)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
        billingClient?.startConnection(purchaseClientStateListener)
    }

    private fun addSkuSubsToMap(skuList: List<SkuDetails>) {
        for (skuDetails in skuList) {
            skuDetailsSubsMap[skuDetails.sku] = skuDetails
        }
    }

    private fun addSkuINAPToMap(skuList: List<SkuDetails>) {
        for (skuDetails in skuList) {
            skuDetailsINAPMap[skuDetails.sku] = skuDetails
        }
    }

    //check all id INAP + Subs
    fun isPurchased(context: Context?): Boolean {
        if (listINAPId.isNotEmpty()) {
            val result = billingClient?.queryPurchases(BillingClient.SkuType.INAPP)
            if (result?.responseCode == BillingClient.BillingResponseCode.OK && result?.purchasesList != null) {
                for (purchase in result.purchasesList!!) {
                    for (id in listINAPId) {
                        if (purchase.skus.contains(id)) {
                            return true
                        }
                    }
                }
            }
        }
        if (listSubcriptionId.isNotEmpty()) {
            val result = billingClient?.queryPurchases(BillingClient.SkuType.SUBS)
            if (result?.responseCode == BillingClient.BillingResponseCode.OK && result.purchasesList != null) {
                for (purchase in result.purchasesList!!) {
                    for (id in listSubcriptionId) {
                        if (purchase.skus.contains(id)) {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    //check  id INAP
    fun isPurchased(context: Context?, productId: String): Boolean {
        Timber.d("isPurchased: $productId")
        val resultINAP = billingClient?.queryPurchases(BillingClient.SkuType.INAPP)
        if (resultINAP?.responseCode == BillingClient.BillingResponseCode.OK && resultINAP?.purchasesList != null) {
            for (purchase in resultINAP.purchasesList!!) {
                if (purchase.skus.contains(productId)) {
                    return true
                }
            }
        }
        val resultSubs = billingClient?.queryPurchases(BillingClient.SkuType.SUBS)
        if (resultSubs?.responseCode == BillingClient.BillingResponseCode.OK && resultSubs?.purchasesList != null) {
            for (purchase in resultSubs?.purchasesList!!) {
                if (purchase.orderId.equals(productId, ignoreCase = true)) {
                    return true
                }
            }
        }
        return false
    }

    fun purchase(activity: Activity) {
        if (productId == null) {
            Timber.e("Purchase false:productId null")
            Toast.makeText(activity, "Product id must not be empty!", Toast.LENGTH_SHORT).show()
            return
        }
        purchase(activity, productId!!)
    }

    fun purchase(activity: Activity, productId: String): String {
        var productId = productId
        if (skuListINAPFromStore == null) {
            if (purchaseListioner != null) purchaseListioner?.displayErrorMessage("Billing error init")
            return ""
        }
        if (AppGlobal.BUILD_DEBUG) {
            // Dùng ID Purchase test khi debug
            productId = PRODUCT_ID_TEST
        }
        val skuDetails = skuDetailsINAPMap[productId] ?: return "Product ID invalid"
        idPurchaseCurrent = productId
        typeIap = TYPE_IAP.PURCHASE
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(skuDetails)
            .build()
        val responseCode = billingClient?.launchBillingFlow(activity, billingFlowParams)
        when (responseCode?.responseCode) {
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
                if (purchaseListioner != null) purchaseListioner?.displayErrorMessage("Billing not supported for type of request")
                return "Billing not supported for type of request"
            }
            BillingClient.BillingResponseCode.ITEM_NOT_OWNED, BillingClient.BillingResponseCode.DEVELOPER_ERROR -> return ""
            BillingClient.BillingResponseCode.ERROR -> {
                if (purchaseListioner != null) purchaseListioner?.displayErrorMessage("Error completing request")
                return "Error completing request"
            }
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> return "Error processing request."
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> return "Selected item is already owned"
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> return "Item not available"
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> return "Play Store service is not connected now"
            BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> return "Timeout"
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> {
                if (purchaseListioner != null) purchaseListioner?.displayErrorMessage("Network error.")
                return "Network Connection down"
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                if (purchaseListioner != null) purchaseListioner?.displayErrorMessage("Request Canceled")
                return "Request Canceled"
            }
            BillingClient.BillingResponseCode.OK -> return "Subscribed Successfully"
        }
        return ""
    }

    fun subscribe(activity: Activity, SubsId: String): String {
        if (skuListSubsFromStore == null) {
            if (purchaseListioner != null) purchaseListioner?.displayErrorMessage("Billing error init")
            return ""
        }
        if (AppGlobal.BUILD_DEBUG) {
            // sử dụng ID Purchase test
            purchase(activity, PRODUCT_ID_TEST)
            return "Billing test"
        }
        val skuDetails = skuDetailsSubsMap[SubsId]
        idPurchaseCurrent = SubsId
        typeIap = TYPE_IAP.SUBSCRIPTION
        if (skuDetails == null) {
            return "SubsId invalid"
        }
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(skuDetails)
            .build()
        val responseCode = billingClient?.launchBillingFlow(activity, billingFlowParams)
        when (responseCode?.responseCode) {
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
                if (purchaseListioner != null) purchaseListioner?.displayErrorMessage("Billing not supported for type of request")
                return "Billing not supported for type of request"
            }
            BillingClient.BillingResponseCode.ITEM_NOT_OWNED, BillingClient.BillingResponseCode.DEVELOPER_ERROR -> return ""
            BillingClient.BillingResponseCode.ERROR -> {
                if (purchaseListioner != null) purchaseListioner?.displayErrorMessage("Error completing request")
                return "Error completing request"
            }
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> return "Error processing request."
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> return "Selected item is already owned"
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> return "Item not available"
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> return "Play Store service is not connected now"
            BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> return "Timeout"
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> {
                if (purchaseListioner != null) purchaseListioner?.displayErrorMessage("Network error.")
                return "Network Connection down"
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                if (purchaseListioner != null) purchaseListioner?.displayErrorMessage("Request Canceled")
                return "Request Canceled"
            }
            BillingClient.BillingResponseCode.OK -> return "Subscribed Successfully"
        }
        return ""
    }

    fun consumePurchase() {
        if (productId == null) {
            Timber.e("Consume Purchase false:productId null ")
            return
        }
        consumePurchase(productId)
    }

    fun consumePurchase(productId: String?) {
        var pc: Purchase? = null
        val resultINAP = billingClient?.queryPurchases(BillingClient.SkuType.INAPP)
        if (resultINAP?.responseCode == BillingClient.BillingResponseCode.OK && resultINAP.purchasesList != null) {
            for (purchase in resultINAP.purchasesList!!) {
                if (purchase.skus.contains(productId)) {
                    pc = purchase
                }
            }
        }
        if (pc == null) return
        try {
            val consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(pc.purchaseToken)
                .build()
            val listener = ConsumeResponseListener { billingResult, purchaseToken ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Timber.e("onConsumeResponse: OK")
                }
            }
            billingClient?.consumeAsync(consumeParams, listener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handlePurchase(purchase: Purchase) {

        //tracking adjust
        val price = getPriceWithoutCurrency(idPurchaseCurrent, typeIap)
        val currentcy = getCurrency(idPurchaseCurrent, typeIap)
        if (purchaseListioner != null) purchaseListioner?.onProductPurchased(purchase.orderId,
            purchase.originalJson)
        if (isConsumePurchase) {
            val consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            val listener =
                ConsumeResponseListener { billingResult: BillingResult, purchaseToken: String? ->
                    Timber.d("onConsumeResponse: " + billingResult.debugMessage)
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    }
                }
            billingClient?.consumeAsync(consumeParams, listener)
        } else {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                if (!purchase.isAcknowledged) {
                    billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult: BillingResult ->
                        Timber.d("onAcknowledgePurchaseResponse: " + billingResult.debugMessage)
                    }
                }
            }
        }
    }

    fun getPrice(productId: String?): String {
        val skuDetails = skuDetailsINAPMap[productId] ?: return ""
        Timber.e("getPrice: " + skuDetails.price)
        return skuDetails.price
    }

    fun getPriceSub(productId: String): String {
        val skuDetails = skuDetailsSubsMap[productId] ?: return ""
        return skuDetails.price
    }

    fun getIntroductorySubPrice(productId: String): String {
        val skuDetails = skuDetailsSubsMap[productId] ?: return ""
        return skuDetails.price
    }

    fun getCurrency(productId: String, typeIAP: Int): String {
        val skuDetails =
            (if (typeIAP == TYPE_IAP.PURCHASE) skuDetailsINAPMap[productId] else skuDetailsSubsMap[productId])
                ?: return ""
        return skuDetails.priceCurrencyCode
    }

    fun getPriceWithoutCurrency(productId: String, typeIAP: Int): Double {
        val skuDetails =
            (if (typeIAP == TYPE_IAP.PURCHASE) skuDetailsINAPMap[productId] else skuDetailsSubsMap[productId])
                ?: return 0.0
        return skuDetails.priceAmountMicros.toDouble()
    }

    private fun formatCurrency(price: Double, currency: String): String {
        val format = NumberFormat.getCurrencyInstance()
        format.maximumFractionDigits = 0
        format.currency = Currency.getInstance(currency)
        return format.format(price)
    }

    var discount = 1.0

    //    @IntDef(TYPE_IAP.PURCHASE, TYPE_IAP.SUBSCRIPTION)
//    @Retention(AnnotationRetention.SOURCE)
    annotation class TYPE_IAP {
        companion object {
            const val PURCHASE = 1
            const val SUBSCRIPTION = 2
        }
    }

    companion object {

        private val LICENSE_KEY: String? = null
        private val MERCHANT_ID: String? = null
        private const val TAG = "PurchaseEG"
        const val PRODUCT_ID_TEST = "android.test.purchased"

        val instance = AppPurchase()
    }
}