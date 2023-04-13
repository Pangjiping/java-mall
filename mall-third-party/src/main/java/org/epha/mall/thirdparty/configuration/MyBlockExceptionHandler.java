package org.epha.mall.thirdparty.configuration;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.epha.common.utils.R;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author pangjiping
 */
@Component
@Slf4j
public class MyBlockExceptionHandler implements BlockExceptionHandler {
    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, BlockException e) throws Exception {
        log.info("UrlBlockHandler.....................................");

        R r = null;

        if (e instanceof FlowException) {
            r = R.error(5001, "接口限流");
        } else if (e instanceof DegradeException) {
            r = R.error(5002, "接口降级");
        } else if (e instanceof ParamFlowException) {
            r = R.error(5003, "热点参数限流");
        } else if (e instanceof SystemBlockException) {
            r = R.error(5004, "触发系统保护规则");
        } else if (e instanceof AuthorityException) {
            r = R.error(5005, "授权规则不通过");
        }

        httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        httpServletResponse.getWriter().write(JSON.toJSONString(r));
    }
}
