package com.hrl.mon.organ.constant;

public enum SysOrganTypeEnum {

    TYPE_COMPANY("1","公司");

    private String code;
    private String name;

    SysOrganTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
