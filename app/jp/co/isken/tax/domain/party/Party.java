package jp.co.isken.tax.domain.party;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.Model;

import com.avaje.ebean.validation.NotNull;

//@MappedSuperclass
@Entity
public class Party extends Model {
	private static final long serialVersionUID = 1L;
	@NotNull
	private String name;
	@NotNull
	private PartyType type;
	//永続化
	@Id
	private int id;

	public Party(String name, PartyType type) {
		this.name = name;
		this.type = type;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}

	public PartyType getType() {
		return type;
	}

}
