package org.example.unishpere;

public class a {


    void price(){
        new priceCalculator().returnPrice();
    }
}

class priceCalculator{
    int tax;
    int price;

    int returnPrice(){
        return price * tax;
    }

}
