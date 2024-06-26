package com.nhnacademy.shoppingmall.domain.order.service;

import com.nhnacademy.shoppingmall.domain.cart.dto.CartViewDTO;
import com.nhnacademy.shoppingmall.domain.cart.repository.UserCartRepository;
import com.nhnacademy.shoppingmall.domain.cart.service.CartService;
import com.nhnacademy.shoppingmall.domain.order.domain.Order;
import com.nhnacademy.shoppingmall.domain.order.domain.OrderDetail;
import com.nhnacademy.shoppingmall.domain.order.domain.OrderForm;
import com.nhnacademy.shoppingmall.domain.order.dto.OrderCompleteViewDTO;
import com.nhnacademy.shoppingmall.domain.order.repository.OrderDetailRepository;
import com.nhnacademy.shoppingmall.domain.order.repository.OrderRepository;
import com.nhnacademy.shoppingmall.domain.product.entity.Product;
import com.nhnacademy.shoppingmall.domain.product.repository.ProductRepository;
import com.nhnacademy.shoppingmall.domain.user.service.UserService;
import com.nhnacademy.shoppingmall.global.thread.channel.RequestChannel;
import com.nhnacademy.shoppingmall.global.thread.request.ChannelRequest;
import com.nhnacademy.shoppingmall.global.thread.request.impl.PointChannelRequest;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Builder
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository ;
    private final CartService cartService;
    private final ProductRepository productRepository;
    private final UserService userService;
    private final UserCartRepository userCartRepository ;

    /**
     * order 테이블
     * 1번 1번상품 -  1번 상품상세 상품 주문상세 1:1
     */
    @Override
    public OrderCompleteViewDTO saveOrder(Optional<String> userId, OrderForm orderForm, RequestChannel requestChannel) {
        if(userId.isEmpty()) {
            throw new IllegalArgumentException("로그인 ID가 존재하지 않습니다.");
        }

        CartViewDTO cartView = cartService.getCartView(userId, Optional.empty());
        List<CartViewDTO.ProductQuantity> products = cartView.getProducts();

        int totalPay = 0;
        OrderDetail orderDetail = null;

        //장바구니에 있는 각 상품 순회
        for (CartViewDTO.ProductQuantity product : products) {
            totalPay += product.getSelectQuantity() * product.getProduct().getPrice();

            if(product.getProduct().getProductField() < product.getSelectQuantity()) {
                throw new IllegalArgumentException("재고가 부족합니다!");
            }

            orderDetail = new OrderDetail(null, product.getSelectQuantity()
                    , product.getSelectQuantity() * product.getProduct().getPrice()
                    , orderForm.getAddress()
                    , orderForm.getAddressee()
                    , orderForm.getPhone()
                    , orderForm.getComment()
                    , orderForm.getRDate());

            int insertOrderDetailId = orderDetailRepository.save(orderDetail);
            orderRepository.save(new Order(product.getProduct().getId(), userId.get(), insertOrderDetailId));

            //재고 차감
            log.info("상품 재고 감소 {} -> {} ", product.getProduct().getName(), product.getProduct().getProductField() - product.getSelectQuantity());
            productRepository.decreaseProductQuantity(product.getProduct().getId(), product.getSelectQuantity());
        }

        //유저 포인트 감소
        log.info("user Id = {} 에서 totalPrice = {} 를 차감합니다", userId, totalPay);
        userService.decreasePoint(userId.get(), totalPay);

        //장바구니 삭제
        Integer cartId = userCartRepository.findCartIdByUserId(userId.get());
        if(cartId == null) throw new RuntimeException("장바구니 ID가 존재하지 않습니다.");
        cartService.deleteCart(cartId);

        //결제완료 포인트 추가
        ChannelRequest request = new PointChannelRequest(userService.getUser(userId.get()), totalPay);
        try {
            //포인트 충전 쓰레드 시작
            requestChannel.addRequest(request);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //현재 주문 내역 응답을 위해 저장
        return new OrderCompleteViewDTO(orderDetail,products, totalPay);
    }

    @Override
    public OrderCompleteViewDTO getOrder(Optional<String> userId) {
        if(userId.isEmpty()) {
            throw new IllegalArgumentException("로그인 ID가 존재하지 않습니다.");
        }

        OrderDetail orderDetail = null;

        List<CartViewDTO.ProductQuantity> products = new ArrayList<>();
        int totalPrice = 0;
        List<Order> orders = orderRepository.findByUserId(userId.get());
        for (Order order : orders) {
            orderDetail = orderDetailRepository.findById(order.getOrderDetailId());
            Optional<Product> product = productRepository.findById(order.getProductId());
            if(product.isEmpty()) throw new RuntimeException("상품 정보를 조회할 수 없습니다");
            products.add(new CartViewDTO.ProductQuantity(product.get(),orderDetail.getQuantity()) );
            totalPrice += orderDetail.getTotalPrice();
        }

        return new OrderCompleteViewDTO(orderDetail, products, totalPrice);
    }

}
