package com.forever.dadamda.dto.scrap;

import com.forever.dadamda.entity.scrap.Product;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GetProductResponse {

    // 공통 부분
    private Long scrapId;
    private String description;
    private String pageUrl;
    private String siteName;
    private String thumbnailUrl;
    private String title;

    // Product 부분
    private String price;

    public static GetProductResponse of(Product product) {
        return new GetProductResponseBuilder()
                .scrapId(product.getId())
                .description(product.getDescription())
                .pageUrl(product.getPageUrl())
                .siteName(product.getSiteName())
                .thumbnailUrl(product.getThumbnailUrl())
                .title(product.getTitle())
                .price(product.getPrice())
                .build();
    }

}