package com.example.ordersearch.service;

import com.example.ordersearch.model.Order;
import com.example.ordersearch.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderSearchService {

    private final OrderRepository orderRepository;
    private final ElasticsearchRestTemplate elasticsearchTemplate;

    public List<Order> searchOrders(String searchTerm, Date fromDate, Date toDate, int page, int size) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // Multi-field search with fuzzy matching
        if (searchTerm != null && !searchTerm.isEmpty()) {
            BoolQueryBuilder shouldQuery = QueryBuilders.boolQuery()
                .should(QueryBuilders.matchQuery("customerName", searchTerm).fuzziness("AUTO"))
                .should(QueryBuilders.matchQuery("bookingCode", searchTerm).fuzziness("AUTO"))
                .should(QueryBuilders.matchQuery("customerEmail", searchTerm).fuzziness("AUTO"))
                .minimumShouldMatch(1);
            boolQuery.must(shouldQuery);
        }

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withPageable(PageRequest.of(page, size))
                .build();

        SearchHits<Order> searchHits = elasticsearchTemplate.search(searchQuery, Order.class);

        return searchHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }
} 