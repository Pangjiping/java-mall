package org.epha.mall.elasticsearch.service;

import org.epha.mall.elasticsearch.vo.SearchParam;
import org.epha.mall.elasticsearch.vo.SearchResult;

import java.io.IOException;

/**
 * @author pangjiping
 */
public interface MallSearchService {

    SearchResult search(SearchParam param) throws IOException;
}
