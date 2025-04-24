package jp.co.metateam.library.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import io.micrometer.common.util.StringUtils;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotEmpty;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.AccountDto;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.BookMstDto;
import jp.co.metateam.library.repository.BookMstRepository;

@Service
public class BookMstService {

    private final BookMstRepository bookMstRepository;
    
    @Autowired
    public BookMstService(BookMstRepository bookMstRepository){
        this.bookMstRepository = bookMstRepository;
    }

    public List<BookMstDto> findAvailableWithStockCount() {
        List<BookMst> books = this.bookMstRepository.findLimitedBook();
        List<BookMstDto> bookMstDtoList = new ArrayList<BookMstDto>();

        // 書籍の在庫数を取得
        // FIXME: 現状は書籍ID毎にDBに問い合わせている。一度のSQLで完了させたい。
        for (int i = 0; i < books.size(); i++) {
            BookMst book = books.get(i);
            BookMstDto bookMstDto = new BookMstDto();
            bookMstDto.setId(book.getId());
            bookMstDto.setIsbn(book.getIsbn());
            bookMstDto.setTitle(book.getTitle());
            bookMstDtoList.add(bookMstDto);
        }

        return bookMstDtoList;
    }


    public boolean checkEntry(BookMstDto bookMstDto, Model model) {
        if(checkEntryTiTle(bookMstDto, model) || checkEntryIsbn(bookMstDto, model) == true) {
            return true;
        } else {
            return false;
        }
    }
    public boolean checkEntryTiTle(BookMstDto bookMstDto, Model model) {

        String bookTitle = bookMstDto.getTitle();
        ArrayList<String> errTitleFlg = new ArrayList<>();
        boolean checkTF = false;

        if(StringUtils.isEmpty(bookTitle) == true){
            errTitleFlg.add("書籍名の入力は必須です");
            model.addAttribute("title", errTitleFlg);
            checkTF = true;
            return false;
        }
        
        if (bookTitle.length() >256) {
            errTitleFlg.add("書籍名は255字以内で入力してください");
            model.addAttribute("title", errTitleFlg);
            checkTF = true;
        }
        if (checkTF == false) {
            return true;
        }
        return false;
    }

    public boolean checkEntryIsbn(BookMstDto bookMstDto, Model model) {

        String bookIsbn = bookMstDto.getIsbn();
        ArrayList<String> errIsbnFlg = new ArrayList<>();
        boolean checkTF = false;

        
        if(StringUtils.isEmpty(bookIsbn) == true){
            errIsbnFlg.add("ISBNの入力は必須です");
            model.addAttribute("isbn", errIsbnFlg);
            checkTF = true;
            return false;
        }
        
        if (bookIsbn.length() != 13) {
            errIsbnFlg.add("ISBNは13桁の数字で入力してください");
            model.addAttribute("isbn", errIsbnFlg);
            checkTF = true;
        }
        if (bookIsbn != "^[0-9]+$") {
            errIsbnFlg.add("ISBNは半角数字で入力してください");
            model.addAttribute("isbn", errIsbnFlg);
            checkTF = true;
        }
        if (!this.bookMstRepository.selectByIsbn(bookIsbn).isEmpty()) {
            errIsbnFlg.add("既に登録済みのISBNです");
            model.addAttribute("isbn", errIsbnFlg);
            checkTF = true;
        }
        if (checkTF == false) {
            return true;
        }
        return false;
    }

    @Transactional
    public void save(BookMstDto bookMstDto) {
        try {
            // BookMstDtoからBookMstへの変換
            BookMst book = new BookMst();

            book.setTitle(bookMstDto.getTitle());
            book.setIsbn(bookMstDto.getIsbn());

            // データベースへの保存
            this.bookMstRepository.save(book);
        } catch (Exception e) {
            throw e;
        }
    }
}



