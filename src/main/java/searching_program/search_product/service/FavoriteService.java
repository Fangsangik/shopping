package searching_program.search_product.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searching_program.search_product.domain.Item;
import searching_program.search_product.domain.ItemFavorite;
import searching_program.search_product.domain.Member;
import searching_program.search_product.dto.DtoEntityConverter;
import searching_program.search_product.dto.ItemDto;
import searching_program.search_product.repository.ItemFavoriteRepository;
import searching_program.search_product.repository.ItemRepository;
import searching_program.search_product.repository.MemberRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    private final ItemFavoriteRepository itemFavoriteRepository;
    private final DtoEntityConverter converter;

    @Transactional
    public void addFavorites(String userId, Long itemId) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        // 중복 확인
        Optional<ItemFavorite> existingFavorite = itemFavoriteRepository.findByMemberUserIdAndItemId(userId, itemId);
        if (existingFavorite.isPresent()) {
            throw new IllegalArgumentException("이미 즐겨찾기에 추가된 아이템입니다.");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("아이템 정보를 찾을 수 없습니다."));

        ItemFavorite itemFavorite = ItemFavorite.builder()
                .member(member)
                .item(item)
                .build();
        itemFavoriteRepository.save(itemFavorite);
    }

    @Transactional
    public void removeFavorites(String userId, Long itemId) {
        ItemFavorite itemFavorite = itemFavoriteRepository.findByMemberUserIdAndItemId(userId, itemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 아이템은 즐겨찾기 목록에 존재하지 않습니다. userId: " + userId + ", itemId: " + itemId));

        itemFavoriteRepository.delete(itemFavorite);
    }

    @Transactional(readOnly = true)
    public List<ItemDto> findFavoriteItemsByUserId(String userId) {
        List<ItemFavorite> favorites = itemFavoriteRepository.findByMemberUserId(userId);
        return favorites.stream()
                .map(favoriteItem ->  converter.convertToItemDto(favoriteItem.getItem()))
                .collect(Collectors.toList());
    }
}

