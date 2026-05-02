package com.darieldon.ivcurve.model;

public record IVPoint(
        double voltage, double current, double power) {

    public static IVPoint of(double v, double i) {
        return new IVPoint(v, i, v*i);
    }
}
