package com.dbkj.meet.validator;

import com.dbkj.meet.vo.ChargeVo;
import com.jfinal.core.Controller;
import com.jfinal.i18n.I18n;
import com.jfinal.i18n.Res;
import com.jfinal.validate.Validator;

/**
 * Created by DELL on 2017/02/27.
 */
public class ChargeValidator extends Validator {
    @Override
    protected void validate(Controller controller) {
        Res resCn= I18n.use("zh_CN");
        validateRequired("chargeVo.id","idMsg","");
        validateRequired("chargeVo.chargeAmount","chargeAmountMsg",resCn.get("chargeamount.format.wrong"));
        validateRequired("chargeVo.actualCharge","actualChargeMsg",resCn.get("actualcharge.format.wrong"));
    }

    @Override
    protected void handleError(Controller controller) {
        controller.keepBean(ChargeVo.class);
        controller.render("charge.html");
    }
}
