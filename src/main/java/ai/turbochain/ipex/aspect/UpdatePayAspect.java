package ai.turbochain.ipex.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import ai.turbochain.ipex.entity.Advertise;
import ai.turbochain.ipex.entity.Order;
import ai.turbochain.ipex.entity.transform.AuthMember;
import ai.turbochain.ipex.service.AdvertiseService;
import ai.turbochain.ipex.service.LocaleMessageSourceService;
import ai.turbochain.ipex.service.OrderService;

import javax.servlet.http.HttpServletRequest;

import static ai.turbochain.ipex.constant.SysConstant.SESSION_MEMBER;

import java.util.List;

/**
 * 	修改支付信息前必须下架所有的广告，并且没有正在进行中的订单
 *
 * @author GS
 * @date 2018年03月28日
 */
@Aspect
@Component
@Slf4j
public class UpdatePayAspect {
    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private AdvertiseService advertiseService;
    @Autowired
    private OrderService orderService;

    private ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Pointcut("execution(public * ai.turbochain.ipex.controller.ApproveController.updateAli(..))"+
            "||execution(public * ai.turbochain.ipex.controller.ApproveController.updateBank(..))"+
            "||execution(public * ai.turbochain.ipex.controller.ApproveController.updateWechat(..))")
    public void updatePay() {
    }

    @Before("updatePay()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        log.info("❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤");
        check(joinPoint);
    }

    public void check(JoinPoint joinPoint) throws Exception {
        startTime.set(System.currentTimeMillis());
        // 接收到请求，记录请求内容
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        AuthMember authMember = (AuthMember) request.getSession().getAttribute(SESSION_MEMBER);
        List<Order> list1 = orderService.getAllOrdering(authMember.getId());
        if (list1.size()>0){
            throw new IllegalArgumentException(msService.getMessage("HAVE_ORDER_ING"));
        }
        List<Advertise> list = advertiseService.getAllPutOnAdvertis(authMember.getId());
        if (list.size()>0){
            throw new IllegalArgumentException(msService.getMessage("MUST_PUT_OFF_ALL_ADVERTISE"));
        }
    }

    @AfterReturning(pointcut = "updatePay()")
    public void doAfterReturning() throws Throwable {
        log.info("处理耗时：" + (System.currentTimeMillis() - startTime.get()) + "ms");
        log.info("↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑");
        startTime.remove();
    }
}
