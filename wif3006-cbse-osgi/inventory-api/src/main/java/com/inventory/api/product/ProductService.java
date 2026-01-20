package com.inventory.api.product;

import java.util.List;
import java.util.Scanner; // Don't forget to import Scanner!

public interface ProductService {

    void addProduct(Product product);

    List<Product> getAllProducts();

    void showMenu(Scanner scanner);
}