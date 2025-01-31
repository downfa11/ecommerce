package ns.example.ecommerce.ecommerce.utils;

import ns.example.ecommerce.ecommerce.domain.enums.ErrorCode;
import ns.example.ecommerce.ecommerce.utils.exception.IssueException;

public enum RedisScriptCode {
    SUCCESS(1),
    DUPLICATED_COUPON_ISSUE(2),
    INVALID_COUPON_ISSUE_QUANTITY(3);

    RedisScriptCode(int code) {}

    public static RedisScriptCode find(String code){
        int codeValue = Integer.parseInt(code);

        if(codeValue==1) return SUCCESS;
        if(codeValue==2) return DUPLICATED_COUPON_ISSUE;
        if(codeValue==3) return INVALID_COUPON_ISSUE_QUANTITY;

        throw new IllegalArgumentException("Invalid redis script code.");
    }

    public static void checkRequestResult(RedisScriptCode code){
        if(code==INVALID_COUPON_ISSUE_QUANTITY)
            throw new IssueException(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY);
        if(code==DUPLICATED_COUPON_ISSUE)
            throw new IssueException(ErrorCode.DUPLICATED_COUPON_ISSUE);
    }
}
