package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import hello.itemservice.web.validation.form.ItemSaveForm;
import hello.itemservice.web.validation.form.ItemUpdateForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/validation/v4/items")
@RequiredArgsConstructor
public class ValidationItemControllerV4 {

    private final ItemRepository itemRepository;

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v4/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v4/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item()); // Model에 빈 값을 넘긴 이유는 검증에 실패했을 때
        return "validation/v4/addForm"; // <- 재사용 가능하기 때문 (데이터 넘어간 게 다시 보이도록) // ${item}
    }

    @PostMapping("/add")
    public String addItem(@Validated @ModelAttribute("item") ItemSaveForm form, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        // @ModelAttribute("item") 에 item 이름을 넣어준 부분을 주의하자.
        // 이것을 넣지 않으면 ItemSaveForm 의 경우 규칙에 의해 itemSaveForm 이라는 이름으로 MVC Model에 담기게 된다.
        // 이렇게 되면 뷰 템플릿에서 접근하는 th:object 이름도 함께 변경해주어야 한다.

        // 특정 필드가 아닌 복합 룰 검증
        // 오브젝트 오류(글로벌 오류)의 경우 @ScriptAssert 을 억지로 사용하는 것 보다는 오브젝트 오류 관련 부분만 직접 자바 코드로 작성하는 것을 권장한다.
        if (form.getPrice() != null && form.getQuantity() != null) {
            int resultPrice = form.getPrice() * form.getQuantity();
            if (resultPrice < 10000) {
                bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }

        // 검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.info("errors = {} ", bindingResult);
            return "validation/v4/addForm";
        }

        // 성공 로직
        Item item = new Item();
        item.setItemName(form.getItemName());
        item.setPrice(form.getPrice());
        item.setQuantity(form.getQuantity());
        // 폼 객체의 데이터를 기반으로 Item 객체를 생성한다.
        // 이렇게 폼 객체 처럼 중간에 다른 객체가 추가되면 변환하는 과정이 추가된다.
        // 원래는 생성자에서 처리해주는 것이 좋다.

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v4/items/{itemId}";
    }

    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v3/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @Validated @ModelAttribute("item") ItemUpdateForm form, BindingResult bindingResult) {
        // 특정 필드가 아닌 복합 룰 검증
        // 오브젝트 오류(글로벌 오류)의 경우 @ScriptAssert 을 억지로 사용하는 것 보다는 오브젝트 오류 관련 부분만 직접 자바 코드로 작성하는 것을 권장한다.
        if (form.getPrice() != null && form.getQuantity() != null) {
            int resultPrice = form.getPrice() * form.getQuantity();
            if (resultPrice < 10000) {
                bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }
        // 검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.info("errors = {} ", bindingResult);
            return "validation/v4/editForm";
        }

        Item itemParam = new Item();
        itemParam.setItemName(form.getItemName());
        itemParam.setPrice(form.getPrice());
        itemParam.setQuantity(form.getQuantity());
        // 수정의 경우도 등록과 같다. 그리고 폼 객체를 Item 객체로 변환하는 과정을 거친다.

        itemRepository.update(itemId, itemParam);
        return "redirect:/validation/v4/items/{itemId}";
    }

}