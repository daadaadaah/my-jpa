package jpabook.jpashop;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JpashopApplication {

	public static void main(String[] args) {
		SpringApplication.run(JpashopApplication.class, args);
	}

	/**
	 * 강제 지연 로딩을 true로 설정해 준 이유
	 * - 이걸 하지 않으면, GET http://localhost:8080/api/simple-orders 요청시, 다음과 같은 응답 객체를 얻는다.
	 * [{
	 * 		"id": 4,
	 * 		"member": null,
	 * 		"orderItems": null,
	 * 		"delivery": null,
	 * 		"orderDate": "2023-09-30T14:15:39.993332",
	 * 		"status": "ORDER",
	 * 		"totalPrice": 50000
	 * },{
	 * 		"id": 11,
	 * 		"member": null,
	 * 		"orderItems": null,
	 * 		"delivery": null,
	 * 		"orderDate": "2023-09-30T14:15:40.026326",
	 * 		"status": "ORDER",
	 * 		"totalPrice": 220000
	 * }]
	 * - 왜냐하면, Order 클래스에서 member, orderItems, deliveery 가 지연로딩이기 때문이다.
	 * - 따라서, 다음과 같은 응답 구조를 얻기 위해서 강제 지연 로딩을 true로 설정한다.
	 * [{
	 * 		"id": 4,
	 * 		"member":{
	 * 				"id": 1,
	 * 				"name": "userA",
	 * 				"address":{
	 * 						"city": "서울",
	 * 						"street": "1",
	 * 						"zipcode": "1111"
	 * 				}
	 * 		},
	 * 		"orderItems":[
	 * 			{"id": 6, "item":{"id": 2, "name": "JPA1 BOOK", "price": 10000,…},
	 * 			{"id": 7, "item":{"id": 3, "name": "JPA2 BOOK", "price": 20000,…}
	 * 		],
	 * 		"delivery":{
	 * 				"id": 5,
	 * 				"address":{
	 * 						"city": "서울",
	 * 						"street": "1",
	 * 						"zipcode": "1111"
	 * 				},
	 * 		"status": null
	 * 		},
	 * 		"orderDate": "2023-09-30T14:18:39.544324",
	 * 		"status": "ORDER",
	 * 		"totalPrice": 50000
	 * }, {
	 * 		"id": 11,
	 * 	     //...
	 * }]
	 */
	@Bean
	Hibernate5Module hibernate5Module() {
		Hibernate5Module hibernate5Module = new Hibernate5Module();
		//강제 지연 로딩 설정
		hibernate5Module.configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING, true);
		return hibernate5Module;
	}
}
