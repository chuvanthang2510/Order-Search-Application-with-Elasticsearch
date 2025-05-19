package com.example.ordersearch.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.util.Date;

@Document(indexName = "orders_v2")
@Setting(
    settingPath = "vietnamese-analyzer.json",
    shards = 5,
    replicas = 1,
    refreshInterval = "30s"
)
@Data
public class Order {
    @Id
    private String id;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "partial_match_analyzer"),
            otherFields = {
                @InnerField(suffix = "raw", type = FieldType.Keyword)
            }
    )
    private String code;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "partial_match_analyzer"),
            otherFields = {
                @InnerField(suffix = "raw", type = FieldType.Keyword)
            }
    )
    private String bookingCode;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "partial_match_analyzer"),
            otherFields = {
                @InnerField(suffix = "raw", type = FieldType.Keyword)
            }
    )
    private String phoneNumber;

    @MultiField(
            mainField = @Field(type = FieldType.Text, 
                    analyzer = "vietnamese_analyzer",
                    searchAnalyzer = "vietnamese_search_analyzer"),
            otherFields = {
                @InnerField(suffix = "raw", type = FieldType.Keyword)
            }
    )
    private String customerName;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "partial_match_analyzer"),
            otherFields = {
                @InnerField(suffix = "raw", type = FieldType.Keyword)
            }
    )
    private String customerEmail;

    @Field(type = FieldType.Date, format = DateFormat.custom, pattern = "dd/MM/yyyy HH:mm:ss")
    private Date orderDate;

    @Field(type = FieldType.Double)
    private double totalAmount;

    @Field(type = FieldType.Keyword)
    private String status;
}