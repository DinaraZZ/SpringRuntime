drop table if exists product_characteristics;
drop table if exists characteristics;
drop table if exists product;
drop table if exists category;
drop table if exists "user";
drop table if exists "order";
drop table if exists review;
drop table if exists order_product;


create table category
(
    id   serial8,
    name varchar not null,
    primary key (id)
);

create table product
(
    id          serial8,
    category_id int8,
    name        varchar not null,
    price       int4    not null,
    visibility  boolean not null,
    primary key (id),
    foreign key (category_id) references category (id)
);

create table characteristic
(
    id          serial8,
    category_id int8    not null,
    name        varchar not null,
    primary key (id),
    foreign key (category_id) references category (id)
);

create table product_characteristic
(
    id                serial8,
    product_id        int8    not null,
    characteristic_id int8    not null,
    description       varchar not null,
    primary key (id),
    foreign key (product_id) references product (id),
    foreign key (characteristic_id) references characteristic (id)
);


create table "user"
(
    id           serial8,
    role         int2      not null,
    login        varchar   not null unique,
    password     varchar   not null,
    first_name   varchar   not null,
    last_name    varchar   not null,
    sign_up_date timestamp not null,
    primary key (id)
);

create table "order"
(
    id         serial8,
    user_id    int8      not null,
    status     int2      not null,
    order_date timestamp not null,
    primary key (id),
    foreign key (user_id) references "user" (id)
);

create table order_product
(
    id         serial8,
    order_id   int8 not null,
    product_id int8 not null,
    amount     int2 not null,
    primary key (id),
    foreign key (order_id) references "order" (id),
    foreign key (product_id) references product (id)
);

create table review
(
    id          serial8,
    user_id     int8      not null,
    product_id  int8      not null,
    published   boolean   not null,
    rating      int2 check (rating between 1 and 5),
    commentary  varchar   not null,
    review_date timestamp not null,
    primary key (id),
    foreign key (user_id) references "user" (id),
    foreign key (product_id) references product (id)
);

create table cart
(
    id         serial8,
    user_id    int8 not null,
    product_id int8 not null,
    amount     int2 not null,
    primary key (id),
    foreign key (user_id) references "user" (id),
    foreign key (product_id) references product (id)
);

insert into category(name)
values ('smartphones'),
       ('furniture'),
       ('headphones');

insert into product(category_id, name, price, visibility)
values (2, 'iPhone 13 Pro Max', 560000, true),
       (2, 'Samsung S24', 670000, true),
       (2, 'iPhone XR', 390000, true),
       (3, 'Table', 76550, true),
       (3, 'Sofa Grey', 567000, true),
       (4, 'AirPods 3', 84990, true),
       (4, 'Realme Buds T100', 13890, true),
       (4, 'Marshall Major IV', 33000, true),
       (4, 'TWS F9-5', 1195, true),
       (4, 'JBL Tune 510BT', 13000, true);