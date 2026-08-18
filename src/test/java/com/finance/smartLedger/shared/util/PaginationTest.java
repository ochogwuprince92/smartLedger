package com.finance.smartLedger.shared.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Pagination Tests")
class PaginationTest {

  @Test
  @DisplayName("Should create pagination with calculated values")
  void shouldCreatePaginationWithCalculatedValues() {
    List<String> content = List.of("item1", "item2", "item3");
    int pageNumber = 0;
    int pageSize = 10;
    long totalElements = 3;

    Pagination<String> pagination = Pagination.of(content, pageNumber, pageSize, totalElements);

    assertEquals(content, pagination.getContent());
    assertEquals(pageNumber, pagination.getPageNumber());
    assertEquals(pageSize, pagination.getPageSize());
    assertEquals(totalElements, pagination.getTotalElements());
    assertEquals(1, pagination.getTotalPages());
    assertTrue(pagination.isFirst());
    assertTrue(pagination.isLast());
    assertFalse(pagination.isHasNext());
    assertFalse(pagination.isHasPrevious());
  }

  @Test
  @DisplayName("Should calculate total pages correctly")
  void shouldCalculateTotalPagesCorrectly() {
    List<String> content = List.of("item1", "item2", "item3");
    int pageSize = 2;
    long totalElements = 5;

    Pagination<String> pagination = Pagination.of(content, 0, pageSize, totalElements);

    assertEquals(3, pagination.getTotalPages());
  }

  @Test
  @DisplayName("Should identify first page correctly")
  void shouldIdentifyFirstPageCorrectly() {
    List<String> content = List.of("item1");

    Pagination<String> firstPage = Pagination.of(content, 0, 10, 20);
    assertTrue(firstPage.isFirst());
    assertFalse(firstPage.isHasPrevious());

    Pagination<String> secondPage = Pagination.of(content, 1, 10, 20);
    assertFalse(secondPage.isFirst());
    assertTrue(secondPage.isHasPrevious());
  }

  @Test
  @DisplayName("Should identify last page correctly")
  void shouldIdentifyLastPageCorrectly() {
    List<String> content = List.of("item1");

    Pagination<String> lastPage = Pagination.of(content, 1, 10, 20);
    assertTrue(lastPage.isLast());
    assertFalse(lastPage.isHasNext());

    Pagination<String> firstPage = Pagination.of(content, 0, 10, 20);
    assertFalse(firstPage.isLast());
    assertTrue(firstPage.isHasNext());
  }

  @Test
  @DisplayName("Should handle empty pagination")
  void shouldHandleEmptyPagination() {
    List<String> content = List.of();

    Pagination<String> pagination = Pagination.empty(content);

    assertEquals(content, pagination.getContent());
    assertEquals(0, pagination.getPageNumber());
    assertEquals(0, pagination.getPageSize());
    assertEquals(0, pagination.getTotalElements());
    assertEquals(0, pagination.getTotalPages());
    assertTrue(pagination.isFirst());
    assertTrue(pagination.isLast());
    assertFalse(pagination.isHasNext());
    assertFalse(pagination.isHasPrevious());
  }

  @Test
  @DisplayName("Should handle single page")
  void shouldHandleSinglePage() {
    List<String> content = List.of("item1", "item2");

    Pagination<String> pagination = Pagination.of(content, 0, 10, 2);

    assertEquals(1, pagination.getTotalPages());
    assertTrue(pagination.isFirst());
    assertTrue(pagination.isLast());
    assertFalse(pagination.isHasNext());
    assertFalse(pagination.isHasPrevious());
  }

  @Test
  @DisplayName("Should handle multiple pages")
  void shouldHandleMultiplePages() {
    List<String> content = List.of("item1");

    Pagination<String> page1 = Pagination.of(content, 0, 1, 3);
    Pagination<String> page2 = Pagination.of(content, 1, 1, 3);
    Pagination<String> page3 = Pagination.of(content, 2, 1, 3);

    assertTrue(page1.isFirst());
    assertFalse(page1.isLast());
    assertTrue(page1.isHasNext());
    assertFalse(page1.isHasPrevious());

    assertFalse(page2.isFirst());
    assertFalse(page2.isLast());
    assertTrue(page2.isHasNext());
    assertTrue(page2.isHasPrevious());

    assertFalse(page3.isFirst());
    assertTrue(page3.isLast());
    assertFalse(page3.isHasNext());
    assertTrue(page3.isHasPrevious());
  }

  @Test
  @DisplayName("Should create pagination using constructor")
  void shouldCreatePaginationUsingConstructor() {
    List<String> content = List.of("item1", "item2");

    Pagination<String> pagination =
        new Pagination<>(content, 1, 10, 20, 2, false, true, false, true);

    assertEquals(content, pagination.getContent());
    assertEquals(1, pagination.getPageNumber());
    assertEquals(10, pagination.getPageSize());
    assertEquals(20, pagination.getTotalElements());
    assertEquals(2, pagination.getTotalPages());
    assertFalse(pagination.isFirst());
    assertTrue(pagination.isLast());
    assertFalse(pagination.isHasNext());
    assertTrue(pagination.isHasPrevious());
  }

  @Test
  @DisplayName("Should set and get content")
  void shouldSetAndGetContent() {
    Pagination<String> pagination = new Pagination<>();
    List<String> content = List.of("item1", "item2");

    pagination.setContent(content);
    assertEquals(content, pagination.getContent());
  }

  @Test
  @DisplayName("Should set and get page number")
  void shouldSetAndGetPageNumber() {
    Pagination<String> pagination = new Pagination<>();

    pagination.setPageNumber(5);
    assertEquals(5, pagination.getPageNumber());
  }

  @Test
  @DisplayName("Should set and get page size")
  void shouldSetAndGetPageSize() {
    Pagination<String> pagination = new Pagination<>();

    pagination.setPageSize(25);
    assertEquals(25, pagination.getPageSize());
  }

  @Test
  @DisplayName("Should set and get total elements")
  void shouldSetAndGetTotalElements() {
    Pagination<String> pagination = new Pagination<>();

    pagination.setTotalElements(100);
    assertEquals(100, pagination.getTotalElements());
  }

  @Test
  @DisplayName("Should set and get total pages")
  void shouldSetAndGetTotalPages() {
    Pagination<String> pagination = new Pagination<>();

    pagination.setTotalPages(10);
    assertEquals(10, pagination.getTotalPages());
  }

  @Test
  @DisplayName("Should set and get first flag")
  void shouldSetAndGetFirstFlag() {
    Pagination<String> pagination = new Pagination<>();

    pagination.setFirst(true);
    assertTrue(pagination.isFirst());

    pagination.setFirst(false);
    assertFalse(pagination.isFirst());
  }

  @Test
  @DisplayName("Should set and get last flag")
  void shouldSetAndGetLastFlag() {
    Pagination<String> pagination = new Pagination<>();

    pagination.setLast(true);
    assertTrue(pagination.isLast());

    pagination.setLast(false);
    assertFalse(pagination.isLast());
  }

  @Test
  @DisplayName("Should set and get has next flag")
  void shouldSetAndGetHasNextFlag() {
    Pagination<String> pagination = new Pagination<>();

    pagination.setHasNext(true);
    assertTrue(pagination.isHasNext());

    pagination.setHasNext(false);
    assertFalse(pagination.isHasNext());
  }

  @Test
  @DisplayName("Should set and get has previous flag")
  void shouldSetAndGetHasPreviousFlag() {
    Pagination<String> pagination = new Pagination<>();

    pagination.setHasPrevious(true);
    assertTrue(pagination.isHasPrevious());

    pagination.setHasPrevious(false);
    assertFalse(pagination.isHasPrevious());
  }

  @Test
  @DisplayName("Should handle zero total elements")
  void shouldHandleZeroTotalElements() {
    List<String> content = List.of();

    Pagination<String> pagination = Pagination.of(content, 0, 10, 0);

    assertEquals(0, pagination.getTotalElements());
    assertEquals(0, pagination.getTotalPages());
    assertTrue(pagination.isFirst());
    assertTrue(pagination.isLast());
  }

  @Test
  @DisplayName("Should handle large page numbers")
  void shouldHandleLargePageNumbers() {
    List<String> content = List.of("item1");

    Pagination<String> pagination = Pagination.of(content, 100, 10, 1000);

    assertEquals(100, pagination.getPageNumber());
    assertEquals(100, pagination.getTotalPages());
    assertFalse(pagination.isFirst());
    assertTrue(pagination.isLast());
    assertFalse(pagination.isHasNext());
    assertTrue(pagination.isHasPrevious());
  }

  @Test
  @DisplayName("Should handle generic types")
  void shouldHandleGenericTypes() {
    class TestItem {
      String name;

      TestItem(String name) {
        this.name = name;
      }
    }

    List<TestItem> content = List.of(new TestItem("item1"));
    Pagination<TestItem> pagination = Pagination.of(content, 0, 10, 1);

    assertEquals(1, pagination.getContent().size());
    assertEquals("item1", pagination.getContent().get(0).name);
  }
}
