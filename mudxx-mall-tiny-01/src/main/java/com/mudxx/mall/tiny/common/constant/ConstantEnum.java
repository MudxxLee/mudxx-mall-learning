package com.mudxx.mall.tiny.common.constant;

/**
 * @author laiw
 * @date 2023/2/10 11:22
 */
public class ConstantEnum {

    public enum StatusEnum {
        Disabled(0, "禁用"),
        Enable(1, "启用");

        private int code;
        private String desc;

        private StatusEnum(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        public int getCode() {
            return code;
        }
        public String getDesc() {
            return desc;
        }
    }

}
