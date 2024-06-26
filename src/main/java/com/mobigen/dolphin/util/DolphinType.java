package com.mobigen.dolphin.util;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public enum DolphinType {
    BOOL("BOOL"),
    TEXT("TEXT"),
    INT("INT"),
    LONG("LONG"),
    REAL("REAL"),
    UNDEFINED("UNDEFINED");;

    private final String value;

    DolphinType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static DolphinType fromValue(String value) {
        for (DolphinType type : DolphinType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return UNDEFINED;
    }
}
