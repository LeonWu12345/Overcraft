package com.jam8ee.overcraft.core.gameplay.hero;

import java.util.Locale;

public enum HeroId {
    SOLDIER76("soldier76"),
    REINHARDT("reinhardt");

    private final String id;

    HeroId(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static HeroId fromString(String raw) {
        if (raw == null || raw.isBlank()) return SOLDIER76;
        String norm = raw.trim().toLowerCase(Locale.ROOT);
        for (HeroId h : values()) {
            if (h.id.equals(norm)) return h;
        }
        return SOLDIER76;
    }
}
