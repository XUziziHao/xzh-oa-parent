package com.atxzh.auth.activiti;

import org.activiti.api.task.model.impl.TaskImpl;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MyTaskListener implements TaskListener {

    @Override
    public void notify(DelegateTask task) {
        if(task.getName().equals("经理审批")){
            //这里指定任务负责人
            task.setAssignee("zhangsan");
        } else if(task.getName().equals("人事审批")){
            //这里指定任务负责人
            task.setAssignee("lisi");
        }
    }
}
