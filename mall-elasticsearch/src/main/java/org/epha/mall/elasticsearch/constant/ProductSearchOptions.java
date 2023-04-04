package org.epha.mall.elasticsearch.constant;

/**
 * @author pangjiping
 */
public class ProductSearchOptions {

    public static final String INDEX = "jmall_product";

    public static final Integer PAGE_SIZE = 2;

    public static final Integer AGGREGATIONS_DEFAULT_SIZE = 10;

    public static final String SKU_TITLE = "skuTitle";

    public static final String CATALOG_ID = "catalogId";

    public static final String HAS_STOCK = "hasStock";

    public static final String BRAND_ID = "brandId";
    public static final String BRAND_NAME = "brandName";
    public static final String BRAND_IMG = "brandImg";

    public static final String SKU_PRICE_DELIMITER = "_";
    public static final String SKU_PRICE = "skuPrice";

    public static final String SORT_DELIMITER = "_";
    public static final String SORT_ASC = "asc";
    public static final String SORT_DESC = "desc";

    public static final String AGGREGATIONS_BRAND="brand_agg";
    public static final String AGGREGATIONS_BRAND_NAME = "brand_name_agg";
    public static final String AGGREGATIONS_BRAND_IMG = "brand_img_agg";

    public static final String AGGREGATIONS_CATALOG = "catalog_agg";

    public static class Attrs {
        public static final String ATTR = "attrs";

        public static final String ATTR_ID_VALUES_DELIMITER = "_";
        public static final String ATTR_VALUES_DELIMITER = ":";

        public static final String ATTR_ID = "attrs.attrId";
        public static final String ATTR_NAME = "attrs.attrName";
        public static final String ATTR_VALUE = "attrs.attrValue";

        public static final String AGGREGATIONS_ATTR = "attr_agg";
        public static final String AGGREGATIONS_ATTR_ID = "attr_id_agg";
        public static final String AGGREGATIONS_ATTR_NAME = "attr_name_agg";
        public static final String AGGREGATIONS_ATTR_VALUE = "attr_value_agg";

    }

}
