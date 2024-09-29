package de.jmueller3.snake;

import java.util.Random;

public class Food {
    private Position position;
    private FoodType type;

    public Food(Position position, FoodType type) {
        this.position = position;
        this.type = type;
    }

    public Position getPosition() {
        return position;
    }

    public FoodType getType() {
        return type;
    }

    public enum FoodType {
        APPLE(0xFFFF0000), // Rot
        CHOCOLATE(0xFF8B4513), // Braun
        LICORICE(0xFF000000), // Schwarz
        CANDY(0xFFFFC0CB), // Rosa
        BANANA(0xFFFFFF00); // Gelb

        private final int color;

        FoodType(int color) {
            this.color = color;
        }

        public int getColor() {
            return color;
        }

        public static FoodType getRandomType() {
            Random random = new Random();
            int chance = random.nextInt(100);
            if (chance < 60) {
                return APPLE;
            } else if (chance < 70) {
                return CHOCOLATE;
            } else if (chance < 80) {
                return LICORICE;
            } else if (chance < 90) {
                return CANDY;
            } else {
                return BANANA;
            }
        }
    }
}
