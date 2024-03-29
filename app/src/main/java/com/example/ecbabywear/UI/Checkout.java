package com.example.ecbabywear.UI;

import static com.example.ecbabywear.ApplicationClass.cart;
import static com.example.ecbabywear.ApplicationClass.cartPrice;
import static com.example.ecbabywear.ApplicationClass.firebaseAuth;
import static com.example.ecbabywear.ApplicationClass.navigateToActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecbabywear.ApplicationClass;
import com.example.ecbabywear.Model.Order;
import com.example.ecbabywear.Piece;
import com.example.ecbabywear.Repositories.OrderRepository;
import com.example.ecbabywear.Utilis.CartAdapter;
import com.example.ecbabywear.Utilis.OnDataChangedListener;
import com.example.ecbabywear.databinding.ActivityCartBinding;
import com.example.ecbabywear.databinding.ActivityCheckoutBinding;
import com.stripe.android.Stripe;
import com.stripe.android.cards.Bin;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;
import okhttp3.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.example.ecbabywear.R;

public class Checkout extends AppCompatActivity implements OnDataChangedListener {
    private static final String TAG = "CheckoutActivity";
    private static final String BACKEND_URL = "https://nunu-stripe.onrender.com";
    private OkHttpClient httpClient = new OkHttpClient();
    DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.DEFAULT);
    private Stripe stripe;
    private String paymentIntentClientSecret;
    private PaymentSheet paymentSheet;
    private Button payButton;
    private double totalPrice;
    OrderRepository orderRepository;
    ActivityCheckoutBinding Binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Binding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(Binding.getRoot());
        Binding.checkoutToolbar.textView11.setText("Checkout");
        orderRepository = new OrderRepository(firebaseAuth.getCurrentUser().getUid());
        stripe = new Stripe(getApplicationContext(),
                "pk_test_51NR7UYDNDAkufzaAXcN5keXddWnchfuIjmJ8tjpnFPDgeLDjLaf4OkaM4bD7dDM9pKbUNE9SITtw7r1LZBvLruoJ00rp2XpCYd");

        Binding.cityLocation.setText("Pick Your location");
        Binding.countryLocation.setText("");

        payButton = findViewById(R.id.pay_button);
        payButton.setOnClickListener(this::onPayClicked);
        payButton.setEnabled(false);
        InitializeCartRecView();
        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);
        Binding.btnGetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUserLocation();}
        });
        try {
            fetchPaymentIntent();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void updateUserLocation() {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions
                    (this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                            1);
        } else {
            Location location = locationManager.getLastKnownLocation(locationManager.getAllProviders().get(2));
            System.out.println("location: " + location);
            if (location != null) {
                Geocoder geocoder = new Geocoder(this,
                        Locale.getDefault());
                try {
                    List<Address> addresses =
                            geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (addresses.size() > 0) {
                        String city = addresses.get(0).getLocality();
                        String country = addresses.get(0).getCountryName();
                        String govern = addresses.get(0).getAdminArea();
                        String formattedLocation = String.format("%s, %s", city, govern);
                        System.out.println(addresses.toString());
                        System.out.println(formattedLocation);
                        System.out.println("Address line :" + addresses.get(0).getAddressLine(0) );
                        System.out.println("Address line1 :" + addresses.get(0).getAddressLine(1) );
                        System.out.println("Address line2:" + addresses.get(0).getAddressLine(2) );

                        Binding.cityLocation.setText(formattedLocation);
                        Binding.countryLocation.setText(country);
                    }
                } catch (IOException e) {
                }
            } else {
                System.out.println("Providers: " + locationManager.getAllProviders());
            }

        }

    }


        private void showAlert(String title, @Nullable String message) {
        runOnUiThread(() -> {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("Ok", null)
                    .create();
            dialog.show();
        });
    }
    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_LONG).show());
    }

    private String cartToJsonArray() throws JSONException {
        totalPrice = getIntent().getDoubleExtra("totalPrice",0);
        JSONArray itemsArray = new JSONArray();
        for (Piece item : cart) {
            JSONObject itemObject = new JSONObject();
            itemObject.put("id", item.getName());
            itemsArray.put(itemObject);
        }

        JSONObject shoppingCartContent = new JSONObject();

        shoppingCartContent.put("items", itemsArray);
        shoppingCartContent.put("totalPrice", totalPrice* 100);

        return shoppingCartContent.toString();
    }

    private void fetchPaymentIntent() throws JSONException {
        final String shoppingCartContent = cartToJsonArray();
        if (totalPrice == 0 || cart.isEmpty() ){
            Toast.makeText(this, "Cart cannot be empty!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Checkout.this, Cart.class);
            startActivity(intent);
        }
        showTotalPrice();
        final RequestBody requestBody = RequestBody.create(
                shoppingCartContent,
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(BACKEND_URL + "/create-payment-intent")
                .post(requestBody)
                .build();

        new OkHttpClient()
                .newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        showAlert("Failed to load data", "Error: " + e.toString());
                    }
                    @Override
                    public void onResponse(
                            @NonNull Call call,
                            @NonNull Response response
                    ) {
                        if (!response.isSuccessful()) {
                            showAlert(
                                    "Failed to load page",
                                    "Error: " + response.toString()
                            );
                        } else {
                            final JSONObject responseJson = parseResponse(response.body());
                            paymentIntentClientSecret = responseJson.optString("clientSecret");
                            runOnUiThread(() -> payButton.setEnabled(true));
                            Log.i(TAG, "Retrieved PaymentIntent");
                        }
                    }
                });
    }


    private JSONObject parseResponse(ResponseBody responseBody) {
        if (responseBody != null) {
            try {
                return new JSONObject(responseBody.string());
            } catch (IOException | JSONException e) {
                Log.e(TAG, "Error parsing response", e);
            }
        }
        return new JSONObject();
    }

    private void onPayClicked(View view) {
        PaymentSheet.Configuration configuration = new PaymentSheet.Configuration.Builder("NUNU")
                .merchantDisplayName("NUNU")
                .googlePay(new PaymentSheet.GooglePayConfiguration(
                        PaymentSheet.GooglePayConfiguration.Environment.Production,
                        "usd"
                ))
                .build();
        paymentSheet.presentWithPaymentIntent(paymentIntentClientSecret, configuration);
    }

    private void onPaymentSheetResult(final PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            showToast("Payment complete!");
            checkout("Completed");
        } else if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            Log.i(TAG, "Payment canceled!");
            checkout("Cancelled");
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            Throwable error = ((PaymentSheetResult.Failed) paymentSheetResult).getError();
            showAlert("Payment failed", error.getLocalizedMessage());
            checkout("Cancelled");
        }

    }

    private void checkout(String status){
        String date = dateFormat.format(new Date());
        Order order = new Order(firebaseAuth.getCurrentUser().getUid(), cart,date, String.valueOf(totalPrice), status);
        orderRepository.addOrderToDatabase(order);
        cart.clear();
        navigateToActivity(Checkout.this, HomePage.class);
    }

    private void InitializeCartRecView(){
        CartAdapter cartAdapter = new CartAdapter(this, cart);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        Binding.checkoutRecview.setLayoutManager(linearLayoutManager);
        Binding.checkoutRecview.setAdapter(cartAdapter);

    }

    @Override
    public void onDataChanged() {
        totalPrice = ApplicationClass.cartPrice;
        showTotalPrice();
    }


    private void showTotalPrice() {
        Binding.checkoutTotalPrice.setText(String.valueOf(cartPrice));

    }
}
