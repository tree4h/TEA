package jp.co.isken.tax.view;

public class Receipt {
	
	public int id;			//商流移動番号
	public int gid;			//商品番号
	public String name;		//商品名
	public double amount;	//量
	public String unit;		//単位
	public double price;	//商品代金

	public Receipt(int id, int gid, String name, double amount, String unit, double price) {
		this.id = id;
		this.gid = gid;
		this.name = name;
		this.amount = amount;
		this.unit = unit;
		this.price = price;
	}

}
