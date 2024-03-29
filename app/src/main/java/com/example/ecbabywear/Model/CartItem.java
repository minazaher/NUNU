package com.example.ecbabywear.Model;

import com.example.ecbabywear.Piece;

public class CartItem extends Piece {

    private int itemQuantity;

    public CartItem(){

    }

    public CartItem(Piece piece, int itemQuantity) {
        this.setName(piece.getName());
        this.setPrice(piece.getPrice());
        this.setItemQuantity(itemQuantity);
        this.setShortDescription(piece.getShortDescription());
        this.setLongDescription(piece.getLongDescription());
        this.setURL(piece.getURL());
        this.setStatus(piece.getStatus());
    }

    public int getItemQuantity() {
        return itemQuantity;
    }

    public void setItemQuantity(int itemQuantity) {
        this.itemQuantity = itemQuantity;
    }

    public void decreaseItemQuantity(){
        if (this.getItemQuantity() >0)
            this.itemQuantity -=1;
    }

    public void increaseItemQuantity(){
            this.itemQuantity +=1;
    }

    public Double getItemPrice(){
        double Price = Double.parseDouble(this.getPrice());
        return itemQuantity *  Price;
    }
}
