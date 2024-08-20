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
    public void addFavorites(Long memberId, Long itemId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("아이탬 정보를 찾을 수 없습니다."));

        ItemFavorite itemFavorite = ItemFavorite.builder()
                .member(member)
                .item(item)
                .build();
        itemFavoriteRepository.save(itemFavorite);
    }

    @Transactional
    public void removeFavorites(Long memberId, Long itemId) {
        itemFavoriteRepository.deleteByMemberIdAndItemId(memberId, itemId);
    }

    @Transactional(readOnly = true)
    public List<ItemDto> findFavoriteItemsByMemberId(Long memberId) {
        List<Item> favoriteItems = itemFavoriteRepository.findItemsByMemberId(memberId);
        return favoriteItems.stream()
                .map(converter::convertToItemDto)
                .collect(Collectors.toList());
    }
}
