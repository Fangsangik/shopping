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
import java.util.stream.Collectors;

@Slf4j
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
        if (member.getUserId() == null) {
            throw new IllegalArgumentException("userId 값이 null이면 안됩니다.");
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
        itemFavoriteRepository.deleteByMemberUserIdAndItemId(userId, itemId);
    }

    @Transactional(readOnly = true)
    public List<ItemDto> findFavoriteItemsByUserId(String userId) {
        // userId를 기반으로 해당 사용자의 즐겨찾기 아이템을 조회
        List<ItemFavorite> favorites = itemFavoriteRepository.findByMemberUserId(userId);

        // Item 객체들을 ItemDto로 변환
        return favorites.stream()
                .map(favoriteItem -> converter.convertToItemDto(favoriteItem.getItem()))
                .collect(Collectors.toList());
    }
}
