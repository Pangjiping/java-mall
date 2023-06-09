package org.epha.common.constant;

public class ProductConstant {

    public enum AttrEnum {
        ATTR_TYPE_BASE(1, "基本属性"),
        ATTR_TYPE_SALE(0, "销售属性");

        private final int code;
        private final String message;

        AttrEnum(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return this.code;
        }

        public String getMessage() {
            return this.message;
        }
    }

    public enum StatusEnum{
        SPU_NEW(0, "商品新建"),
        SPU_UP(1, "商品上架"),
        SPU_DOWN(2,"商品下架");

        private final int code;
        private final String message;

        StatusEnum(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return this.code;
        }

        public String getMessage() {
            return this.message;
        }
    }
}
