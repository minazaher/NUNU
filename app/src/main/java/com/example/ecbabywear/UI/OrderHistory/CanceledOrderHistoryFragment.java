package com.example.ecbabywear.UI.OrderHistory;

import static com.example.ecbabywear.ApplicationClass.firebaseAuth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecbabywear.ApplicationClass;
import com.example.ecbabywear.Model.CartItem;
import com.example.ecbabywear.Model.Order;
import com.example.ecbabywear.R;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class CanceledOrderHistoryFragment extends Fragment {
    private OrderRepository orderRepository;
    private ArrayList<Order> cancelledOrders;

    public CanceledOrderHistoryFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        orderRepository = new OrderRepository();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_history, container, false);
        FirebaseUser firebaseUser = firebaseAuth.getInstance().getCurrentUser();
        orderRepository.getCancelledOrdersByUser(firebaseUser, orders -> {
            RecyclerView ordersRecyclerView = (RecyclerView) view.findViewById(R.id.orders_recylerview);
            cancelledOrders = (ArrayList<Order>) orders;
            OrdersAdapter adapter = new OrdersAdapter(cancelledOrders, this.getContext());
            ordersRecyclerView.setAdapter(adapter);
            ordersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        });
        return view;
    }
}