package com.atxzh.process.service.impl;

import com.atguigu.model.process.ProcessTemplate;
import com.atguigu.model.process.ProcessType;
import com.atxzh.process.mapper.OaProcessTypeMapper;
import com.atxzh.process.service.OaProcessTemplateService;
import com.atxzh.process.service.OaProcessTypeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 审批类型 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2023-08-30
 */
@Service
public class OaProcessTypeServiceImpl extends ServiceImpl<OaProcessTypeMapper, ProcessType> implements OaProcessTypeService {

    @Autowired
    private OaProcessTemplateService processTemplateService;
    //查询所有的审批分类和每个分类的审批模板
    @Override
    public List<ProcessType> findProcessType() {
        //1.查询所有的审批分类
        List<ProcessType> processTypeList = baseMapper.selectList(null);
        //2. 遍历所有的审批分类
        for (ProcessType processType :processTypeList) {
            //3.根据审批分类的id 查询对用审批模板
            //审批分类id
            Long typeId = processType.getId();
            //查询对用审批模板
            LambdaQueryWrapper<ProcessTemplate> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProcessTemplate::getProcessTypeId, typeId);
            List<ProcessTemplate> processTemplateList = processTemplateService.list(wrapper);
            //4.根据id查询的审批模板，分装到每个对象
            processType.setProcessTemplateList(processTemplateList);
        }
        return processTypeList;
    }
}
