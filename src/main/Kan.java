package main;

import main.annotation.AutoCombine;
import main.annotation.Ingredient;
import main.radar.Radar;
import main.service.UserService;
import main.start.RainedApplication;


@Ingredient
public class Kan {
    @AutoCombine
    UserService service;


    public static void main(String[] args) {
        RainedApplication.explosion(Kan.class);
        Kan app = Radar.container.getBean(Kan.class);
        app.service.show();

    }
}
