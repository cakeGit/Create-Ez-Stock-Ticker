package com.aztech.ez_stock_ticker.foundation;

import com.aztech.ez_stock_ticker.ClientConfig;
import net.minecraft.client.gui.screens.Screen;

import java.util.function.Supplier;

public enum StackSnapping {
    NONE(ClientConfig.CONFIG.defaultSnappingExpression),
    SHIFT(ClientConfig.CONFIG.shiftSnappingExpression),
    CONTROL(ClientConfig.CONFIG.ctrlSnappingExpression),
    SHIFT_CONTROL(ClientConfig.CONFIG.ctrlShiftSnappingExpression);

    private final Supplier<String> expressionSupplier;

    StackSnapping(Supplier<String> expressionSupplier) {
        this.expressionSupplier = expressionSupplier;
    }

    public String getExpression() {
        return expressionSupplier.get();
    }

    public static StackSnapping get() {
        if (Screen.hasShiftDown() && Screen.hasControlDown()) {
            return SHIFT_CONTROL;
        } else if (Screen.hasShiftDown()) {
            return SHIFT;
        } else if (Screen.hasControlDown()) {
            return CONTROL;
        } else {
            return NONE;
        }
    }

    public static int getSnappingIncrementSafe(int stackSize) {
        return getSnappingIncrementSafe(stackSize, get().getExpression());
    }

    public static int getSnappingIncrementSafe(int stackSize, String expression) {
        try {
            return getSnappingIncrement(stackSize, expression);
        } catch (IllegalArgumentException e) {
            return 1;
        }
    }

    /**
     * Get the snapping increment for a given stack size and expression.
     *
     * @param stackSize  The size of the stack.
     * @param expression The expression to evaluate, can be in the form of a normal number "16" or
     *                   a simple expression starting with "stack", like "stack*5", "stack*0.25", "stack/4".
     *                   "stack" expressions must be in the form "stack(*|/)(number)", and will ceil round and clamp >0
     */
    private static int getSnappingIncrement(int stackSize, String expression) throws IllegalArgumentException {
        if (expression == null) {
            throw new IllegalArgumentException("Snapping expression cannot be null");
        }

        if (expression.startsWith("stack")) {
            return getSnappingIncrementForStackExpression(stackSize, expression);
        }
        // Otherwise, just parse the number
        int increment = Integer.parseInt(expression);
        if (increment <= 0) {
            throw new IllegalArgumentException("Snapping increment must be greater than 0");
        }
        return increment;
    }

    private static int getSnappingIncrementForStackExpression(int stackSize, String expression) throws IllegalArgumentException {
        if (stackSize <= 0) stackSize = 1; //Clamp up for factory logistics

        if (expression.equals("stack")) {
            return stackSize;
        }

        if (expression.length() < 7) {
            throw new IllegalArgumentException("Invalid snapping expression, must include an operator and then a number");
        }

        char operator = expression.charAt(5);

        double operand = Double.parseDouble(expression.substring(6));
        if (operand <= 0 || Double.isNaN(operand) || Double.isInfinite(operand)) {
            throw new IllegalArgumentException("Factor in snapping expression must be a number greater than 0");
        }

        int snapping;
        if (operator == '*') {
            snapping = (int) Math.ceil(stackSize * operand);
        } else if (operator == '/') {
            snapping = (int) Math.ceil(stackSize / operand);
        } else {
            throw new IllegalArgumentException("Invalid snapping expression, must be in the form of 'stack*number' or 'stack/number'");
        }

        return Math.max(1, snapping);
    }

    public static boolean isValidExpression(String value) {
        try {
            getSnappingIncrement(64, value);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

}
