package com.ldy.usercenter.model.enums;

/**
 * 队伍状态枚举
 *
 * @author LDY
 */
public enum TeamStatusEnum {

    PUBLIC(0, "公开"),
    PRIVATE(1, "私有"),
    SECRET(2, "加密");

    private int value;

    private String text;

    TeamStatusEnum(int value, String text) {
        this.value = value;
        this.text = text;
    }

    public static TeamStatusEnum getEnumByValue(int status) {
        if (status == 0) {
            return PRIVATE;
        }
        if (status == 1) {
            return PRIVATE;
        }
        return SECRET;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
