package com.learning.commons.module.dto;

import com.learning.commons.dto.BaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class TestDto
        extends BaseDto
        implements Serializable {

    private Long id;

    private String name;
}
