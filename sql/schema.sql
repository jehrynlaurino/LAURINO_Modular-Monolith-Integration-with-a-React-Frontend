-- Run this in Supabase: SQL Editor -> New query -> Run.
-- It is intentionally safe to re-run while you are developing.

create table if not exists inventory (
    product_id varchar(50) primary key,
    name varchar(150) not null,
    stock integer not null check (stock >= 0)
);

create table if not exists orders (
    order_id bigserial primary key,
    product_id varchar(50) not null,
    quantity integer not null check (quantity > 0),
    status varchar(20) not null check (status in ('CONFIRMED', 'REJECTED')),
    reason varchar(255),
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
