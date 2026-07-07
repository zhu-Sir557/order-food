package com.restaurant.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.List;
import lombok.Data;

/**
 * Paginated result wrapper.
 *
 * @param <T> the type of records in the page
 */
@Data
public class PageResult<T> {

    /** Data records */
    private List<T> records;

    /** Total record count */
    private long total;

    /** Current page number */
    private long current;

    /** Page size */
    private long size;

    /**
     * Default constructor.
     */
    public PageResult() {
    }

    /**
     * Full constructor.
     *
     * @param records data records
     * @param total   total count
     * @param current current page
     * @param size    page size
     */
    public PageResult(List<T> records, long total, long current, long size) {
        this.records = records;
        this.total = total;
        this.current = current;
        this.size = size;
    }

    /**
     * Create a PageResult from a MyBatis-Plus IPage.
     *
     * @param page the IPage result from MyBatis-Plus
     * @param <T>  the record type
     * @return a PageResult wrapping the page data
     */
    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(
                page.getRecords(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize());
    }
}
