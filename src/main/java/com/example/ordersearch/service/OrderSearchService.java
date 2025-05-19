package com.example.ordersearch.service;

import com.example.ordersearch.model.Order;
import com.example.ordersearch.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSearchService {

    private final OrderRepository orderRepository;
    private final ElasticsearchRestTemplate elasticsearchTemplate;
    private final RestHighLevelClient restHighLevelClient;

    private String removeVietnameseDiacritics(String str) {
        str = str.replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a");
        str = str.replaceAll("[èéẹẻẽêềếệểễ]", "e");
        str = str.replaceAll("[ìíịỉĩ]", "i");
        str = str.replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o");
        str = str.replaceAll("[ùúụủũưừứựửữ]", "u");
        str = str.replaceAll("[ỳýỵỷỹ]", "y");
        str = str.replaceAll("[đ]", "d");
        str = str.replaceAll("[ÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴ]", "A");
        str = str.replaceAll("[ÈÉẸẺẼÊỀẾỆỂỄ]", "E");
        str = str.replaceAll("[ÌÍỊỈĨ]", "I");
        str = str.replaceAll("[ÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠ]", "O");
        str = str.replaceAll("[ÙÚỤỦŨƯỪỨỰỬỮ]", "U");
        str = str.replaceAll("[ỲÝỴỶỸ]", "Y");
        str = str.replaceAll("[Đ]", "D");
        return str;
    }

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

    public void generateFakeData(int numberOfRecords) {
        log.info("Starting to generate {} fake records", numberOfRecords);
        
        // Danh sách các giá trị mẫu
        String[] firstNames = {"Nguyễn", "Trần", "Lê", "Phạm", "Hoàng", "Huỳnh", "Phan", "Vũ", "Võ", "Đặng"};
        String[] middleNames = {"Văn", "Thị", "Hoàng", "Đức", "Minh", "Hữu", "Công", "Đình", "Xuân", "Hồng"};
        String[] lastNames = {"An", "Bình", "Cường", "Dũng", "Em", "Phúc", "Giang", "Hùng", "Khang", "Linh"};
        String[] domains = {"gmail.com", "yahoo.com", "hotmail.com", "outlook.com", "company.com"};
        String[] statuses = {"PENDING", "CONFIRMED", "CANCELLED", "COMPLETED", "PROCESSING"};
        
        // Số lượng bản ghi mỗi lần bulk
        int batchSize = 10000;
        int totalBatches = (int) Math.ceil((double) numberOfRecords / batchSize);
        
        AtomicInteger counter = new AtomicInteger(0);
        Random random = new Random();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        
        for (int batch = 0; batch < totalBatches; batch++) {
            BulkRequest bulkRequest = new BulkRequest();
            int currentBatchSize = Math.min(batchSize, numberOfRecords - (batch * batchSize));
            
            for (int i = 0; i < currentBatchSize; i++) {
                // Tạo dữ liệu ngẫu nhiên
                String firstName = firstNames[random.nextInt(firstNames.length)];
                String middleName = middleNames[random.nextInt(middleNames.length)];
                String lastName = lastNames[random.nextInt(lastNames.length)];
                String customerName = String.format("%s %s %s", firstName, middleName, lastName);
                
                String username = (firstName + lastName).toLowerCase().replaceAll("\\s+", "");
                username = removeVietnameseDiacritics(username);
                String domain = domains[random.nextInt(domains.length)];
                String customerEmail = String.format("%s@%s", username, domain);
                
                String code = String.format("ORD%08d", counter.incrementAndGet());
                String bookingCode = String.format("BK%d%06d", 
                    Calendar.getInstance().get(Calendar.YEAR),
                    random.nextInt(1000000));
                String phoneNumber = String.format("09%d", 10000000 + random.nextInt(90000000));
                
                // Tạo ngày ngẫu nhiên trong 2 năm gần đây
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.YEAR, -2);
                long startTime = cal.getTimeInMillis();
                long endTime = System.currentTimeMillis();
                long randomTime = startTime + (long)(random.nextDouble() * (endTime - startTime));
                Date orderDate = new Date(randomTime);
                
                double totalAmount = 100000 + random.nextDouble() * 9000000;
                String status = statuses[random.nextInt(statuses.length)];
                
                // Tạo JSON document
                Map<String, Object> document = new HashMap<>();
                document.put("id", UUID.randomUUID().toString());
                document.put("code", code);
                document.put("bookingCode", bookingCode);
                document.put("phoneNumber", phoneNumber);
                document.put("customerName", customerName);
                document.put("customerEmail", customerEmail);
                document.put("orderDate", sdf.format(orderDate));
                document.put("totalAmount", totalAmount);
                document.put("status", status);
                
                // Thêm vào bulk request
                IndexRequest indexRequest = new IndexRequest("orders_v2")
                    .source(document, XContentType.JSON);
                bulkRequest.add(indexRequest);
            }
            
            try {
                BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
                if (bulkResponse.hasFailures()) {
                    log.error("Bulk request failed: {}", bulkResponse.buildFailureMessage());
                } else {
                    log.info("Successfully indexed batch {}/{} ({} records)", 
                        batch + 1, totalBatches, currentBatchSize);
                }
            } catch (Exception e) {
                log.error("Error during bulk indexing: ", e);
            }
        }
        
        log.info("Finished generating {} fake records", numberOfRecords);
    }
} 