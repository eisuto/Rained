package main.service;

import main.annotation.Ingredient;

@Ingredient
public class UserService {
    public void show(){
        System.out.println("Hello,Auto!");
    }
}
