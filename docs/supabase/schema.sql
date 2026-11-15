-- Expense Tracker: Supabase PostgreSQL Remote Schema
-- Version: 1.0 (v0.4 Cloud Sync)
-- Tables: ledgers, categories, transactions
-- Enforces Row-Level Security (RLS) bound to auth.uid()

-- 1. Ledgers table
create table if not exists public.ledgers (
    id text primary key default gen_random_uuid()::text,
    user_id uuid references auth.users(id) on delete cascade not null,
    name text not null,
    currency_code text not null default 'USD',
    is_default boolean not null default false,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    is_deleted boolean not null default false
);
alter table public.ledgers enable row level security;

do $$
begin
    if not exists (select 1 from pg_policies where tablename = 'ledgers' and policyname = 'Users manage own ledgers') then
        create policy "Users manage own ledgers" on public.ledgers
            for all
            to authenticated
            using (user_id = (select auth.uid()))
            with check (user_id = (select auth.uid()));
    end if;
end $$;

-- 2. Categories table
create table if not exists public.categories (
    id text primary key default gen_random_uuid()::text,
    user_id uuid references auth.users(id) on delete cascade not null,
    name text not null,
    type text not null check (type in ('EXPENSE', 'INCOME')),
    icon text not null default 'other',
    is_default boolean not null default false,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    is_deleted boolean not null default false
);
alter table public.categories enable row level security;

do $$
begin
    if not exists (select 1 from pg_policies where tablename = 'categories' and policyname = 'Users manage own categories') then
        create policy "Users manage own categories" on public.categories
            for all
            to authenticated
            using (user_id = (select auth.uid()))
            with check (user_id = (select auth.uid()));
    end if;
end $$;

-- 3. Transactions table
create table if not exists public.transactions (
    id text primary key default gen_random_uuid()::text,
    user_id uuid references auth.users(id) on delete cascade not null,
    ledger_id text references public.ledgers(id) on delete cascade not null,
    category_id text references public.categories(id) on delete set null,
    amount numeric(15, 2) not null,
    type text not null check (type in ('EXPENSE', 'INCOME')),
    date bigint not null,
    note text not null default '',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    is_deleted boolean not null default false
);
alter table public.transactions enable row level security;

do $$
begin
    if not exists (select 1 from pg_policies where tablename = 'transactions' and policyname = 'Users manage own transactions') then
        create policy "Users manage own transactions" on public.transactions
            for all
            to authenticated
            using (user_id = (select auth.uid()))
            with check (user_id = (select auth.uid()));
    end if;
end $$;

-- Indexes for performance
create index if not exists idx_ledgers_user_id on public.ledgers(user_id);
create index if not exists idx_categories_user_id on public.categories(user_id);
create index if not exists idx_transactions_user_id on public.transactions(user_id);
create index if not exists idx_transactions_ledger_id on public.transactions(ledger_id);
create index if not exists idx_transactions_category_id on public.transactions(category_id);
create index if not exists idx_transactions_updated_at on public.transactions(updated_at);

-- Security hardening: prevent anon/authenticated execution of security definer helper functions
revoke execute on function public.rls_auto_enable() from public, anon, authenticated;
