package Controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class orders {
	private Timestamp ordertime;
	private Timestamp deliverytime;
	private String user_name;
	private String carrier;
	private Boolean isdelivered;
	private Double totalcost;
	private List<orderproducts> orderItems;
	private Integer orderid;

	public orders(Timestamp ordertime, Timestamp deliverytime, String user_name, String carrier, Boolean isdelivered,
			Double totalcost) {
		this.ordertime = ordertime;
		this.deliverytime = deliverytime;
		this.user_name = user_name;
		this.carrier = carrier;
		this.isdelivered = isdelivered;
		this.totalcost = totalcost;
		this.orderItems = new ArrayList<>();
	}

	public Timestamp getOrderTime() {
		return ordertime;
	}

	public Timestamp getDeliveryTime() {
		return deliverytime;
	}

	public String getUserName() {
		return user_name;
	}

	public String getCarrier() {
		return carrier;
	}

	public Boolean getDeliveryStatus() {
		return isdelivered;
	}

	public Double getCost() {
		return totalcost;
	}

	public List<orderproducts> getOrderItems() {
		return orderItems;
	}

	public void addOrderItem(orderproducts item) {
		orderItems.add(item);
	}

	public Double getQuantity(orderproducts item) {
		return item.getQuantity();
	}

	public String getId(orderproducts item) {
		return item.getName();
	}

	public Integer getOrderId() {
		return orderid;
	}

	public void setOrderId(Integer orderid) {
		this.orderid = orderid;
	}

	public void setOrderItems(List<orderproducts> orderItems) {
		this.orderItems = orderItems;
	}
}
