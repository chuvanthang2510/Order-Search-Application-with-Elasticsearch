package com.example.ordersearch.service;

import com.example.ordersearch.model.Order;
import com.example.ordersearch.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSearchService {

    private final OrderRepository orderRepository;
    private final ElasticsearchRestTemplate elasticsearchTemplate;

    public List<Order> searchOrders(String searchTerm, Date fromDate, Date toDate, int page, int size) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        
        if (searchTerm != null && !searchTerm.isEmpty()) {
            BoolQueryBuilder shouldQuery = QueryBuilders.boolQuery();
            
            // Kiểm tra nếu searchTerm là email
            if (searchTerm.contains("@")) {
                // Tìm kiếm chính xác cho email
                shouldQuery.should(QueryBuilders.termQuery("customerEmail.raw", searchTerm.toLowerCase()));
            } else {
                // 1. Tìm kiếm chính xác cụm từ cho tên khách hàng
                shouldQuery.should(QueryBuilders.matchPhraseQuery("customerName", searchTerm));
                
                // 2. Tìm kiếm chính xác cho các trường khác
                shouldQuery.should(QueryBuilders.termQuery("code.raw", searchTerm));
                shouldQuery.should(QueryBuilders.termQuery("bookingCode.raw", searchTerm));
                shouldQuery.should(QueryBuilders.termQuery("phoneNumber.raw", searchTerm));
                
                // 3. Tìm kiếm một phần cho các trường khác
                shouldQuery.should(QueryBuilders.matchQuery("code", searchTerm));
                shouldQuery.should(QueryBuilders.matchQuery("bookingCode", searchTerm));
                shouldQuery.should(QueryBuilders.matchQuery("phoneNumber", searchTerm));
            }

            shouldQuery.minimumShouldMatch(1);
            boolQuery.must(shouldQuery);
        }

        // Filter theo ngày
        if (fromDate != null || toDate != null) {
            RangeQueryBuilder dateRangeQuery = QueryBuilders.rangeQuery("orderDate");
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

            if (fromDate != null) {
                dateRangeQuery.gte(sdf.format(fromDate));
            }
            if (toDate != null) {
                dateRangeQuery.lte(sdf.format(toDate));
            }

            boolQuery.must(dateRangeQuery);
        }

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withPageable(PageRequest.of(page, size))
                .withSort(SortBuilders.scoreSort().order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("orderDate").order(SortOrder.DESC))
                .build();

        log.debug("Search query: {}", searchQuery.getQuery().toString());
        
        SearchHits<Order> searchHits = elasticsearchTemplate.search(searchQuery, Order.class);
        log.debug("Total hits: {}", searchHits.getTotalHits());

        return searchHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }
} 