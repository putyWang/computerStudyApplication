package com.learning.commons.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 分页排序类
 */
@Data
public class SortPageParam
        extends PageParam{

    /**
     * 排序字段
     */
    @ApiModelProperty(value = "排序字段", notes = "排序字段")
    private List<String> sorts;

    /**
     * 升序还是降序
     */
    @ApiModelProperty(value = "升序还是降序", notes = "升序还是降序")
    private List<String> ACSs;
}
