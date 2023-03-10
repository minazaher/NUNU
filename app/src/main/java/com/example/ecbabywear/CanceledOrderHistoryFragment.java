package com.example.ecbabywear;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecbabywear.Model.CartItem;
import com.example.ecbabywear.Model.Order;
import com.example.ecbabywear.UI.Cart;

import java.util.ArrayList;

public class CanceledOrderHistoryFragment extends Fragment {

    View v;
    private ArrayList<Order> orders;
    private ArrayList<CartItem> cartItems;


    public CanceledOrderHistoryFragment(){

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        orders = new ArrayList<>();
        cartItems = new ArrayList<CartItem>();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_order_history, container, false);
        RecyclerView ordersRecyclerView = (RecyclerView) v.findViewById(R.id.orders_recylerview);

        System.out.println("Orders From Fragment : " + orders.toString());
        OrdersAdapter ordersAdapter = new OrdersAdapter(ApplicationClass.Cancelled , getContext());
        ordersRecyclerView.setAdapter(ordersAdapter);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        return v;
    }
}