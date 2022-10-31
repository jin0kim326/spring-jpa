package jpabook.jpashop.service;

import jpabook.jpashop.controller.BookForm;
import lombok.Data;

@Data
public class UpdateItemDto {
    private Long id;
    private String name;
    private int price;
    private int stockQuantity;
    private String author;
    private String isbn;

    public UpdateItemDto(BookForm form) {
        id = form.getId();
        name = form.getName();
        price = form.getPrice();
        stockQuantity = form.getStockQuantity();
        author = form.getAuthor();
        isbn = form.getIsbn();
    }
}
