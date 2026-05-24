package com.thriftby.service;

import com.thriftby.entity.*;
import com.thriftby.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<Wishlist> findByUser(Long userId) {
        return wishlistRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public boolean isInWishlist(Long userId, Long itemId) {
        return wishlistRepository.existsByUserIdAndItemId(userId, itemId);
    }

    public void toggle(Long userId, Long itemId) {
        if (wishlistRepository.existsByUserIdAndItemId(userId, itemId)) {
            wishlistRepository.deleteByUserIdAndItemId(userId, itemId);
        } else {
            User user = userRepository.findById(userId).orElseThrow();
            Item item = itemRepository.findById(itemId).orElseThrow();
            Wishlist entry = Wishlist.builder().user(user).item(item).build();
            wishlistRepository.save(entry);
        }
    }
}
