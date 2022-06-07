package com.note.daily.keep.pro;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.note.daily.keep.BuildConfig;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ProHelper {
    public static final String DRAW_PRO_TEST = "android.test.purchased";
    public static final String DRAW_PRO_1 = BuildConfig.DEBUG ? DRAW_PRO_TEST : "draw_pro_1";
    public static final String DRAW_PRO_2 = BuildConfig.DEBUG ? DRAW_PRO_TEST : "draw_pro_2";
    public static final String DRAW_PRO_3 = BuildConfig.DEBUG ? DRAW_PRO_TEST : "draw_pro_3";
    public static final String DRAW_PRO_4 = BuildConfig.DEBUG ? DRAW_PRO_TEST : "draw_pro_4";
    private BillingClient billingClient;
    private BillingClientStateListener billingClientStateListener;
    private PurchaseCallback callback;
    private static ProHelper instance;
    private List<SkuDetails> skuDetailsListIAP = new ArrayList<>();

    public ProHelper setCallback(PurchaseCallback callback) {
        this.callback = callback;
        return this;
    }

    private final PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {

        public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                && purchases != null) {
                for (int i = 0; i < purchases.size(); i++) {
                    handlePurchase(purchases.get(i));
                }
                return;
            }
            if (callback != null) {
                callback.purchaseFail();
            }
        }
    };


    public void purchaseSuccess() {
        if (EventBus.getDefault().hasSubscriberForEvent(ProEvent.class)) {
            EventBus.getDefault().post(new ProEvent());
        }
        ProPrefs.get().updateLocalPurchasedState(true);
        if (callback != null) {
            callback.purchaseSuccessfully();
        }
    }

    void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (purchase.isAcknowledged()) {
                return;
            }

            AcknowledgePurchaseParams acknowledgePurchaseParams =
                AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.getPurchaseToken())
                    .build();
            billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {
                purchaseSuccess();
            });
        }
    }

    public static synchronized ProHelper getInstance() {
        if (instance == null || instance.billingClient == null) {
            instance = new ProHelper();
        }
        return instance;
    }

    private ProHelper() {
    }

    public void initBilling(Context context) {
        initBilling(context, null);
    }

    public void initBilling(final Context context, BillingClientStateListener billingClientStateListener) {
        billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build();
        this.billingClientStateListener = billingClientStateListener;
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                onBillingConnected(billingResult);
            }

            @Override
            public void onBillingServiceDisconnected() {
                if (billingClientStateListener != null) {
                    billingClientStateListener.onBillingServiceDisconnected();
                }
            }
        });
    }

    private void onBillingConnected(@NonNull BillingResult billingResult) {
        if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
            if (billingClientStateListener != null) {
                billingClientStateListener.onBillingSetupFinished(billingResult);
            }
            return;
        }
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        if (BuildConfig.DEBUG) {
            params.setSkusList(Arrays.asList(DRAW_PRO_TEST, DRAW_PRO_1, DRAW_PRO_2, DRAW_PRO_3, DRAW_PRO_4));
        } else {
            params.setSkusList(Arrays.asList(DRAW_PRO_1, DRAW_PRO_2, DRAW_PRO_3, DRAW_PRO_4));
        }
        params.setType(BillingClient.SkuType.INAPP);
        billingClient.querySkuDetailsAsync(params.build(),
            (billingResult12, skuDetailsList) -> {
                ProHelper.this.skuDetailsListIAP = skuDetailsList;
                if (billingClientStateListener != null) {
                    billingClientStateListener.onBillingSetupFinished(billingResult);
                }
            });
    }

    public void consume(Context context, String productId) {
        Purchase purchase = getPurchase(productId);
        if (purchase != null) {
            ConsumeParams consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();
            billingClient.consumeAsync(consumeParams, (billingResult, s) -> {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    fetchPurchased(null);
                }
            });
        }
    }

    private Purchase getPurchase(String productId) {
        try {
            Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
            for (Purchase purchase :
                Objects.requireNonNull(purchasesResult.getPurchasesList())) {
                if (purchase.getSkus().contains(productId)) return purchase;
            }
        } catch (Exception exception) {
        }
        return null;
    }

    public boolean isRemovedAds(Context context) {
        if (billingClient == null) {
            initBilling(context);
        }
        return isPurchased() || isSubscribed() || isRemovedAdsLocalState(context);
    }

    public boolean isRemovedAdsLocalState(Context context) {
        ProPrefs pPrefs = ProPrefs.get();
        return pPrefs.isLocalSubscribedState() || pPrefs.isLocalPurchasedState();
    }

    private boolean isPurchased() {
        try {
            Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
            for (Purchase purchase : Objects.requireNonNull(purchasesResult.getPurchasesList())) {
                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                    ProPrefs.get().updateLocalPurchasedState(true);
                    return true;
                }
            }
            ProPrefs.get().updateLocalPurchasedState(false);
        } catch (Exception ignored) {
        }
        return false;
    }

    public void fetchPurchased(ProCallback callback) {
        billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP, (billingResult, list) -> {
            try {
                boolean purchased = false;
                for (Purchase purchase : Objects.requireNonNull(list)) {
                    if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                        purchased = true;
                    }
                }
                if (!purchased) {
                    billingClient.queryPurchasesAsync(BillingClient.SkuType.SUBS, (billingResult1, list1) -> {
                        boolean subscribed = false;
                        for (Purchase purchase : Objects.requireNonNull(list1)) {
                            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                                subscribed = true;
                            }
                        }
                        ProPrefs.get().updateLocalPurchasedState(subscribed);
                        if (callback != null) {
                            if (subscribed) {
                                callback.onPurchased();
                            } else {
                                callback.onAppNotPurchased();
                            }
                        }
                    });
                    return;
                }
                ProPrefs.get().updateLocalPurchasedState(purchased);
                if (callback != null) {
                    callback.onPurchased();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private boolean isSubscribed() {
        try {
            Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.SUBS);
            for (Purchase purchase : Objects.requireNonNull(purchasesResult.getPurchasesList())) {
                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                    ProPrefs.get().updateLocalSubscribedState(true);
                    return true;
                }
            }
            ProPrefs.get().updateLocalSubscribedState(false);
        } catch (Exception exception) {
        }
        return false;
    }

    public void purchase(Activity activity, String productId) {
        try {
            if (billingClient == null) {
                initBilling(activity);
            }
            SkuDetails skuDetails = getSkuDetail(skuDetailsListIAP, productId);
            BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails)
                .build();
            billingClient.launchBillingFlow(activity, billingFlowParams);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private SkuDetails getSkuDetail(List<SkuDetails> skuDetailsListSUB, String productId) {
        try {
            for (SkuDetails skuDetails : skuDetailsListSUB) {
                if (skuDetails.getSku().equals(productId)) {
                    return skuDetails;
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    public String getPrice(String productId) {
        Log.i("superx", "getPrice: " + productId);
        String defaultPrice = "0$";
        if (billingClient == null || !billingClient.isReady()) {
            return defaultPrice;
        }
        for (SkuDetails skuDetails : skuDetailsListIAP) {
            if (skuDetails.getSku().equals(productId)) {
                return skuDetails.getPrice();
            }
        }

        return defaultPrice;
    }
}
