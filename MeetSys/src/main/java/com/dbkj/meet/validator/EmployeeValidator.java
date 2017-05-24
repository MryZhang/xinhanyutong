package com.dbkj.meet.validator;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.model.Department;
import com.dbkj.meet.model.Employee;
import com.dbkj.meet.model.User;
import com.dbkj.meet.services.EmployeeService;
import com.dbkj.meet.services.inter.IEmployeeService;
import com.dbkj.meet.utils.ValidateUtil;
import com.jfinal.core.Controller;
import com.jfinal.i18n.I18n;
import com.jfinal.i18n.Res;
import com.jfinal.kit.StrKit;
import com.jfinal.validate.Validator;

import java.util.List;

/**
 * Created by MrQin on 2016/11/14.
 */
public class EmployeeValidator extends Validator {

    private IEmployeeService employeeService=new EmployeeService();

    protected void validate(Controller controller) {
        Res res = I18n.use("zh_CN");
        Employee employee=controller.getModel(Employee.class,"e");
        if(StrKit.isBlank(employee.getName())){
            addError("nameMsg",res.get("employee.name.not.empty"));
        }
        if(!StrKit.isBlank(employee.getEmail())&& !ValidateUtil.validateEmail(employee.getEmail())){
            addError("emailMsg",res.get("employee.email.format.wrong"));
        }
        if(employee.getDid()==0){
            addError("didMsg",res.get("employee.department.id.not.select"));
        }
    }

    protected void handleError(Controller controller) {
        controller.keepModel(Employee.class,"e");
        User user=controller.getSessionAttr(Constant.USER_KEY);
        int cid=user.getCid();
        List<Department> departmentList=employeeService.getDepartments(cid);
        controller.setAttr("dlist",departmentList);
        controller.render("info.html");
    }
}
