-- Craftora initial schema (Phase 1 MVP).
-- Deferred entities (not created until their phase): payment_event, favorite, review, custom_domain.

-- ---------------------------------------------------------------- users & auth
create table app_user (
    id                uuid primary key,
    email             varchar(254) not null unique,  -- always stored lower-case
    password_hash     varchar(100) not null,
    display_name      varchar(100) not null,
    preferred_locale  varchar(5)   not null default 'pl',
    status            varchar(20)  not null default 'ACTIVE',
    email_verified_at timestamptz,
    created_at        timestamptz  not null,
    updated_at        timestamptz  not null
);

create table user_role (
    user_id uuid        not null references app_user (id) on delete cascade,
    role    varchar(20) not null,
    primary key (user_id, role)
);

create table user_session (
    id           uuid primary key,
    user_id      uuid        not null references app_user (id) on delete cascade,
    token_hash   varchar(64) not null unique,
    created_at   timestamptz not null,
    expires_at   timestamptz not null,
    last_seen_at timestamptz not null
);
create index ix_user_session_user on user_session (user_id);
create index ix_user_session_expires on user_session (expires_at);

create table user_token (
    id         uuid primary key,
    user_id    uuid        not null references app_user (id) on delete cascade,
    type       varchar(30) not null,
    token_hash varchar(64) not null unique,
    expires_at timestamptz not null,
    used_at    timestamptz,
    created_at timestamptz not null
);
create index ix_user_token_user on user_token (user_id, type);

-- ---------------------------------------------------------------- catalogue
create table category (
    id         uuid primary key,
    slug       varchar(80) not null unique,
    position   int         not null default 0,
    active     boolean     not null default true,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table category_translation (
    category_id uuid         not null references category (id) on delete cascade,
    locale      varchar(5)   not null,
    name        varchar(100) not null,
    primary key (category_id, locale)
);

create table tag (
    id   uuid primary key,
    slug varchar(60) not null unique,
    name varchar(60) not null
);

-- ---------------------------------------------------------------- plans
create table subscription_plan (
    code                   varchar(20) primary key,
    name                   varchar(60) not null,
    price_minor            bigint      not null,
    currency               varchar(3)     not null,
    billing_interval       varchar(10) not null,
    max_published_products int         not null,
    max_images_per_product int         not null,
    custom_domain          boolean     not null default false,
    analytics              boolean     not null default false,
    reduced_branding       boolean     not null default false,
    position               int         not null default 0,
    active                 boolean     not null default true
);

-- ---------------------------------------------------------------- makers
create table maker_profile (
    id                    uuid primary key,
    user_id               uuid         not null unique references app_user (id) on delete cascade,
    slug                  varchar(60)  not null unique,
    business_name         varchar(120) not null,
    tagline               varchar(160),
    bio                   text,
    city                  varchar(80),
    region                varchar(80),
    country_code          varchar(2)      not null default 'PL',
    show_location         boolean      not null default true,
    logo_key              varchar(255),
    cover_key             varchar(255),
    contact_email         varchar(254),
    phone                 varchar(40),
    website_url           varchar(255),
    instagram_url         varchar(255),
    facebook_url          varchar(255),
    tiktok_url            varchar(255),
    pinterest_url         varchar(255),
    accepts_custom_orders boolean      not null default true,
    primary_category_id   uuid references category (id),
    -- seller identity (trader information); shown publicly where legally required
    legal_name            varchar(160),
    tax_id                varchar(40),
    business_address      varchar(255),
    status                varchar(20)  not null default 'PENDING',
    featured              boolean      not null default false,
    approved_at           timestamptz,
    created_at            timestamptz  not null,
    updated_at            timestamptz  not null
);
create index ix_maker_status on maker_profile (status);
create index ix_maker_city on maker_profile (lower(city));

create table subscription (
    id                 uuid primary key,
    maker_id           uuid        not null unique references maker_profile (id) on delete cascade,
    plan_code          varchar(20) not null references subscription_plan (code),
    status             varchar(20) not null,
    current_period_end timestamptz,
    created_at         timestamptz not null,
    updated_at         timestamptz not null
);

-- ---------------------------------------------------------------- storefronts
create table storefront_template (
    key                   varchar(40) primary key,
    name                  varchar(60)  not null,
    description           varchar(255) not null,
    default_primary_color varchar(7)   not null,
    default_accent_color  varchar(7)   not null,
    default_font          varchar(30)  not null,
    position              int          not null default 0,
    active                boolean      not null default true
);

create table storefront (
    id               uuid primary key,
    maker_id         uuid        not null unique references maker_profile (id) on delete cascade,
    status           varchar(20) not null default 'DRAFT',
    draft_config     text        not null,
    published_config text,
    published_at     timestamptz,
    created_at       timestamptz not null,
    updated_at       timestamptz not null
);

-- ---------------------------------------------------------------- products
create table product (
    id                 uuid primary key,
    maker_id           uuid         not null references maker_profile (id) on delete cascade,
    category_id        uuid references category (id),
    slug               varchar(140) not null,
    name               varchar(140) not null,
    description        text,
    price_minor        bigint,
    currency           varchar(3)      not null default 'PLN',
    status             varchar(20)  not null default 'DRAFT',
    availability_type  varchar(20)  not null default 'READY_MADE',
    production_days    int,
    available          boolean      not null default true,
    customizable       boolean      not null default false,
    materials          varchar(255),
    dimensions         varchar(255),
    shipping_info      text,
    show_on_storefront boolean      not null default true,
    moderation_status  varchar(20)  not null default 'VISIBLE',
    moderation_reason  varchar(255),
    published_at       timestamptz,
    created_at         timestamptz  not null,
    updated_at         timestamptz  not null,
    search_vector      tsvector generated always as (
        setweight(to_tsvector('simple', coalesce(name, '')), 'A') ||
        setweight(to_tsvector('simple', coalesce(materials, '')), 'B') ||
        setweight(to_tsvector('simple', coalesce(description, '')), 'C')
    ) stored,
    constraint ux_product_maker_slug unique (maker_id, slug),
    constraint ck_product_price check (price_minor is null or price_minor >= 0)
);
create index ix_product_maker on product (maker_id);
create index ix_product_listing on product (status, moderation_status, published_at desc);
create index ix_product_category on product (category_id);
create index ix_product_search on product using gin (search_vector);

create table product_image (
    id           uuid primary key,
    product_id   uuid         not null references product (id) on delete cascade,
    storage_key  varchar(255) not null,
    position     int          not null,
    alt_text     varchar(200),
    content_type varchar(50)  not null,
    size_bytes   bigint       not null,
    created_at   timestamptz  not null
);
create index ix_product_image_product on product_image (product_id, position);

create table product_option (
    id         uuid primary key,
    product_id uuid         not null references product (id) on delete cascade,
    name       varchar(80)  not null,
    type       varchar(20)  not null,
    choices    text[],
    required   boolean      not null default false,
    position   int          not null
);
create index ix_product_option_product on product_option (product_id);

create table product_tag (
    product_id uuid not null references product (id) on delete cascade,
    tag_id     uuid not null references tag (id) on delete cascade,
    primary key (product_id, tag_id)
);
create index ix_product_tag_tag on product_tag (tag_id);

create table product_occasion (
    product_id uuid        not null references product (id) on delete cascade,
    occasion   varchar(30) not null,
    primary key (product_id, occasion)
);
create index ix_product_occasion on product_occasion (occasion);

-- ---------------------------------------------------------------- inquiries
create table custom_order_inquiry (
    id               uuid primary key,
    maker_id         uuid         not null references maker_profile (id) on delete cascade,
    product_id       uuid references product (id) on delete set null,
    customer_user_id uuid references app_user (id) on delete set null,
    customer_name    varchar(100) not null,
    customer_email   varchar(254) not null,
    customer_phone   varchar(40),
    message          text         not null,
    budget_minor     bigint,
    currency         varchar(3)      not null default 'PLN',
    deadline         date,
    status           varchar(20)  not null default 'NEW',
    internal_notes   text,
    consent_at       timestamptz  not null,
    created_at       timestamptz  not null,
    updated_at       timestamptz  not null
);
create index ix_inquiry_maker on custom_order_inquiry (maker_id, status, created_at desc);
create index ix_inquiry_customer on custom_order_inquiry (customer_user_id);

create table inquiry_attachment (
    id                uuid primary key,
    inquiry_id        uuid         not null references custom_order_inquiry (id) on delete cascade,
    storage_key       varchar(255) not null,
    original_filename varchar(255) not null,
    content_type      varchar(50)  not null,
    size_bytes        bigint       not null,
    created_at        timestamptz  not null
);
create index ix_inquiry_attachment_inquiry on inquiry_attachment (inquiry_id);

create table inquiry_status_history (
    id                 uuid primary key,
    inquiry_id         uuid        not null references custom_order_inquiry (id) on delete cascade,
    from_status        varchar(20),
    to_status          varchar(20) not null,
    customer_message   text,       -- visible to the customer; never holds internal notes
    changed_by_user_id uuid references app_user (id) on delete set null,
    created_at         timestamptz not null
);
create index ix_inquiry_history_inquiry on inquiry_status_history (inquiry_id, created_at);

-- ---------------------------------------------------------------- moderation & audit
create table report (
    id               uuid primary key,
    target_type      varchar(20)  not null,
    target_id        uuid         not null,
    reason           varchar(30)  not null,
    details          text,
    reporter_user_id uuid references app_user (id) on delete set null,
    reporter_email   varchar(254),
    status           varchar(20)  not null default 'OPEN',
    resolution_note  varchar(500),
    resolved_by      uuid references app_user (id) on delete set null,
    resolved_at      timestamptz,
    created_at       timestamptz  not null
);
create index ix_report_status on report (status, created_at desc);

create table audit_log (
    id            uuid primary key,
    actor_user_id uuid references app_user (id) on delete set null,
    action        varchar(60) not null,
    target_type   varchar(30),
    target_id     uuid,
    details       varchar(500),
    created_at    timestamptz not null
);
create index ix_audit_created on audit_log (created_at desc);
