package org.example.unishpere;

import javafx.scene.Scene;

import java.util.Stack;

public class Navigation {
    private static final Stack<Scene> navigationStack = new Stack<>();

    public static void pushScene(Scene scene) {
        navigationStack.push(scene);
    }

    public static Scene popScene() {
        if (!navigationStack.isEmpty()) {
            return navigationStack.pop();
        }
        return null;
    }
}

