-- Run this in Supabase: SQL Editor -> New query -> Run.
-- Recreates the full schema from scratch, including seed data. Do not
-- hand-edit tables in Supabase - always change this script and re-run it.

drop table if exists notifications;
drop table if exists order_items;
drop table if exists orders;
drop table if exists inventory;

create table inventory (
    product_id varchar(50) primary key,
    name varchar(150) not null,
    stock integer not null check (stock >= 0)
);

create table orders (
    order_id bigserial primary key,
    status varchar(20) not null check (status in ('CONFIRMED', 'REJECTED', 'CANCELLED')),
    reason varchar(255),
    created_at timestamptz not null default now()
);

create table order_items (
    order_item_id bigserial primary key,
    order_id bigint not null references orders (order_id) on delete cascade,
    product_id varchar(50) not null,
    quantity integer not null check (quantity > 0)
);

create table notifications (
    notification_id bigserial primary key,
    message varchar(255) not null,
    created_at timestamptz not null default now()
);

insert into inventory (product_id, name, stock)
values
    ('P100', 'Wireless Mouse', 25),
    ('P200', 'Mechanical Keyboard', 10),
    ('P300', 'USB-C Hub', 0)
on conflict (product_id) do update
set name = excluded.name,
    stock = excluded.stock;
