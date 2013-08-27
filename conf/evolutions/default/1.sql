# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table commercial_account (
  id                        integer auto_increment not null,
  client_id                 integer,
  goods_id                  integer,
  constraint pk_commercial_account primary key (id))
;

create table commercial_entry (
  id                        integer auto_increment not null,
  amount                    double not null,
  unit                      integer not null,
  transaction_id            integer,
  account_id                integer,
  constraint ck_commercial_entry_unit check (unit in (0,1,2)),
  constraint pk_commercial_entry primary key (id))
;

create table commercial_transaction (
  id                        integer auto_increment not null,
  when_charged              datetime not null,
  deal_type                 integer not null,
  tax_type                  integer not null,
  is_taxed_deal             tinyint(1) default 0 not null,
  compute_type              integer not null,
  item                      integer,
  when_booked               datetime not null,
  contract_id               integer,
  constraint ck_commercial_transaction_deal_type check (deal_type in (0,1)),
  constraint ck_commercial_transaction_tax_type check (tax_type in (0,1,2)),
  constraint ck_commercial_transaction_compute_type check (compute_type in (0,1,2)),
  constraint ck_commercial_transaction_item check (item in (0,1)),
  constraint pk_commercial_transaction primary key (id))
;

create table contract (
  id                        integer auto_increment not null,
  type                      integer not null,
  when_agreement            datetime not null,
  firstParty_id             integer,
  range_id                  integer,
  taxCondition_id           integer,
  payCondtion_id            integer,
  constraint ck_contract_type check (type in (0,1)),
  constraint pk_contract primary key (id))
;

create table TaxDateRange (
  id                        integer auto_increment not null,
  begin_date                datetime not null,
  end_date                  datetime not null,
  constraint pk_TaxDateRange primary key (id))
;

create table date_range (
  id                        integer auto_increment not null,
  begin_date                datetime not null,
  end_date                  datetime not null,
  constraint pk_date_range primary key (id))
;

create table goods (
  id                        integer auto_increment not null,
  name                      varchar(255) not null,
  unitPrice_id              integer,
  item                      integer not null,
  constraint ck_goods_item check (item in (0,1)),
  constraint pk_goods primary key (id))
;

create table party (
  id                        integer auto_increment not null,
  name                      varchar(255) not null,
  type                      integer not null,
  constraint ck_party_type check (type in (0,1,2)),
  constraint pk_party primary key (id))
;

create table payment_account (
  id                        integer auto_increment not null,
  client_id                 integer,
  account_type              integer not null,
  constraint ck_payment_account_account_type check (account_type in (0,1)),
  constraint pk_payment_account primary key (id))
;

create table payment_condition (
  id                        integer auto_increment not null,
  rounding_rule             integer not null,
  constraint ck_payment_condition_rounding_rule check (rounding_rule in (0,1,2)),
  constraint pk_payment_condition primary key (id))
;

create table payment_entry (
  id                        integer auto_increment not null,
  price                     double not null,
  transaction_id            integer,
  account_id                integer,
  constraint pk_payment_entry primary key (id))
;

create table payment_transaction (
  id                        integer auto_increment not null,
  when_charged              datetime not null,
  when_booked               datetime not null,
  basisTransaction_id       integer,
  basisEntry_id             integer,
  client_id                 integer,
  constraint pk_payment_transaction primary key (id))
;

create table tax_condition (
  id                        integer auto_increment not null,
  rounding_rule             integer not null,
  tax_unit_rule             integer not null,
  constraint ck_tax_condition_rounding_rule check (rounding_rule in (0,1,2)),
  constraint ck_tax_condition_tax_unit_rule check (tax_unit_rule in (0,1,2)),
  constraint pk_tax_condition primary key (id))
;

create table tax_rate (
  id                        integer auto_increment not null,
  rate                      double not null,
  unit                      integer,
  range_id                  integer,
  item                      integer,
  constraint ck_tax_rate_unit check (unit in (0)),
  constraint ck_tax_rate_item check (item in (0,1,2)),
  constraint pk_tax_rate primary key (id))
;

create table unit_price (
  id                        integer auto_increment not null,
  unit                      integer not null,
  price                     double not null,
  constraint ck_unit_price_unit check (unit in (0,1,2)),
  constraint pk_unit_price primary key (id))
;

alter table commercial_account add constraint fk_commercial_account_client_1 foreign key (client_id) references party (id) on delete restrict on update restrict;
create index ix_commercial_account_client_1 on commercial_account (client_id);
alter table commercial_account add constraint fk_commercial_account_goods_2 foreign key (goods_id) references goods (id) on delete restrict on update restrict;
create index ix_commercial_account_goods_2 on commercial_account (goods_id);
alter table commercial_entry add constraint fk_commercial_entry_transaction_3 foreign key (transaction_id) references commercial_transaction (id) on delete restrict on update restrict;
create index ix_commercial_entry_transaction_3 on commercial_entry (transaction_id);
alter table commercial_entry add constraint fk_commercial_entry_account_4 foreign key (account_id) references commercial_account (id) on delete restrict on update restrict;
create index ix_commercial_entry_account_4 on commercial_entry (account_id);
alter table commercial_transaction add constraint fk_commercial_transaction_contract_5 foreign key (contract_id) references contract (id) on delete restrict on update restrict;
create index ix_commercial_transaction_contract_5 on commercial_transaction (contract_id);
alter table contract add constraint fk_contract_firstParty_6 foreign key (firstParty_id) references party (id) on delete restrict on update restrict;
create index ix_contract_firstParty_6 on contract (firstParty_id);
alter table contract add constraint fk_contract_range_7 foreign key (range_id) references TaxDateRange (id) on delete restrict on update restrict;
create index ix_contract_range_7 on contract (range_id);
alter table contract add constraint fk_contract_taxCondition_8 foreign key (taxCondition_id) references tax_condition (id) on delete restrict on update restrict;
create index ix_contract_taxCondition_8 on contract (taxCondition_id);
alter table contract add constraint fk_contract_payCondition_9 foreign key (payCondtion_id) references payment_condition (id) on delete restrict on update restrict;
create index ix_contract_payCondition_9 on contract (payCondtion_id);
alter table goods add constraint fk_goods_unitPrice_10 foreign key (unitPrice_id) references unit_price (id) on delete restrict on update restrict;
create index ix_goods_unitPrice_10 on goods (unitPrice_id);
alter table payment_account add constraint fk_payment_account_client_11 foreign key (client_id) references party (id) on delete restrict on update restrict;
create index ix_payment_account_client_11 on payment_account (client_id);
alter table payment_entry add constraint fk_payment_entry_transaction_12 foreign key (transaction_id) references payment_transaction (id) on delete restrict on update restrict;
create index ix_payment_entry_transaction_12 on payment_entry (transaction_id);
alter table payment_entry add constraint fk_payment_entry_account_13 foreign key (account_id) references payment_account (id) on delete restrict on update restrict;
create index ix_payment_entry_account_13 on payment_entry (account_id);
alter table payment_transaction add constraint fk_payment_transaction_basisTransaction_14 foreign key (basisTransaction_id) references commercial_transaction (id) on delete restrict on update restrict;
create index ix_payment_transaction_basisTransaction_14 on payment_transaction (basisTransaction_id);
alter table payment_transaction add constraint fk_payment_transaction_basisEntry_15 foreign key (basisEntry_id) references commercial_entry (id) on delete restrict on update restrict;
create index ix_payment_transaction_basisEntry_15 on payment_transaction (basisEntry_id);
alter table payment_transaction add constraint fk_payment_transaction_client_16 foreign key (client_id) references party (id) on delete restrict on update restrict;
create index ix_payment_transaction_client_16 on payment_transaction (client_id);
alter table tax_rate add constraint fk_tax_rate_range_17 foreign key (range_id) references date_range (id) on delete restrict on update restrict;
create index ix_tax_rate_range_17 on tax_rate (range_id);



# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table commercial_account;

drop table commercial_entry;

drop table commercial_transaction;

drop table contract;

drop table TaxDateRange;

drop table date_range;

drop table goods;

drop table party;

drop table payment_account;

drop table payment_condition;

drop table payment_entry;

drop table payment_transaction;

drop table tax_condition;

drop table tax_rate;

drop table unit_price;

SET FOREIGN_KEY_CHECKS=1;

