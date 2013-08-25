package jp.co.isken.tax.domain.payment;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.avaje.ebean.validation.NotNull;

import play.db.ebean.Model;

import jp.co.isken.tax.domain.party.Party;

@Entity
public class PaymentAccount extends Model {
	private static final long serialVersionUID = 1L;
	@ManyToOne
	@JoinColumn(name = "client_id")
	private Party client;
	@NotNull
	private PaymentAccountType accountType;
	//永続化
	@Id
	private int id = 0;

	public PaymentAccount(Party client, PaymentAccountType accountType) {
		this.client = client;
		this.accountType = accountType;
	}
	public Party getClient() {
		return client;
	}
	public PaymentAccountType getAccountType() {
		return accountType;
	}
	public int getId() {
		return id;
	}
}
