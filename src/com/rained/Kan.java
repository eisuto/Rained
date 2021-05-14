package com.rained;

import com.rained.annotation.AutoCombine;
import com.rained.annotation.Ingredient;
import com.rained.radar.Radar;
import com.rained.service.UserService;
import com.rained.start.RainedApplication;


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
