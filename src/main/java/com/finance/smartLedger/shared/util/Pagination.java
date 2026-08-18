package com.finance.smartLedger.shared.util;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pagination<T> {

  private List<T> content;
  private int pageNumber;
  private int pageSize;
  private long totalElements;
  private int totalPages;
  private boolean first;
  private boolean last;
  private boolean hasNext;
  private boolean hasPrevious;

  public static <T> Pagination<T> of(
      List<T> content, int pageNumber, int pageSize, long totalElements) {
    int totalPages = (int) Math.ceil((double) totalElements / pageSize);
    return new Pagination<>(
        content,
        pageNumber,
        pageSize,
        totalElements,
        totalPages,
        pageNumber == 0,
        pageNumber >= totalPages - 1,
        pageNumber < totalPages - 1,
        pageNumber > 0);
  }

  public static <T> Pagination<T> empty(List<T> content) {
    return new Pagination<>(content, 0, 0, 0, 0, true, true, false, false);
  }
}
