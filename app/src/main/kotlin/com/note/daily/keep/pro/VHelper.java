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

public class VHelper {
    private BillingClient client;
    private BillingClientStateListener clientStateListener;
    private VCallback callback;
    private static VHelper instance;
    private List<SkuDetails> skuDetailsListIAP = new ArrayList<>();

    public static final String NOTE_PRO_TEST = "android.test.purchased";
    public static final String NOTE_PRO_1 = BuildConfig.DEBUG ? NOTE_PRO_TEST : "note_pro_1";
    public static final String NOTE_PRO_2 = BuildConfig.DEBUG ? NOTE_PRO_TEST : "note_pro_2";
    public static final String NOTE_PRO_3 = BuildConfig.DEBUG ? NOTE_PRO_TEST : "note_pro_3";
    public static final String NOTE_PRO_4 = BuildConfig.DEBUG ? NOTE_PRO_TEST : "note_pro_4";

    public VHelper setCallback(VCallback callback) {
        this.callback = callback;
        return this;
    }

    public void purchaseSuccess() {
        if (EventBus.getDefault().hasSubscriberForEvent(VEvent.class)) {
            EventBus.getDefault().post(new VEvent());
        }
        VPrefs.get().updateLocalPurchasedState(true);
        if (callback != null) {
            callback.purchaseSuccessfully();
        }
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

    void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (purchase.isAcknowledged()) {
                return;
            }

            AcknowledgePurchaseParams acknowledgePurchaseParams =
                AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.getPurchaseToken())
                    .build();
            client.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {
                purchaseSuccess();
            });
        }
    }

    public static synchronized VHelper getInstance() {
        if (instance == null || instance.client == null) {
            instance = new VHelper();
        }
        return instance;
    }

    private VHelper() {
    }

    public void initBilling(Context context) {
        initBilling(context, null);
    }

    public void initBilling(final Context context, BillingClientStateListener billingClientStateListener) {
        client = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build();
        this.clientStateListener = billingClientStateListener;
        client.startConnection(new BillingClientStateListener() {
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
            if (clientStateListener != null) {
                clientStateListener.onBillingSetupFinished(billingResult);
            }
            return;
        }
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        if (BuildConfig.DEBUG) {
            params.setSkusList(Arrays.asList(NOTE_PRO_TEST, NOTE_PRO_1, NOTE_PRO_2, NOTE_PRO_3, NOTE_PRO_4));
        } else {
            params.setSkusList(Arrays.asList(NOTE_PRO_1, NOTE_PRO_2, NOTE_PRO_3, NOTE_PRO_4));
        }
        params.setType(BillingClient.SkuType.INAPP);
        client.querySkuDetailsAsync(params.build(),
            (billingResult12, skuDetailsList) -> {
                VHelper.this.skuDetailsListIAP = skuDetailsList;
                if (clientStateListener != null) {
                    clientStateListener.onBillingSetupFinished(billingResult);
                }
            });
    }

    public void consume(Context context, String productId) {
        Purchase purchase = getPurchase(productId);
        if (purchase != null) {
            ConsumeParams consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();
            client.consumeAsync(consumeParams, (billingResult, s) -> {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    fetchPurchased(null);
                }
            });
        }
    }

    private Purchase getPurchase(String productId) {
        try {
            Purchase.PurchasesResult purchasesResult = client.queryPurchases(BillingClient.SkuType.INAPP);
            for (Purchase purchase :
                Objects.requireNonNull(purchasesResult.getPurchasesList())) {
                if (purchase.getSkus().contains(productId)) return purchase;
            }
        } catch (Exception exception) {
        }
        return null;
    }

    public boolean isRemovedAds(Context context) {
        if (client == null) {
            initBilling(context);
        }
        return isPurchased() || isSubscribed() || isRemovedAdsLocalState(context);
    }

    public boolean isRemovedAdsLocalState(Context context) {
        VPrefs pPrefs = VPrefs.get();
        return pPrefs.isLocalSubscribedState() || pPrefs.isLocalPurchasedState();
    }

    private boolean isPurchased() {
        try {
            Purchase.PurchasesResult purchasesResult = client.queryPurchases(BillingClient.SkuType.INAPP);
            for (Purchase purchase : Objects.requireNonNull(purchasesResult.getPurchasesList())) {
                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                    VPrefs.get().updateLocalPurchasedState(true);
                    return true;
                }
            }
            VPrefs.get().updateLocalPurchasedState(false);
        } catch (Exception ignored) {
        }
        return false;
    }

    public void fetchPurchased(VPCallback callback) {
        client.queryPurchasesAsync(BillingClient.SkuType.INAPP, (billingResult, list) -> {
            try {
                boolean purchased = false;
                for (Purchase purchase : Objects.requireNonNull(list)) {
                    if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                        purchased = true;
                    }
                }
                if (!purchased) {
                    client.queryPurchasesAsync(BillingClient.SkuType.SUBS, (billingResult1, list1) -> {
                        boolean subscribed = false;
                        for (Purchase purchase : Objects.requireNonNull(list1)) {
                            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                                subscribed = true;
                            }
                        }
                        VPrefs.get().updateLocalPurchasedState(subscribed);
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
                VPrefs.get().updateLocalPurchasedState(purchased);
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
            Purchase.PurchasesResult purchasesResult = client.queryPurchases(BillingClient.SkuType.SUBS);
            for (Purchase purchase : Objects.requireNonNull(purchasesResult.getPurchasesList())) {
                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                    VPrefs.get().updateLocalSubscribedState(true);
                    return true;
                }
            }
            VPrefs.get().updateLocalSubscribedState(false);
        } catch (Exception exception) {
        }
        return false;
    }

    public void purchase(Activity activity, String productId) {
        try {
            if (client == null) {
                initBilling(activity);
            }
            SkuDetails skuDetails = getSkuDetail(skuDetailsListIAP, productId);
            BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails)
                .build();
            client.launchBillingFlow(activity, billingFlowParams);
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
        if (client == null || !client.isReady()) {
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
