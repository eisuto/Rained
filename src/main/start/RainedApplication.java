package main.start;


import main.annotation.AutoCombine;
import main.annotation.Ingredient;
import main.radar.Radar;

/**
 * @author eisuto
 */
public class RainedApplication {

    private static Radar radar;

    public RainedApplication() {

    }

    public static void explosion(Class<?> appClass) {
        radar = new Radar();
        try {
            String packageName = appClass.getPackage().getName();
            radar.getAnnotationClasses(packageName, Ingredient.class);
            radar.automaticInjection(packageName, AutoCombine.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Please check the started package name path.");
        }
    }
}
