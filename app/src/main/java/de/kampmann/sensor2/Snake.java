package de.kampmann.sensor2;

import java.util.ArrayList;

public class Snake {
    private ArrayList<Position> body;
    private char direction;
    private char nextDirection;

    public Snake(int startX, int startY) {
        body = new ArrayList<>();
        body.add(new Position(startX, startY));
        direction = 'R';
        nextDirection = 'R'; // Initialisiere nextDirection mit der gleichen Richtung wie direction
    }

    public ArrayList<Position> getBody() {
        return body;
    }

    public char getDirection() {
        return direction;
    }

    public void setDirection(char newDirection) {
        if ((newDirection == 'U' && direction != 'D') ||
                (newDirection == 'D' && direction != 'U') ||
                (newDirection == 'L' && direction != 'R') ||
                (newDirection == 'R' && direction != 'L')) {
            nextDirection = newDirection;
        }
    }

    public void move() {
        direction = nextDirection;
        Position head = body.get(0);
        Position newHead = new Position(head.getX(), head.getY());
        switch (direction) {
            case 'U':
                newHead.setY(head.getY() - 1);
                break;
            case 'D':
                newHead.setY(head.getY() + 1);
                break;
            case 'L':
                newHead.setX(head.getX() - 1);
                break;
            case 'R':
                newHead.setX(head.getX() + 1);
                break;
        }
        body.add(0, newHead);
    }

    public void shrink() {
        if (body.size() > 1) {
            body.remove(body.size() - 1);
        }
    }

    public boolean checkCollision(int width, int height) {
        Position head = body.get(0);
        // Kollision mit den Wänden
        if (head.getX() < 0 || head.getX() >= width || head.getY() < 0 || head.getY() >= height) {
            return true;
        }
        // Kollision mit sich selbst
        for (int i = 1; i < body.size(); i++) {
            if (head.equals(body.get(i))) {
                return true;
            }
        }
        return false;
    }

    public boolean isHeadOnFood(Position food) {
        Position head = body.get(0);
        return head.equals(food);
    }
}
