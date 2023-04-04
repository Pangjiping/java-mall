package org.epha.mall.elasticsearch.vo;

import lombok.Data;

import java.util.List;

/**
 * 封装页面所有可能传递过来的查询条件
 * <p>
 * catalog3Id=225&keyword=小米&sort=saleCount_asc ...
 *
 * @author pangjiping
 */
@Data
public class SearchParam {
    /**
     * 全文检索匹配关键字
     */
    private String keyword;
    /**
     * 三级分类的id
     */
    private String catalog3Id;
    /**
     * 排序条件 sort=saleCount_asc
     * 前面是按照什么排序，后面是升序(asc)或者降序(desc)
     */
    private String sort;

    /**
     * 好多的过滤条件
     * hasStock（是否有货）、skuPrice区间、brandId、catalog3Id、attrs
     * hasStock=0/1
     * skuPrice=100_500/_500/500_
     * brandId=1&brandId=2
     * attrs按照属性进行筛选，同一个属性多个筛选用:分隔
     */
    private Integer hasStock = 0;
    private String skuPrice;
    private List<Long> brandId;
    private List<String> attrs;
    /**
     * 页码
     */
    private Integer pageNum = 1;

    private String _queryString;


}
