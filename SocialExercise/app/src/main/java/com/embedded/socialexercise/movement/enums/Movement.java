package com.embedded.socialexercise.movement.enums;

public enum Movement {
    NONE("No Exercise detected"), SITUP("Situp"), SQUAT("Squat"), TOE_TOUCH("Toe touch"), TRUNK_ROTATION("Trunk rotation");

    private final String name;

    private Movement(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
