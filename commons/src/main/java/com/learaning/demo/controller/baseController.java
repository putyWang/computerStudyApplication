package com.learaning.demo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.learaning.demo.dto.BaseDto;
import com.learaning.demo.entity.ApiResult;
import com.learaning.demo.entity.BaseEntity;
import com.learaning.demo.param.PageParam;
import com.learaning.demo.service.BaseService;
import com.learaning.demo.utils.CommonBeanUtil;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class baseController<T extends BaseEntity, D extends BaseDto>
        extends Controller{

    /**
     * 返回当前服务类
     * @return
     */
    public abstract BaseService<D, T> getService();

    /**
     * 获取当前Dto
     * @return
     */
    public abstract D getDto();

    /**
     * 获取当前实体类
     * @return
     */
    public abstract T getEntity();

    /**
     * 分页查询所有数据
     * @param page
     * @return
     */
    public ApiResult basePageList(PageParam page){

        IPage<T> iPage = getService().page(page, null);
        List<T> records = iPage.getRecords();
        List<D> list = new ArrayList<>();

        if (! CollectionUtils.isEmpty(records)) {
            list = (List<D>) CommonBeanUtil.copyList(records, this.getDto().getClass());
        }

        return ApiResult.ok((new Page<D>())
                .setPages(iPage.getPages())
                .setCurrent(iPage.getCurrent())
                .setRecords(list)
                .setTotal(iPage.getTotal())
                .setSize(iPage.getSize()));
    }

    /**
     * 展示所有数据
     * @return
     */
    @GetMapping ("/list")
    @ApiOperation (value = "展示所有数据", notes = "展示所有数据")
    public ApiResult list() {

        return ApiResult.ok(getService().list());
    }

    /**
     * 查看详情
     * @param id
     * @return
     */
    @GetMapping ("/{id}")
    @ApiOperation (value = "查看详情", notes = "查看详情")
    public ApiResult selectById (@PathVariable long id) {
        T result = (T) getService().getById(id);

        if (result == null) {
            return ApiResult.ok(null);
        }else {
            D dto = this.getDto();
            CommonBeanUtil.copyAndFormat(dto, result);
            dto = detailDtoHandler(dto);

            return ApiResult.ok(resultDtoHandler(dto));
        }

    }

    /**
     * "新增数据"
     * @param dto
     * @return
     */
    @PostMapping ("/")
    @ApiOperation (value = "新增数据", notes = "新增数据")
    public ApiResult insert(@RequestBody @Validated D dto) {

        if (dto == null) {

            return ApiResult.ok(null);
        }

        T entity = getEntity();
        CommonBeanUtil.copyAndFormat(entity, dto);
        getService().save(entity);

        return ApiResult.ok();
    }

    /**
     * 更新数据
     * @param dto
     * @return
     */
    @PutMapping("/")
    @ApiOperation( value = "更新数据", notes = "更新数据")
    public ApiResult update(@RequestBody @Validated D dto) {
        T entity = getEntity();
        CommonBeanUtil.copyAndFormat(entity, dto);
        getService().updateById(entity);

        return ApiResult.ok();
    }

    /**
     * 根据id删除数据
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    @ApiOperation(value = "根据id删除数据", notes = "根据id删除数据")
    public ApiResult deleteById(@PathVariable long id) {
        getService().removeById(id);

        return ApiResult.ok();
    }

    /**
     * 批量删除
     * @param ids
     * @return
     */
    @DeleteMapping("/delete/batch")
    @ApiOperation(value = "批量删除", notes = "批量删除")
    public ApiResult deleteBatch(@ApiParam(value = "id数组", required = true) List<Serializable> ids) {
        getService().removeByIds(ids);

        return ApiResult.ok();
    }

    /**
     * dto细节处理方法
     */
    public D detailDtoHandler(D dto) {
        return dto;
    }

    /**
     * dto结果处理方法
     * @param dto
     * @return
     */
    public D resultDtoHandler(D dto) {
        return dto;
    }
}
