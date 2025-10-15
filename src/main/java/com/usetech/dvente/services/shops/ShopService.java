package com.usetech.dvente.services.shops;

import com.usetech.dvente.entities.users.Shop;
import com.usetech.dvente.entities.users.ShopGallery;
import com.usetech.dvente.entities.users.ShopUrlHistory;
import com.usetech.dvente.repositories.shops.ShopGalleryRepository;
import com.usetech.dvente.repositories.shops.ShopRepository;
import com.usetech.dvente.repositories.shops.ShopUrlHistoryRepository;
import com.usetech.dvente.responses.shops.ShopResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopRepository shopRepository;
    private final ShopGalleryRepository shopGalleryRepository;
    private final ShopUrlHistoryRepository shopUrlHistoryRepository;

    @Value("${app.url}")
    private String apiUrl;

    @Transactional(readOnly = true)
    public ShopResponse getShopById(UUID shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        // Charger les galleries
        List<ShopGallery> galleries = shopGalleryRepository.findByShop_IdOrderByCreatedAtDesc(shopId);

        // Charger le dernier changement d'URL
        ShopUrlHistory lastUrlChange = shopUrlHistoryRepository
                .findFirstByShop_IdOrderByChangedAtDesc(shopId)
                .orElse(null);

        return ShopResponse.fromEntity(shop, apiUrl, galleries, lastUrlChange);
    }

    @Transactional(readOnly = true)
    public List<ShopResponse> getAllShops() {
        List<Shop> shops = shopRepository.findAll();

        return shops.stream()
                .map(shop -> {
                    List<ShopGallery> galleries = shopGalleryRepository.findByShopOrderByCreatedAtDesc(shop);
                    ShopUrlHistory lastUrlChange = shopUrlHistoryRepository
                            .findFirstByShopOrderByChangedAtDesc(shop)
                            .orElse(null);
                    return ShopResponse.fromEntity(shop, apiUrl, galleries, lastUrlChange);
                })
                .toList();
    }
}