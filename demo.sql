-- ============================================================
-- GAPTEK - DATABASE SCHEMA LENGKAP
-- PostgreSQL v15+
-- Versi: 1.0.0
-- Deskripsi: Schema database enterprise untuk sistem manajemen
--            ritel modern GAPTEK mencakup 95+ modul fungsional
-- ============================================================

-- ============================================================
-- EKSTENSI YANG DIPERLUKAN
-- ============================================================
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";       -- Full-text search
CREATE EXTENSION IF NOT EXISTS "btree_gist";    -- Constraint exclusion (slot booking)
CREATE EXTENSION IF NOT EXISTS "hstore";        -- Key-value metadata

-- ============================================================
-- DOMAIN & TIPE DATA KUSTOM
-- ============================================================
CREATE DOMAIN positive_numeric AS NUMERIC(20,4) CHECK (VALUE >= 0);
CREATE DOMAIN percentage AS NUMERIC(5,2) CHECK (VALUE BETWEEN 0 AND 100);
CREATE DOMAIN phone_number AS VARCHAR(20);
CREATE DOMAIN currency_code AS CHAR(3);

CREATE TYPE gender_type AS ENUM ('M','F','OTHER');
CREATE TYPE approval_status AS ENUM ('PENDING','APPROVED','REJECTED','CANCELLED');
CREATE TYPE sync_status AS ENUM ('SYNCED','PENDING','CONFLICT','FAILED');
CREATE TYPE document_status AS ENUM ('DRAFT','SUBMITTED','PARTIAL','COMPLETED','CANCELLED','VOIDED');
CREATE TYPE payment_method_type AS ENUM ('CASH','QRIS','EDC_DEBIT','EDC_CREDIT','TRANSFER','DEPOSIT','POINTS','STORE_CREDIT','INSTALLMENT','EMPLOYEE_DEDUCTION');
CREATE TYPE tax_type AS ENUM ('INCLUSIVE','EXCLUSIVE','EXEMPT');
CREATE TYPE stock_movement_type AS ENUM ('PURCHASE_IN','SALE_OUT','RETURN_IN','RETURN_OUT','TRANSFER_IN','TRANSFER_OUT','ADJUSTMENT_IN','ADJUSTMENT_OUT','OPNAME','WRITE_OFF','BUNDLE_IN','BUNDLE_OUT','LOAN_OUT','LOAN_IN','SCRAP','SELF_CONSUME','RMA_OUT','RMA_IN','CONSIGN_OUT','CONSIGN_IN','RTO_IN','INITIAL');
CREATE TYPE unit_grade AS ENUM ('NEW','DISPLAY','REFURBISHED_A','REFURBISHED_B','SCRAP','PRE_OWNED');
CREATE TYPE service_status AS ENUM ('RECEIVED','CHECKED','WAITING_PART','IN_REPAIR','QC','DONE','DELIVERED','CANCELLED');
CREATE TYPE delivery_status AS ENUM ('PACKED','READY_TO_SHIP','IN_TRANSIT','DELIVERED','RETURNED','RTO');
CREATE TYPE po_status AS ENUM ('DRAFT','SUBMITTED','PARTIAL','COMPLETED','CANCELLED');
CREATE TYPE promo_type AS ENUM ('PERCENTAGE','NOMINAL','BUY_X_GET_Y','BUNDLE','TIERED','HAPPY_HOUR','FLASH_SALE','GWP','SEASONAL');
CREATE TYPE member_tier AS ENUM ('REGULAR','SILVER','GOLD','PLATINUM');
CREATE TYPE rma_status AS ENUM ('PENDING','SENT_TO_VENDOR','UNDER_REPAIR','WAITING_UNIT','UNIT_REPLACED','UNIT_REPAIRED','CLOSED','CANCELLED');
CREATE TYPE asset_status AS ENUM ('ACTIVE','MAINTENANCE','DISPOSED','LOST');
CREATE TYPE consignment_type AS ENUM ('IN','OUT');
CREATE TYPE loan_status AS ENUM ('ACTIVE','RETURNED','CONVERTED_SALE','OVERDUE');
CREATE TYPE inter_company_status AS ENUM ('DRAFT','SENT','RECEIVED','RECONCILED');
CREATE TYPE queue_status AS ENUM ('WAITING','IN_SERVICE','DONE','CANCELLED','NO_SHOW');
CREATE TYPE fraud_alert_type AS ENUM ('TRANSACTION_SPIKE','ODD_HOUR','HIGH_VOID','PRICE_ANOMALY','REFUND_ABUSE','STOCK_DISCREPANCY','MULTI_ACCOUNT');

-- ============================================================
-- SCHEMA LOGICAL (NAMESPACE)
-- ============================================================
CREATE SCHEMA IF NOT EXISTS core;         -- Master data & konfigurasi
CREATE SCHEMA IF NOT EXISTS pos;          -- Point of Sale
CREATE SCHEMA IF NOT EXISTS inventory;   -- Inventori & gudang
CREATE SCHEMA IF NOT EXISTS crm;         -- Customer & loyalty
CREATE SCHEMA IF NOT EXISTS finance;     -- Keuangan & akuntansi
CREATE SCHEMA IF NOT EXISTS hr;          -- SDM & absensi
CREATE SCHEMA IF NOT EXISTS logistics;   -- Pengiriman
CREATE SCHEMA IF NOT EXISTS audit;       -- Audit trail & keamanan
CREATE SCHEMA IF NOT EXISTS analytics;  -- BI & prediksi

SET search_path = core, pos, inventory, crm, finance, hr, logistics, audit, analytics, public;

-- ===========================================================
-- ███████╗███████╗ ██████╗████████╗██╗ ██████╗ ███╗   ██╗
-- ██╔════╝██╔════╝██╔════╝╚══██╔══╝██║██╔═══██╗████╗  ██║
-- ███████╗█████╗  ██║        ██║   ██║██║   ██║██╔██╗ ██║
-- ╚════██║██╔══╝  ██║        ██║   ██║██║   ██║██║╚██╗██║
-- ███████║███████╗╚██████╗   ██║   ██║╚██████╔╝██║ ╚████║
-- ╚══════╝╚══════╝ ╚═════╝   ╚═╝   ╚═╝ ╚═════╝ ╚═╝  ╚═══╝
-- SECTION 1: CORE - MULTI-TENANT, COMPANY, BRANCH
-- ===========================================================

-- Tenant (SaaS top-level isolation)
CREATE TABLE core.tenants (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code            VARCHAR(20) UNIQUE NOT NULL,
    name            VARCHAR(200) NOT NULL,
    plan            VARCHAR(50) NOT NULL DEFAULT 'STARTER', -- STARTER, PROFESSIONAL, ENTERPRISE
    max_branches    INT NOT NULL DEFAULT 1,
    max_users       INT NOT NULL DEFAULT 5,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    trial_ends_at   TIMESTAMPTZ,
    metadata        HSTORE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Perusahaan / Legal Entity
CREATE TABLE core.companies (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    code            VARCHAR(20) NOT NULL,
    name            VARCHAR(200) NOT NULL,
    legal_name      VARCHAR(200),
    npwp            VARCHAR(20),
    pkp_number      VARCHAR(50),          -- Nomor pengukuhan PKP
    address         TEXT,
    phone           phone_number,
    email           VARCHAR(200),
    logo_url        TEXT,
    base_currency   currency_code NOT NULL DEFAULT 'IDR',
    fiscal_year_start SMALLINT NOT NULL DEFAULT 1, -- Bulan mulai tahun fiskal
    tax_method      tax_type NOT NULL DEFAULT 'EXCLUSIVE',
    rounding_method VARCHAR(20) NOT NULL DEFAULT 'DOWN_100', -- DOWN_100, UP_100, NEAREST_500
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, code)
);

-- Cabang / Outlet
CREATE TABLE core.branches (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id      UUID NOT NULL REFERENCES core.com panies(id),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    code            VARCHAR(20) NOT NULL,
    name            VARCHAR(200) NOT NULL,
    branch_type     VARCHAR(30) NOT NULL DEFAULT 'STORE', -- STORE, WAREHOUSE, HQ, VIRTUAL
    address         TEXT,
    city            VARCHAR(100),
    province        VARCHAR(100),
    latitude        DECIMAL(10,7),
    longitude       DECIMAL(10,7),
    phone           phone_number,
    timezone        VARCHAR(50) NOT NULL DEFAULT 'Asia/Jakarta',
    is_online_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    online_stock_buffer INT NOT NULL DEFAULT 0,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(company_id, code)
);

-- ===========================================================
-- SECTION 2: RBAC - ROLES, USERS, PERMISSIONS
-- ===========================================================

CREATE TABLE core.roles (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    code            VARCHAR(50) NOT NULL,
    name            VARCHAR(100) NOT NULL,
    description     TEXT,
    is_system       BOOLEAN NOT NULL DEFAULT FALSE, -- Role bawaan, tidak bisa dihapus
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, code)
);

CREATE TABLE core.permissions (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    module          VARCHAR(50) NOT NULL,   -- POS, INVENTORY, CRM, FINANCE, HR, etc.
    action          VARCHAR(50) NOT NULL,   -- CREATE, READ, UPDATE, DELETE, APPROVE, VOID
    resource        VARCHAR(100) NOT NULL,  -- TRANSACTION, DISCOUNT, STOCK, PRICE, etc.
    description     TEXT,
    UNIQUE(module, action, resource)
);

CREATE TABLE core.role_permissions (
    role_id         UUID NOT NULL REFERENCES core.roles(id) ON DELETE CASCADE,
    permission_id   UUID NOT NULL REFERENCES core.permissions(id) ON DELETE CASCADE,
    PRIMARY KEY(role_id, permission_id)
);

CREATE TABLE core.users (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    mitra_id       UUID NOT NULL REFERENCES core.tenants(id),
    employee_id     UUID,                  -- FK ke hr.employees (set setelah HR schema)
    username        VARCHAR(100) NOT NULL,
    email           VARCHAR(200),
    password_hash   TEXT NOT NULL,
    full_name       VARCHAR(200) NOT NULL,
    phone           phone_number,
    avatar_url      TEXT,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    last_login_at   TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(mitra_id, username)
);

CREATE TABLE core.user_roles (
    user_id         UUID NOT NULL REFERENCES core.users(id) ON DELETE CASCADE,
    role_id         UUID NOT NULL REFERENCES core.roles(id) ON DELETE CASCADE,
    branch_id       UUID REFERENCES core.branches(id), -- NULL = berlaku di semua cabang
    granted_by      UUID REFERENCES core.users(id),
    granted_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY(user_id, role_id, COALESCE(branch_id, '00000000-0000-0000-0000-000000000000'::UUID))
);

-- Approval Authority Matrix
CREATE TABLE core.approval_matrix (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    action_code     VARCHAR(100) NOT NULL, -- e.g.: DISCOUNT_APPLY, VOID_TRANSACTION
    min_amount      NUMERIC(20,4),
    max_amount      NUMERIC(20,4),
    min_percent     percentage,
    max_percent     percentage,
    required_role_id UUID REFERENCES core.roles(id),
    requires_two_approvers BOOLEAN NOT NULL DEFAULT FALSE,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE(tenant_id, action_code, min_amount, max_amount)
);

-- Session & Token
CREATE TABLE core.user_sessions (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL REFERENCES core.users(id),
    branch_id       UUID REFERENCES core.branches(id),
    token_hash      TEXT NOT NULL,
    device_info     JSONB,
    ip_address      INET,
    expires_at      TIMESTAMPTZ NOT NULL,
    last_active_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_revoked      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ===========================================================
-- SECTION 3: MASTER DATA PRODUK
-- ===========================================================

CREATE TABLE core.categories (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    parent_id       UUID REFERENCES core.categories(id),
    name            VARCHAR(200) NOT NULL,
    description     TEXT,
    image_url       TEXT,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order      INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, code)
);

CREATE TABLE core.brands (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    code            VARCHAR(50) NOT NULL,
    name            VARCHAR(200) NOT NULL,
    logo_url        TEXT,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, code)
);

-- Unit of Measure
CREATE TABLE core.uom (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    code            VARCHAR(20) NOT NULL,
    name            VARCHAR(50) NOT NULL,
    uom_type        VARCHAR(30) NOT NULL DEFAULT 'QUANTITY', -- QUANTITY, WEIGHT, VOLUME, LENGTH
    UNIQUE(tenant_id, code)
);

CREATE TABLE core.uom_conversions (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    from_uom_id     UUID NOT NULL REFERENCES core.uom(id),
    to_uom_id       UUID NOT NULL REFERENCES core.uom(id),
    factor          NUMERIC(20,6) NOT NULL, -- 1 from_uom = factor to_uom (e.g. 1 Karton = 24 Pcs)
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE(tenant_id, from_uom_id, to_uom_id)
);

-- Product Master (Produk Induk)
CREATE TABLE core.products (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    category_id     UUID REFERENCES core.categories(id),
    brand_id        UUID REFERENCES core.brands(id),
    sku             VARCHAR(100) NOT NULL,
    name            VARCHAR(500) NOT NULL,
    description     TEXT,
    base_uom_id     UUID NOT NULL REFERENCES core.uom(id),    -- Satuan terkecil (Pcs)
    purchase_uom_id UUID REFERENCES core.uom(id),             -- Satuan beli (Karton)
    has_serial      BOOLEAN NOT NULL DEFAULT FALSE,            -- Wajib input SN/IMEI?
    has_expiry      BOOLEAN NOT NULL DEFAULT FALSE,            -- Punya kedaluwarsa?
    has_batch       BOOLEAN NOT NULL DEFAULT FALSE,            -- Tracking Batch?
    is_bundle       BOOLEAN NOT NULL DEFAULT FALSE,            -- Produk paket?
    is_digital      BOOLEAN NOT NULL DEFAULT FALSE,            -- Lisensi/digital product?
    is_consignment  BOOLEAN NOT NULL DEFAULT FALSE,            -- Barang titipan?
    is_service      BOOLEAN NOT NULL DEFAULT FALSE,            -- Layanan (non-stok)?
    weight_gram     NUMERIC(10,3),
    dimension_cm    JSONB,                                     -- {l, w, h}
    image_urls      TEXT[],
    barcode         VARCHAR(100),
    tax_code_id     UUID,                                      -- FK ke finance.tax_codes
    warranty_months INT NOT NULL DEFAULT 0,
    reorder_point   NUMERIC(20,4) NOT NULL DEFAULT 0,
    safety_stock    NUMERIC(20,4) NOT NULL DEFAULT 0,
    min_order_qty   NUMERIC(20,4) NOT NULL DEFAULT 1,          -- MOQ
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    is_archived     BOOLEAN NOT NULL DEFAULT FALSE,
    metadata        HSTORE,
    created_by      UUID REFERENCES core.users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, sku)
);
CREATE INDEX idx_products_barcode ON core.products(tenant_id, barcode) WHERE barcode IS NOT NULL;
CREATE INDEX idx_products_name_trgm ON core.products USING gin(name gin_trgm_ops);

-- Product Variant (Varian Warna/Ukuran)
CREATE TABLE core.product_variants (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id      UUID NOT NULL REFERENCES core.products(id),
    sku_variant     VARCHAR(100) NOT NULL,
    name            VARCHAR(200) NOT NULL,   -- e.g. "iPhone 15 - Hitam 256GB"
    barcode         VARCHAR(100),
    attributes      JSONB,                   -- {color: "Hitam", storage: "256GB"}
    image_url       TEXT,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE(product_id, sku_variant)
);

-- Bundle Components
CREATE TABLE core.bundle_components (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    bundle_product_id UUID NOT NULL REFERENCES core.products(id),
    component_product_id UUID NOT NULL REFERENCES core.products(id),
    quantity        NUMERIC(20,4) NOT NULL,
    component_uom_id UUID REFERENCES core.uom(id),
    UNIQUE(bundle_product_id, component_product_id)
);

-- Digital Product License Vault
CREATE TABLE core.digital_licenses (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id      UUID NOT NULL REFERENCES core.products(id),
    license_key     TEXT NOT NULL,
    is_sold         BOOLEAN NOT NULL DEFAULT FALSE,
    sale_id         UUID,                    -- FK ke pos.sales setelah terjual
    sold_at         TIMESTAMPTZ,
    expires_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_digital_licenses_available ON core.digital_licenses(product_id) WHERE is_sold = FALSE;

-- ===========================================================
-- SECTION 4: PRICING ENGINE
-- ===========================================================

-- Daftar Harga (Price List)
CREATE TABLE core.price_lists (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    code            VARCHAR(50) NOT NULL,
    name            VARCHAR(200) NOT NULL,
    currency_code   currency_code NOT NULL DEFAULT 'IDR',
    price_list_type VARCHAR(30) NOT NULL DEFAULT 'SELL', -- SELL, PURCHASE, MEMBER, WHOLESALE
    member_tier     member_tier,                         -- Berlaku untuk tier member apa?
    customer_role   VARCHAR(50),                         -- RETAIL, RESELLER, DISTRIBUTOR
    valid_from      DATE,
    valid_to        DATE,
    is_default      BOOLEAN NOT NULL DEFAULT FALSE,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, code)
);

-- Harga per Produk per Price List
CREATE TABLE core.product_prices (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    price_list_id   UUID NOT NULL REFERENCES core.price_lists(id),
    product_id      UUID NOT NULL REFERENCES core.products(id),
    variant_id      UUID REFERENCES core.product_variants(id),
    uom_id          UUID NOT NULL REFERENCES core.uom(id),
    price           NUMERIC(20,4) NOT NULL,
    min_qty         NUMERIC(20,4) NOT NULL DEFAULT 0,    -- Harga ini berlaku mulai qty berapa
    max_qty         NUMERIC(20,4),
    valid_from      DATE,
    valid_to        DATE,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    updated_by      UUID REFERENCES core.users(id),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(price_list_id, product_id, COALESCE(variant_id, '00000000-0000-0000-0000-000000000000'::UUID), uom_id, min_qty)
);

-- Riwayat Harga Beli dari Supplier (untuk negosiasi)
CREATE TABLE core.purchase_price_history (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id      UUID NOT NULL REFERENCES core.products(id),
    supplier_id     UUID NOT NULL,           -- FK ke core.suppliers
    price           NUMERIC(20,4) NOT NULL,
    currency_code   currency_code NOT NULL DEFAULT 'IDR',
    effective_date  DATE NOT NULL,
    po_id           UUID,                    -- FK ke inventory.purchase_orders
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Regional Pricing
CREATE TABLE core.regional_prices (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_price_id UUID NOT NULL REFERENCES core.product_prices(id),
    branch_id       UUID NOT NULL REFERENCES core.branches(id),
    price_override  NUMERIC(20,4) NOT NULL,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE(product_price_id, branch_id)
);

-- Dynamic Margin Pricing Config
CREATE TABLE core.margin_rules (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    category_id     UUID REFERENCES core.categories(id),
    product_id      UUID REFERENCES core.products(id),
    target_margin_pct percentage NOT NULL,
    price_guard_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    roundup_rule    JSONB,  -- {threshold: 50, roundto: 100} 
    is_active       BOOLEAN NOT NULL DEFAULT TRUE
);

-- ===========================================================
-- SECTION 5: SUPPLIER & VENDOR MANAGEMENT
-- ===========================================================

CREATE TABLE core.suppliers (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    code            VARCHAR(50) NOT NULL,
    name            VARCHAR(200) NOT NULL,
    legal_name      VARCHAR(200),
    npwp            VARCHAR(20),
    address         TEXT,
    city            VARCHAR(100),
    phone           phone_number,
    email           VARCHAR(200),
    contact_person  VARCHAR(200),
    payment_terms_days INT NOT NULL DEFAULT 30,
    lead_time_days  INT NOT NULL DEFAULT 7,
    credit_limit    NUMERIC(20,4),
    is_approved     BOOLEAN NOT NULL DEFAULT FALSE, -- AVL: Approved Vendor List
    is_blocked      BOOLEAN NOT NULL DEFAULT FALSE,
    block_reason    TEXT,
    blocked_by      UUID REFERENCES core.users(id),
    blocked_at      TIMESTAMPTZ,
    metadata        HSTORE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, code)
);

-- Supplier Performance Tracking
CREATE TABLE core.supplier_performance (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    supplier_id     UUID NOT NULL REFERENCES core.suppliers(id),
    period_year     SMALLINT NOT NULL,
    period_month    SMALLINT NOT NULL,
    total_orders    INT NOT NULL DEFAULT 0,
    late_deliveries INT NOT NULL DEFAULT 0,
    damaged_items   INT NOT NULL DEFAULT 0,
    wrong_items     INT NOT NULL DEFAULT 0,
    on_time_rate    percentage,
    quality_rate    percentage,
    notes           TEXT,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(supplier_id, period_year, period_month)
);

-- Supplier Contract & Rebate
CREATE TABLE core.supplier_contracts (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    supplier_id     UUID NOT NULL REFERENCES core.suppliers(id),
    contract_no     VARCHAR(100),
    title           VARCHAR(200) NOT NULL,
    start_date      DATE NOT NULL,
    end_date        DATE,
    document_url    TEXT,
    notes           TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE core.supplier_rebate_targets (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    supplier_id     UUID NOT NULL REFERENCES core.suppliers(id),
    contract_id     UUID REFERENCES core.supplier_contracts(id),
    period_start    DATE NOT NULL,
    period_end      DATE NOT NULL,
    target_amount   NUMERIC(20,4) NOT NULL,
    rebate_pct      percentage NOT NULL,
    rebate_amount_fixed NUMERIC(20,4),      -- Bonus nominal tetap
    achieved_amount NUMERIC(20,4) NOT NULL DEFAULT 0,
    rebate_status   approval_status NOT NULL DEFAULT 'PENDING',
    claimed_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Gondola / Rak Sewa Supplier
CREATE TABLE core.supplier_shelf_rents (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    supplier_id     UUID NOT NULL REFERENCES core.suppliers(id),
    branch_id       UUID NOT NULL REFERENCES core.branches(id),
    shelf_location  VARCHAR(100) NOT NULL,
    monthly_fee     NUMERIC(20,4) NOT NULL,
    start_date      DATE NOT NULL,
    end_date        DATE,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ===========================================================
-- SECTION 6: TAX & COA (CHART OF ACCOUNTS)
-- ===========================================================

CREATE TABLE finance.tax_codes (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    code            VARCHAR(20) NOT NULL,
    name            VARCHAR(100) NOT NULL,
    rate            percentage NOT NULL,
    tax_type        tax_type NOT NULL DEFAULT 'EXCLUSIVE',
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE(tenant_id, code)
);

CREATE TABLE finance.chart_of_accounts (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id      UUID NOT NULL REFERENCES core.companies(id),
    parent_id       UUID REFERENCES finance.chart_of_accounts(id),
    account_code    VARCHAR(20) NOT NULL,
    account_name    VARCHAR(200) NOT NULL,
    account_type    VARCHAR(30) NOT NULL, -- ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE, COGS
    normal_balance  CHAR(1) NOT NULL CHECK (normal_balance IN ('D','C')), -- Debit/Credit
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(company_id, account_code)
);

-- Mapping Transaksi ke COA
CREATE TABLE finance.transaction_coa_mapping (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    transaction_type VARCHAR(100) NOT NULL, -- SALE, PURCHASE, COGS, TAX_PAYABLE, LOYALTY_EXPENSE, etc.
    coa_id          UUID NOT NULL REFERENCES finance.chart_of_accounts(id),
    debit_credit    CHAR(1) NOT NULL CHECK (debit_credit IN ('D','C')),
    UNIQUE(tenant_id, transaction_type)
);

-- Kurs Mata Uang
CREATE TABLE finance.exchange_rates (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    from_currency   currency_code NOT NULL,
    to_currency     currency_code NOT NULL,
    rate            NUMERIC(20,6) NOT NULL,
    effective_date  DATE NOT NULL,
    source          VARCHAR(50),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, from_currency, to_currency, effective_date)
);

-- ===========================================================
-- SECTION 7: PROMO & VOUCHER ENGINE
-- ===========================================================

CREATE TABLE pos.promotions (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    code            VARCHAR(50) NOT NULL,
    name            VARCHAR(200) NOT NULL,
    description     TEXT,
    promo_type      promo_type NOT NULL,
    discount_pct    percentage,
    discount_amount NUMERIC(20,4),
    min_purchase    NUMERIC(20,4),
    max_discount    NUMERIC(20,4),
    buy_qty         INT,                     -- "Beli X"
    get_qty         INT,                     -- "Gratis Y"
    free_product_id UUID REFERENCES core.products(id),
    start_date      TIMESTAMPTZ NOT NULL,
    end_date        TIMESTAMPTZ,
    start_time      TIME,                    -- Happy Hour start
    end_time        TIME,                    -- Happy Hour end
    applicable_days INT[],                   -- [1,2,3,4,5] = Senin-Jumat
    quota_total     INT,                     -- Total kuota promo
    quota_per_day   INT,
    quota_per_customer INT,
    quota_per_branch INT,
    used_total      INT NOT NULL DEFAULT 0,
    applicable_branches UUID[],              -- NULL = semua cabang
    applicable_tiers member_tier[],         -- NULL = semua tier
    applicable_categories UUID[],
    applicable_products UUID[],
    stackable_with_voucher BOOLEAN NOT NULL DEFAULT FALSE,
    stackable_with_discount BOOLEAN NOT NULL DEFAULT FALSE,
    gwp_product_id  UUID REFERENCES core.products(id),
    gwp_quantity    NUMERIC(20,4),
    priority        INT NOT NULL DEFAULT 0,  -- Urutan aplikasi promo
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_by      UUID REFERENCES core.users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, code)
);

CREATE TABLE pos.tiered_discounts (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    promotion_id    UUID NOT NULL REFERENCES pos.promotions(id) ON DELETE CASCADE,
    min_qty         NUMERIC(20,4) NOT NULL,
    max_qty         NUMERIC(20,4),
    discount_pct    percentage,
    discount_amount NUMERIC(20,4),
    UNIQUE(promotion_id, min_qty)
);

CREATE TABLE pos.vouchers (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    code            VARCHAR(50) NOT NULL,
    name            VARCHAR(200) NOT NULL,
    voucher_type    VARCHAR(30) NOT NULL DEFAULT 'DISCOUNT', -- DISCOUNT, CASHBACK, FREE_SHIPPING, FREE_ITEM
    discount_pct    percentage,
    discount_amount NUMERIC(20,4),
    min_purchase    NUMERIC(20,4),
    max_discount    NUMERIC(20,4),
    applicable_categories UUID[],
    applicable_products UUID[],
    applicable_branches UUID[],
    total_quota     INT,
    used_count      INT NOT NULL DEFAULT 0,
    valid_from      TIMESTAMPTZ NOT NULL,
    valid_until     TIMESTAMPTZ NOT NULL,
    is_single_use   BOOLEAN NOT NULL DEFAULT TRUE,
    is_birthday_voucher BOOLEAN NOT NULL DEFAULT FALSE,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    generated_batch VARCHAR(100),          -- Untuk voucher yang di-generate massal
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, code)
);

CREATE TABLE pos.voucher_usages (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    voucher_id      UUID NOT NULL REFERENCES pos.vouchers(id),
    customer_id     UUID,                  -- FK ke crm.customers
    sale_id         UUID,                  -- FK ke pos.sales
    used_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    discount_applied NUMERIC(20,4) NOT NULL
);
CREATE INDEX idx_voucher_usages_customer ON pos.voucher_usages(voucher_id, customer_id);

-- ===========================================================
-- SECTION 8: CUSTOMER (CRM)
-- ===========================================================

CREATE TABLE crm.customers (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    customer_no     VARCHAR(50) NOT NULL,
    full_name       VARCHAR(200) NOT NULL,
    phone           phone_number,
    email           VARCHAR(200),
    gender          gender_type,
    date_of_birth   DATE,
    address         TEXT,
    city            VARCHAR(100),
    npwp            VARCHAR(20),           -- Untuk pelanggan B2B / faktur pajak
    customer_type   VARCHAR(30) NOT NULL DEFAULT 'RETAIL', -- RETAIL, RESELLER, DISTRIBUTOR, CORPORATE
    member_tier     member_tier NOT NULL DEFAULT 'REGULAR',
    tier_since      DATE,
    tier_expires_at DATE,
    total_spending_ytd NUMERIC(20,4) NOT NULL DEFAULT 0,  -- Year-to-date
    total_spending_lifetime NUMERIC(20,4) NOT NULL DEFAULT 0,
    credit_limit    NUMERIC(20,4) NOT NULL DEFAULT 0,
    credit_score    INT NOT NULL DEFAULT 50,              -- 0-100 skor kredit internal
    outstanding_ar  NUMERIC(20,4) NOT NULL DEFAULT 0,    -- Piutang berjalan
    deposit_balance NUMERIC(20,4) NOT NULL DEFAULT 0,
    point_balance   INT NOT NULL DEFAULT 0,
    referrer_customer_id UUID REFERENCES crm.customers(id),
    referral_code   VARCHAR(20) UNIQUE,
    membership_type VARCHAR(50),                         -- Basic, Premium
    membership_expires_at TIMESTAMPTZ,
    is_blocked      BOOLEAN NOT NULL DEFAULT FALSE,
    block_reason    TEXT,
    data_source     VARCHAR(50),                         -- WALK_IN, ONLINE, STAFF, REFERRAL
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    merged_into_id  UUID REFERENCES crm.customers(id),  -- Jika akun duplikat sudah digabung
    created_by      UUID REFERENCES core.users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, customer_no)
);
CREATE INDEX idx_customers_phone ON crm.customers(tenant_id, phone) WHERE phone IS NOT NULL;
CREATE INDEX idx_customers_referral ON crm.customers(referral_code) WHERE referral_code IS NOT NULL;

-- Membership Berbayar
CREATE TABLE crm.membership_packages (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    code            VARCHAR(50) NOT NULL,
    name            VARCHAR(200) NOT NULL,
    price           NUMERIC(20,4) NOT NULL,
    duration_months INT NOT NULL DEFAULT 12,
    tier_granted    member_tier NOT NULL,
    benefits        JSONB,                 -- {free_delivery: true, priority_service: true}
    auto_renewal    BOOLEAN NOT NULL DEFAULT FALSE,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE(tenant_id, code)
);

-- Loyalty Points
CREATE TABLE crm.loyalty_rules (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    name            VARCHAR(200) NOT NULL,
    points_per_amount NUMERIC(10,4) NOT NULL DEFAULT 1, -- 1 poin per Rp 10.000
    amount_per_point NUMERIC(10,4) NOT NULL DEFAULT 10000,
    max_burn_pct    percentage NOT NULL DEFAULT 50,     -- Maks 50% transaksi bisa pakai poin
    point_value     NUMERIC(10,4) NOT NULL DEFAULT 1,   -- 1 poin = Rp 1
    expiry_months   INT NOT NULL DEFAULT 12,
    burn_order      VARCHAR(20) NOT NULL DEFAULT 'FIFO', -- FIFO / LIFO
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE crm.customer_points (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id     UUID NOT NULL REFERENCES crm.customers(id),
    points          INT NOT NULL,                        -- Positif = earn, Negatif = burn
    movement_type   VARCHAR(30) NOT NULL,               -- EARN, BURN, EXPIRE, ADJUST, REFERRAL
    reference_id    UUID,                               -- FK ke sale, return, dll
    reference_type  VARCHAR(50),
    expires_at      TIMESTAMPTZ,
    notes           TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_customer_points ON crm.customer_points(customer_id, expires_at);

-- Customer Deposit
CREATE TABLE crm.customer_deposits (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id     UUID NOT NULL REFERENCES crm.customers(id),
    amount          NUMERIC(20,4) NOT NULL,              -- Positif = topup, Negatif = used
    movement_type   VARCHAR(30) NOT NULL,               -- TOPUP, USED, REFUND, ADJUSTMENT
    reference_id    UUID,
    reference_type  VARCHAR(50),
    balance_after   NUMERIC(20,4) NOT NULL,
    notes           TEXT,
    created_by      UUID REFERENCES core.users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Referral
CREATE TABLE crm.referral_events (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    referrer_id     UUID NOT NULL REFERENCES crm.customers(id),
    referee_id      UUID NOT NULL REFERENCES crm.customers(id),
    sale_id         UUID,
    incentive_type  VARCHAR(30) NOT NULL DEFAULT 'POINTS', -- POINTS, VOUCHER
    incentive_value NUMERIC(20,4) NOT NULL,
    is_fulfilled    BOOLEAN NOT NULL DEFAULT FALSE,
    fulfilled_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ===========================================================
-- SECTION 9: INVENTORY - STOK & GUDANG
-- ===========================================================

-- Lokasi Bin Gudang
CREATE TABLE inventory.bin_locations (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    branch_id       UUID NOT NULL REFERENCES core.branches(id),
    code            VARCHAR(50) NOT NULL,  -- e.g. A-02-03 (Rak A, Baris 2, Level 3)
    zone            VARCHAR(50),
    aisle           VARCHAR(20),
    rack            VARCHAR(20),
    level           VARCHAR(20),
    position        VARCHAR(20),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE(branch_id, code)
);

-- Stok Produk per Cabang
CREATE TABLE inventory.stock_balances (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    branch_id       UUID NOT NULL REFERENCES core.branches(id),
    product_id      UUID NOT NULL REFERENCES core.products(id),
    variant_id      UUID REFERENCES core.product_variants(id),
    uom_id          UUID NOT NULL REFERENCES core.uom(id),
    qty_on_hand     NUMERIC(20,4) NOT NULL DEFAULT 0,
    qty_reserved    NUMERIC(20,4) NOT NULL DEFAULT 0,    -- Terreservasi (promo, VIP, quotation)
    qty_in_transit  NUMERIC(20,4) NOT NULL DEFAULT 0,   -- Sedang dikirim antar cabang
    qty_on_order    NUMERIC(20,4) NOT NULL DEFAULT 0,   -- Sudah di-PO, belum datang
    qty_consigned   NUMERIC(20,4) NOT NULL DEFAULT 0,   -- Stok konsinyasi (milik kita di luar)
    qty_loan        NUMERIC(20,4) NOT NULL DEFAULT 0,   -- Sedang dipinjam
    qty_scrap       NUMERIC(20,4) NOT NULL DEFAULT 0,   -- Menunggu write-off
    avg_cogs        NUMERIC(20,4) NOT NULL DEFAULT 0,   -- Weighted Average COGS
    last_cost       NUMERIC(20,4) NOT NULL DEFAULT 0,   -- Harga beli terakhir
    last_movement_at TIMESTAMPTZ,
    is_frozen       BOOLEAN NOT NULL DEFAULT FALSE,      -- Dibekukan saat cycle count
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(branch_id, product_id, COALESCE(variant_id, '00000000-0000-0000-0000-000000000000'::UUID), uom_id)
);

-- Batch Tracking
CREATE TABLE inventory.batches (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    product_id      UUID NOT NULL REFERENCES core.products(id),
    batch_no        VARCHAR(100) NOT NULL,
    manufacture_date DATE,
    expiry_date     DATE,
    supplier_id     UUID REFERENCES core.suppliers(id),
    po_id           UUID,
    qty_initial     NUMERIC(20,4) NOT NULL DEFAULT 0,
    qty_remaining   NUMERIC(20,4) NOT NULL DEFAULT 0,
    notes           TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, product_id, batch_no)
);
CREATE INDEX idx_batches_expiry ON inventory.batches(product_id, expiry_date) WHERE expiry_date IS NOT NULL;

-- Serial Number / IMEI Tracking
CREATE TABLE inventory.serial_numbers (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    product_id      UUID NOT NULL REFERENCES core.products(id),
    variant_id      UUID REFERENCES core.product_variants(id),
    serial_no       VARCHAR(200) NOT NULL,
    imei1           VARCHAR(20),
    imei2           VARCHAR(20),
    batch_id        UUID REFERENCES inventory.batches(id),
    current_branch_id UUID REFERENCES core.branches(id),
    bin_location_id UUID REFERENCES inventory.bin_locations(id),
    unit_grade      unit_grade NOT NULL DEFAULT 'NEW',
    status          VARCHAR(30) NOT NULL DEFAULT 'IN_STOCK', -- IN_STOCK, SOLD, LOAN, DISPLAY, RMA, SCRAP, TRANSIT
    purchase_po_id  UUID,
    purchase_date   DATE,
    purchase_price  NUMERIC(20,4),
    sale_id         UUID,
    sale_date       DATE,
    sale_price      NUMERIC(20,4),
    warranty_start  DATE,
    warranty_end    DATE,
    insurance_registered BOOLEAN NOT NULL DEFAULT FALSE,
    insurance_registered_at TIMESTAMPTZ,
    swap_origin_sn_id UUID REFERENCES inventory.serial_numbers(id), -- Untuk RMA unit swap
    metadata        HSTORE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, serial_no)
);
CREATE INDEX idx_sn_imei ON inventory.serial_numbers(tenant_id, imei1) WHERE imei1 IS NOT NULL;

-- Pergerakan Stok (Immutable Ledger)
CREATE TABLE inventory.stock_movements (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    branch_id       UUID NOT NULL REFERENCES core.branches(id),
    product_id      UUID NOT NULL REFERENCES core.products(id),
    variant_id      UUID REFERENCES core.product_variants(id),
    batch_id        UUID REFERENCES inventory.batches(id),
    serial_no_id    UUID REFERENCES inventory.serial_numbers(id),
    uom_id          UUID NOT NULL REFERENCES core.uom(id),
    movement_type   stock_movement_type NOT NULL,
    qty             NUMERIC(20,4) NOT NULL,               -- Positif = masuk, Negatif = keluar
    qty_before      NUMERIC(20,4) NOT NULL,
    qty_after       NUMERIC(20,4) NOT NULL,
    unit_cost       NUMERIC(20,4),
    total_cost      NUMERIC(20,4),
    reference_id    UUID NOT NULL,
    reference_type  VARCHAR(50) NOT NULL,
    reference_no    VARCHAR(100),
    bin_location_id UUID REFERENCES inventory.bin_locations(id),
    notes           TEXT,
    created_by      UUID REFERENCES core.users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_stock_movements_product ON inventory.stock_movements(branch_id, product_id, created_at);
CREATE INDEX idx_stock_movements_ref ON inventory.stock_movements(reference_type, reference_id);

-- ===========================================================
-- SECTION 10: PURCHASE ORDER
-- ===========================================================

CREATE TABLE inventory.purchase_orders (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    branch_id       UUID NOT NULL REFERENCES core.branches(id),
    po_no           VARCHAR(100) NOT NULL,
    supplier_id     UUID NOT NULL REFERENCES core.suppliers(id),
    status          po_status NOT NULL DEFAULT 'DRAFT',
    order_date      DATE NOT NULL,
    expected_date   DATE,
    currency_code   currency_code NOT NULL DEFAULT 'IDR',
    exchange_rate   NUMERIC(20,6) NOT NULL DEFAULT 1,
    subtotal        NUMERIC(20,4) NOT NULL DEFAULT 0,
    discount_amount NUMERIC(20,4) NOT NULL DEFAULT 0,
    tax_amount      NUMERIC(20,4) NOT NULL DEFAULT 0,
    other_cost      NUMERIC(20,4) NOT NULL DEFAULT 0,   -- Ongkos kirim, bea cukai
    total_amount    NUMERIC(20,4) NOT NULL DEFAULT 0,
    notes           TEXT,
    is_back_to_back BOOLEAN NOT NULL DEFAULT FALSE,      -- Special procurement
    customer_so_id  UUID,                               -- FK ke pos.sales_orders
    created_by      UUID REFERENCES core.users(id),
    approved_by     UUID REFERENCES core.users(id),
    approved_at     TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, po_no)
);

CREATE TABLE inventory.purchase_order_items (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    po_id           UUID NOT NULL REFERENCES inventory.purchase_orders(id) ON DELETE CASCADE,
    product_id      UUID NOT NULL REFERENCES core.products(id),
    variant_id      UUID REFERENCES core.product_variants(id),
    uom_id          UUID NOT NULL REFERENCES core.uom(id),
    qty_ordered     NUMERIC(20,4) NOT NULL,
    qty_received    NUMERIC(20,4) NOT NULL DEFAULT 0,
    unit_price      NUMERIC(20,4) NOT NULL,
    discount_pct    percentage NOT NULL DEFAULT 0,
    tax_code_id     UUID REFERENCES finance.tax_codes(id),
    line_total      NUMERIC(20,4) NOT NULL,
    notes           TEXT
);

-- Penerimaan Barang (Goods Receipt)
CREATE TABLE inventory.goods_receipts (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    branch_id       UUID NOT NULL REFERENCES core.branches(id),
    gr_no           VARCHAR(100) NOT NULL,
    po_id           UUID NOT NULL REFERENCES inventory.purchase_orders(id),
    receipt_date    DATE NOT NULL DEFAULT CURRENT_DATE,
    supplier_ref_no VARCHAR(100),
    notes           TEXT,
    status          VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    created_by      UUID REFERENCES core.users(id),
    confirmed_by    UUID REFERENCES core.users(id),
    confirmed_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, gr_no)
);

CREATE TABLE inventory.goods_receipt_items (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    gr_id           UUID NOT NULL REFERENCES inventory.goods_receipts(id) ON DELETE CASCADE,
    po_item_id      UUID NOT NULL REFERENCES inventory.purchase_order_items(id),
    product_id      UUID NOT NULL REFERENCES core.products(id),
    variant_id      UUID REFERENCES core.product_variants(id),
    batch_id        UUID REFERENCES inventory.batches(id),
    uom_id          UUID NOT NULL REFERENCES core.uom(id),
    qty_received    NUMERIC(20,4) NOT NULL,
    qty_good        NUMERIC(20,4) NOT NULL DEFAULT 0,
    qty_damaged     NUMERIC(20,4) NOT NULL DEFAULT 0,
    bin_location_id UUID REFERENCES inventory.bin_locations(id),
    unit_cost       NUMERIC(20,4) NOT NULL,
    landed_cost     NUMERIC(20,4) NOT NULL DEFAULT 0,    -- HPP landing setelah alokasi biaya
    notes           TEXT
);

-- Landed Cost Allocation
CREATE TABLE inventory.landed_cost_allocations (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    gr_id           UUID NOT NULL REFERENCES inventory.goods_receipts(id),
    cost_type       VARCHAR(50) NOT NULL,  -- FREIGHT, IMPORT_DUTY, INSURANCE, OTHER
    amount          NUMERIC(20,4) NOT NULL,
    allocation_method VARCHAR(20) NOT NULL DEFAULT 'VALUE', -- VALUE, WEIGHT, QTY
    notes           TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ===========================================================
-- SECTION 11: STOCK TRANSFER ANTAR CABANG
-- ===========================================================

CREATE TABLE inventory.stock_transfers (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    transfer_no     VARCHAR(100) NOT NULL,
    from_branch_id  UUID NOT NULL REFERENCES core.branches(id),
    to_branch_id    UUID NOT NULL REFERENCES core.branches(id),
    status          VARCHAR(30) NOT NULL DEFAULT 'DRAFT', -- DRAFT, SENT, IN_TRANSIT, RECEIVED, CANCELLED
    transfer_date   DATE NOT NULL DEFAULT CURRENT_DATE,
    expected_arrival DATE,
    actual_arrival  DATE,
    shipping_cost   NUMERIC(20,4),
    notes           TEXT,
    created_by      UUID REFERENCES core.users(id),
    sent_by         UUID REFERENCES core.users(id),
    sent_at         TIMESTAMPTZ,
    received_by     UUID REFERENCES core.users(id),
    received_at     TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, transfer_no)
);

CREATE TABLE inventory.stock_transfer_items (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    transfer_id     UUID NOT NULL REFERENCES inventory.stock_transfers(id) ON DELETE CASCADE,
    product_id      UUID NOT NULL REFERENCES core.products(id),
    variant_id      UUID REFERENCES core.product_variants(id),
    batch_id        UUID REFERENCES inventory.batches(id),
    serial_no_id    UUID REFERENCES inventory.serial_numbers(id),
    uom_id          UUID NOT NULL REFERENCES core.uom(id),
    qty_sent        NUMERIC(20,4) NOT NULL,
    qty_received    NUMERIC(20,4) NOT NULL DEFAULT 0,
    unit_cost       NUMERIC(20,4),
    notes           TEXT
);

-- ===========================================================
-- SECTION 12: STOK OPNAME
-- ===========================================================

CREATE TABLE inventory.stock_takes (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    branch_id       UUID NOT NULL REFERENCES core.branches(id),
    take_no         VARCHAR(100) NOT NULL,
    take_type       VARCHAR(30) NOT NULL DEFAULT 'FULL', -- FULL, PARTIAL, CYCLE_COUNT, SPOT_CHECK
    status          VARCHAR(30) NOT NULL DEFAULT 'DRAFT', -- DRAFT, IN_PROGRESS, COMPLETED, APPROVED
    scheduled_at    TIMESTAMPTZ,
    started_at      TIMESTAMPTZ,
    completed_at    TIMESTAMPTZ,
    approved_by     UUID REFERENCES core.users(id),
    approved_at     TIMESTAMPTZ,
    notes           TEXT,
    created_by      UUID REFERENCES core.users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, take_no)
);

CREATE TABLE inventory.stock_take_items (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    stock_take_id   UUID NOT NULL REFERENCES inventory.stock_takes(id) ON DELETE CASCADE,
    product_id      UUID NOT NULL REFERENCES core.products(id),
    variant_id      UUID REFERENCES core.product_variants(id),
    uom_id          UUID NOT NULL REFERENCES core.uom(id),
    system_qty      NUMERIC(20,4),           -- NULL = blind opname (staf tidak lihat)
    counted_qty     NUMERIC(20,4),           -- Diisi staf saat hitung
    difference      NUMERIC(20,4),           -- Auto-calculated: counted - system
    unit_cost       NUMERIC(20,4),
    difference_value NUMERIC(20,4),          -- difference * unit_cost
    notes           TEXT,
    counted_by      UUID REFERENCES core.users(id),
    counted_at      TIMESTAMPTZ
);

-- Stock Adjustment
CREATE TABLE inventory.stock_adjustments (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    branch_id       UUID NOT NULL REFERENCES core.branches(id),
    adj_no          VARCHAR(100) NOT NULL,
    stock_take_id   UUID REFERENCES inventory.stock_takes(id),
    adj_type        VARCHAR(30) NOT NULL DEFAULT 'MANUAL', -- OPNAME, MANUAL, SHRINKAGE, SCRAP, WRITE_OFF
    total_value     NUMERIC(20,4) NOT NULL DEFAULT 0,
    notes           TEXT,
    document_url    TEXT,                    -- Bukti pendukung
    status          approval_status NOT NULL DEFAULT 'PENDING',
    requested_by    UUID REFERENCES core.users(id),
    approved_by     UUID REFERENCES core.users(id),
    approved_at     TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, adj_no)
);

CREATE TABLE inventory.stock_adjustment_items (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    adjustment_id   UUID NOT NULL REFERENCES inventory.stock_adjustments(id) ON DELETE CASCADE,
    product_id      UUID NOT NULL REFERENCES core.products(id),
    variant_id      UUID REFERENCES core.product_variants(id),
    batch_id        UUID REFERENCES inventory.batches(id),
    serial_no_id    UUID REFERENCES inventory.serial_numbers(id),
    uom_id          UUID NOT NULL REFERENCES core.uom(id),
    qty_adjust      NUMERIC(20,4) NOT NULL,   -- Positif = adjust in, Negatif = adjust out
    unit_cost       NUMERIC(20,4) NOT NULL,
    reason          TEXT
);

-- ===========================================================
-- SECTION 13: POINT OF SALE - TRANSAKSI PENJUALAN
-- ===========================================================

-- Sesi Kasir / Shift
CREATE TABLE pos.shifts (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    branch_id       UUID NOT NULL REFERENCES core.branches(id),
    shift_no        VARCHAR(100) NOT NULL,
    cashier_id      UUID NOT NULL REFERENCES core.users(id),
    supervisor_id   UUID REFERENCES core.users(id),
    opened_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    closed_at       TIMESTAMPTZ,
    opening_cash    NUMERIC(20,4) NOT NULL DEFAULT 0,
    closing_cash_system NUMERIC(20,4),       -- Total kas menurut sistem
    closing_cash_actual NUMERIC(20,4),       -- Kas fisik yang dihitung kasir (blind close)
    cash_difference NUMERIC(20,4),
    petty_cash_total NUMERIC(20,4) NOT NULL DEFAULT 0,
    status          VARCHAR(20) NOT NULL DEFAULT 'OPEN', -- OPEN, CLOSED
    late_open_minutes INT NOT NULL DEFAULT 0,
    handover_confirmed_by UUID REFERENCES core.users(id),
    notes           TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, shift_no)
);

-- Petty Cash
CREATE TABLE pos.petty_cash_entries (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    shift_id        UUID NOT NULL REFERENCES pos.shifts(id),
    amount          NUMERIC(20,4) NOT NULL,
    category        VARCHAR(100) NOT NULL, -- PARKING, WATER, CLEANING, OTHER
    description     TEXT,
    receipt_url     TEXT,
    requires_approval BOOLEAN NOT NULL DEFAULT FALSE,
    approved_by     UUID REFERENCES core.users(id),
    approved_at     TIMESTAMPTZ,
    created_by      UUID REFERENCES core.users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Transaksi Penjualan (Sales Header)
CREATE TABLE pos.sales (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    branch_id       UUID NOT NULL REFERENCES core.branches(id),
    shift_id        UUID NOT NULL REFERENCES pos.shifts(id),
    sale_no         VARCHAR(100) NOT NULL,
    sale_date       DATE NOT NULL DEFAULT CURRENT_DATE,
    sale_time       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    customer_id     UUID REFERENCES crm.customers(id),
    price_list_id   UUID REFERENCES core.price_lists(id),
    cashier_id      UUID NOT NULL REFERENCES core.users(id),
    sales_id        UUID REFERENCES core.users(id),         -- Sales person (untuk komisi)
    sale_type       VARCHAR(30) NOT NULL DEFAULT 'RETAIL',  -- RETAIL, WHOLESALE, ONLINE, EMPLOYEE
    is_online       BOOLEAN NOT NULL DEFAULT FALSE,
    channel         VARCHAR(50),                            -- TOKOPEDIA, SHOPEE, WEBSITE, WALK_IN
    -- Pricing breakdown (urutan hitung engine)
    subtotal_before_discount NUMERIC(20,4) NOT NULL DEFAULT 0,
    item_discount_total NUMERIC(20,4) NOT NULL DEFAULT 0,
    subtotal        NUMERIC(20,4) NOT NULL DEFAULT 0,       -- Setelah diskon item
    global_discount_pct percentage NOT NULL DEFAULT 0,
    global_discount_amount NUMERIC(20,4) NOT NULL DEFAULT 0,
    voucher_id      UUID REFERENCES pos.vouchers(id),
    voucher_discount NUMERIC(20,4) NOT NULL DEFAULT 0,
    manual_adjustment NUMERIC(20,4) NOT NULL DEFAULT 0,     -- Perlu PIN Supervisor
    manual_adj_approved_by UUID REFERENCES core.users(id),
    taxable_amount  NUMERIC(20,4) NOT NULL DEFAULT 0,
    tax_amount      NUMERIC(20,4) NOT NULL DEFAULT 0,
    rounding_amount NUMERIC(20,4) NOT NULL DEFAULT 0,
    grand_total     NUMERIC(20,4) NOT NULL DEFAULT 0,
    -- Points
    points_used     INT NOT NULL DEFAULT 0,
    points_used_value NUMERIC(20,4) NOT NULL DEFAULT 0,
    points_earned   INT NOT NULL DEFAULT 0,
    -- Status
    status          VARCHAR(30) NOT NULL DEFAULT 'OPEN',    -- OPEN, PAID, VOIDED, RETURNED
    is_posted       BOOLEAN NOT NULL DEFAULT FALSE,         -- Sudah masuk laporan keuangan?
    void_reason     TEXT,
    voided_by       UUID REFERENCES core.users(id),
    voided_at       TIMESTAMPTZ,
    notes           TEXT,
    receipt_print_count INT NOT NULL DEFAULT 0,
    offline_created BOOLEAN NOT NULL DEFAULT FALSE,         -- Dibuat saat offline?
    sync_status     sync_status NOT NULL DEFAULT 'SYNCED',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, sale_no)
);
CREATE INDEX idx_sales_date ON pos.sales(branch_id, sale_date);
CREATE INDEX idx_sales_customer ON pos.sales(customer_id) WHERE customer_id IS NOT NULL;

-- Item Penjualan
CREATE TABLE pos.sale_items (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sale_id         UUID NOT NULL REFERENCES pos.sales(id) ON DELETE CASCADE,
    product_id      UUID NOT NULL REFERENCES core.products(id),
    variant_id      UUID REFERENCES core.product_variants(id),
    batch_id        UUID REFERENCES inventory.batches(id),
    serial_no_id    UUID REFERENCES inventory.serial_numbers(id),
    uom_id          UUID NOT NULL REFERENCES core.uom(id),
    qty             NUMERIC(20,4) NOT NULL,
    base_price      NUMERIC(20,4) NOT NULL,
    item_discount_pct percentage NOT NULL DEFAULT 0,
    item_discount_amount NUMERIC(20,4) NOT NULL DEFAULT 0,
    unit_price_after_discount NUMERIC(20,4) NOT NULL,
    tax_code_id     UUID REFERENCES finance.tax_codes(id),
    tax_rate        percentage NOT NULL DEFAULT 0,
    tax_amount      NUMERIC(20,4) NOT NULL DEFAULT 0,
    subtotal        NUMERIC(20,4) NOT NULL,
    promo_id        UUID REFERENCES pos.promotions(id),
    cogs            NUMERIC(20,4) NOT NULL DEFAULT 0,       -- HPP saat terjual
    is_gift         BOOLEAN NOT NULL DEFAULT FALSE,         -- GWP = true
    commission_sales_id UUID REFERENCES core.users(id),
    commission_amount NUMERIC(20,4),
    sort_order      INT NOT NULL DEFAULT 0,
    notes           TEXT
);

-- Pembayaran Multi-Metode
CREATE TABLE pos.sale_payments (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sale_id         UUID NOT NULL REFERENCES pos.sales(id) ON DELETE CASCADE,
    payment_method  payment_method_type NOT NULL,
    amount          NUMERIC(20,4) NOT NULL,
    reference_no    VARCHAR(200),                           -- No. ref EDC, Transfer
    bank_name       VARCHAR(100),
    card_last4      VARCHAR(4),
    is_verified     BOOLEAN NOT NULL DEFAULT TRUE,
    verified_by     UUID REFERENCES core.users(id),
    verified_at     TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ===========================================================
-- SECTION 14: VOID & RETURN
-- ===========================================================

-- Return / Retur Penjualan
CREATE TABLE pos.sales_returns (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    branch_id       UUID NOT NULL REFERENCES core.branches(id),
    return_no       VARCHAR(100) NOT NULL,
    original_sale_id UUID NOT NULL REFERENCES pos.sales(id),
    customer_id     UUID REFERENCES crm.customers(id),
    return_date     DATE NOT NULL DEFAULT CURRENT_DATE,
    return_type     VARCHAR(30) NOT NULL DEFAULT 'RETURN', -- RETURN (retur setelah transaksi)
    refund_method   VARCHAR(30) NOT NULL DEFAULT 'STORE_CREDIT', -- CASH, STORE_CREDIT, EXCHANGE
    total_refund    NUMERIC(20,4) NOT NULL DEFAULT 0,
    admin_fee       NUMERIC(20,4) NOT NULL DEFAULT 0,
    net_refund      NUMERIC(20,4) NOT NULL DEFAULT 0,
    status          approval_status NOT NULL DEFAULT 'PENDING',
    return_reason   TEXT NOT NULL,
    approved_by     UUID REFERENCES core.users(id),
    approved_at     TIMESTAMPTZ,
    processed_by    UUID REFERENCES core.users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, return_no)
);

CREATE TABLE pos.sales_return_items (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    return_id       UUID NOT NULL REFERENCES pos.sales_returns(id) ON DELETE CASCADE,
    sale_item_id    UUID NOT NULL REFERENCES pos.sale_items(id),
    product_id      UUID NOT NULL REFERENCES core.products(id),
    serial_no_id    UUID REFERENCES inventory.serial_numbers(id),
    qty_returned    NUMERIC(20,4) NOT NULL,
    unit_price      NUMERIC(20,4) NOT NULL,
    refund_amount   NUMERIC(20,4) NOT NULL,
    return_condition VARCHAR(30) NOT NULL DEFAULT 'GOOD', -- GOOD, DAMAGED, INCOMPLETE
    quarantine_location_id UUID REFERENCES inventory.bin_locations(id)
);

-- Purchase Return (Retur ke Supplier)
CREATE TABLE inventory.purchase_returns (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    branch_id       UUID NOT NULL REFERENCES core.branches(id),
    return_no       VARCHAR(100) NOT NULL,
    supplier_id     UUID NOT NULL REFERENCES core.suppliers(id),
    po_id           UUID REFERENCES inventory.purchase_orders(id),
    return_date     DATE NOT NULL DEFAULT CURRENT_DATE,
    total_amount    NUMERIC(20,4) NOT NULL DEFAULT 0,
    status          approval_status NOT NULL DEFAULT 'PENDING',
    reason          TEXT NOT NULL,
    ap_deducted     BOOLEAN NOT NULL DEFAULT FALSE,
    created_by      UUID REFERENCES core.users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, return_no)
);

CREATE TABLE inventory.purchase_return_items (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    purchase_return_id UUID NOT NULL REFERENCES inventory.purchase_returns(id),
    product_id      UUID NOT NULL REFERENCES core.products(id),
    serial_no_id    UUID REFERENCES inventory.serial_numbers(id),
    qty             NUMERIC(20,4) NOT NULL,
    unit_price      NUMERIC(20,4) NOT NULL,
    return_reason   TEXT
);

-- ===========================================================
-- SECTION 15: SERVICE CENTER & GARANSI (AFTER-SALES)
-- ===========================================================

CREATE TABLE pos.service_tickets (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    branch_id       UUID NOT NULL REFERENCES core.branches(id),
    ticket_no       VARCHAR(100) NOT NULL,
    customer_id     UUID NOT NULL REFERENCES crm.customers(id),
    product_id      UUID REFERENCES core.products(id),
    serial_no_id    UUID REFERENCES inventory.serial_numbers(id),
    imei            VARCHAR(20),
    complaint       TEXT NOT NULL,
    physical_condition TEXT,               -- Catatan kondisi fisik saat diterima
    accessories_received TEXT[],           -- ['charger', 'earphone', 'dus']
    is_warranty_claim BOOLEAN NOT NULL DEFAULT FALSE,
    warranty_end    DATE,
    status          service_status NOT NULL DEFAULT 'RECEIVED',
    assigned_tech_id UUID REFERENCES core.users(id),
    estimated_cost  NUMERIC(20,4),
    customer_approved_cost BOOLEAN,
    approved_cost_at TIMESTAMPTZ,
    final_cost      NUMERIC(20,4),
    completed_at    TIMESTAMPTZ,
    delivered_at    TIMESTAMPTZ,
    max_custody_days INT NOT NULL DEFAULT 30,  -- Batas lama penitipan
    custody_warning_sent BOOLEAN NOT NULL DEFAULT FALSE,
    notes           TEXT,
    created_by      UUID REFERENCES core.users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, ticket_no)
);

-- RMA - Return Merchandise Authorization ke Vendor/Pabrik
CREATE TABLE pos.rma_tickets (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    rma_no          VARCHAR(100) NOT NULL,
    service_ticket_id UUID REFERENCES pos.service_tickets(id),
    supplier_id     UUID NOT NULL REFERENCES core.suppliers(id),
    product_id      UUID NOT NULL REFERENCES core.products(id),
    serial_no_id    UUID REFERENCES inventory.serial_numbers(id),
    status          rma_status NOT NULL DEFAULT 'PENDING',
    sent_date       DATE,
    expected_return DATE,
    actual_return   DATE,
    replacement_sn_id UUID REFERENCES inventory.serial_numbers(id), -- Unit pengganti (swap)
    shipping_cost   NUMERIC(20,4),
    admin_cost      NUMERIC(20,4),
    notes           TEXT,
    created_by      UUID REFERENCES core.users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, rma_no)
);

-- Extended Warranty (Asuransi Tambahan)
CREATE TABLE pos.extended_warranties (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sale_item_id    UUID NOT NULL REFERENCES pos.sale_items(id),
    serial_no_id    UUID REFERENCES inventory.serial_numbers(id),
    provider        VARCHAR(200) NOT NULL,               -- Nama asuransi/vendor garansi
    policy_no       VARCHAR(100),
    premium_amount  NUMERIC(20,4) NOT NULL,
    start_date      DATE NOT NULL,
    end_date        DATE NOT NULL,
    is_registered   BOOLEAN NOT NULL DEFAULT FALSE,
    registered_at   TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ===========================================================
-- SECTION 16: ACCOUNTS RECEIVABLE / PAYABLE
-- ===========================================================

-- Piutang Pelanggan
CREATE TABLE finance.customer_invoices (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    branch_id       UUID NOT NULL REFERENCES core.branches(id),
    invoice_no      VARCHAR(100) NOT NULL,
    customer_id     UUID NOT NULL REFERENCES crm.customers(id),
    sale_id         UUID REFERENCES pos.sales(id),
    invoice_date    DATE NOT NULL,
    due_date        DATE NOT NULL,
    total_amount    NUMERIC(20,4) NOT NULL,
    paid_amount     NUMERIC(20,4) NOT NULL DEFAULT 0,
    outstanding     NUMERIC(20,4) NOT NULL DEFAULT 0,
    status          VARCHAR(30) NOT NULL DEFAULT 'OPEN', -- OPEN, PARTIAL, PAID, OVERDUE, WRITTEN_OFF
    term_of_payment VARCHAR(50),
    late_days       INT NOT NULL DEFAULT 0,
    penalty_rate    percentage NOT NULL DEFAULT 0,
    penalty_amount  NUMERIC(20,4) NOT NULL DEFAULT 0,
    health_score    INT NOT NULL DEFAULT 100,            -- AR Early Warning Score
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, invoice_no)
);

CREATE TABLE finance.invoice_payments (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    invoice_id      UUID NOT NULL REFERENCES finance.customer_invoices(id),
    payment_date    DATE NOT NULL,
    amount          NUMERIC(20,4) NOT NULL,
    payment_method  payment_method_type NOT NULL,
    reference_no    VARCHAR(200),
    notes           TEXT,
    created_by      UUID REFERENCES core.users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Hutang ke Supplier (Accounts Payable)
CREATE TABLE finance.supplier_invoices (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    supplier_id     UUID NOT NULL REFERENCES core.suppliers(id),
    po_id           UUID REFERENCES inventory.purchase_orders(id),
    invoice_no      VARCHAR(100) NOT NULL,
    supplier_inv_no VARCHAR(100),
    invoice_date    DATE NOT NULL,
    due_date        DATE NOT NULL,
    total_amount    NUMERIC(20,4) NOT NULL,
    paid_amount     NUMERIC(20,4) NOT NULL DEFAULT 0,
    outstanding     NUMERIC(20,4) NOT NULL DEFAULT 0,
    status          VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    reminder_sent_h7 BOOLEAN NOT NULL DEFAULT FALSE,
    reminder_sent_h1 BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, invoice_no)
);

CREATE TABLE finance.supplier_payments (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    supplier_invoice_id UUID NOT NULL REFERENCES finance.supplier_invoices(id),
    payment_date    DATE NOT NULL,
    amount          NUMERIC(20,4) NOT NULL,
    payment_method  payment_method_type NOT NULL,
    reference_no    VARCHAR(200),
    notes           TEXT,
    created_by      UUID REFERENCES core.users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Cicilan Internal Pelanggan
CREATE TABLE finance.installment_plans (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id     UUID NOT NULL REFERENCES crm.customers(id),
    sale_id         UUID REFERENCES pos.sales(id),
    total_amount    NUMERIC(20,4) NOT NULL,
    down_payment    NUMERIC(20,4) NOT NULL DEFAULT 0,
    installment_count INT NOT NULL,
    monthly_amount  NUMERIC(20,4) NOT NULL,
    interest_rate   percentage NOT NULL DEFAULT 0,
    penalty_rate    percentage NOT NULL DEFAULT 0,
    start_date      DATE NOT NULL,
    status          VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    approved_by     UUID REFERENCES core.users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE finance.installment_schedules (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    plan_id         UUID NOT NULL REFERENCES finance.installment_plans(id),
    period_no       INT NOT NULL,
    due_date        DATE NOT NULL,
    principal_amount NUMERIC(20,4) NOT NULL,
    interest_amount NUMERIC(20,4) NOT NULL DEFAULT 0,
    total_due       NUMERIC(20,4) NOT NULL,
    paid_amount     NUMERIC(20,4) NOT NULL DEFAULT 0,
    paid_at         TIMESTAMPTZ,
    penalty_amount  NUMERIC(20,4) NOT NULL DEFAULT 0,
    status          VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, PARTIAL, PAID, OVERDUE
    UNIQUE(plan_id, period_no)
);

-- Pre-Order / Down Payment
CREATE TABLE pos.preorders (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    branch_id       UUID NOT NULL REFERENCES core.branches(id),
    po_no           VARCHAR(100) NOT NULL,
    customer_id     UUID NOT NULL REFERENCES crm.customers(id),
    product_id      UUID NOT NULL REFERENCES core.products(id),
    variant_id      UUID REFERENCES core.product_variants(id),
    qty             NUMERIC(20,4) NOT NULL,
    agreed_price    NUMERIC(20,4) NOT NULL,
    dp_amount       NUMERIC(20,4) NOT NULL DEFAULT 0,
    dp_paid         NUMERIC(20,4) NOT NULL DEFAULT 0,
    remaining_amount NUMERIC(20,4) NOT NULL DEFAULT 0,
    cancel_policy   VARCHAR(30) NOT NULL DEFAULT 'FORFEIT', -- FORFEIT, PARTIAL_REFUND, FULL_REFUND
    cancel_pct      percentage NOT NULL DEFAULT 0,
    status          VARCHAR(30) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, READY, COMPLETED, CANCELLED
    estimated_arrival DATE,
    actual_arrival  DATE,
    sale_id         UUID REFERENCES pos.sales(id),          -- FK saat PO dikonversi ke sale
    cancelled_by    UUID REFERENCES core.users(id),
    cancel_reason   TEXT,
    created_by      UUID REFERENCES core.users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, po_no)
);

-- Bank Reconciliation
CREATE TABLE finance.bank_accounts (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id      UUID NOT NULL REFERENCES core.companies(id),
    bank_name       VARCHAR(100) NOT NULL,
    account_no      VARCHAR(50) NOT NULL,
    account_name    VARCHAR(200) NOT NULL,
    currency_code   currency_code NOT NULL DEFAULT 'IDR',
    coa_id          UUID REFERENCES finance.chart_of_accounts(id),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE(company_id, account_no)
);

CREATE TABLE finance.bank_statements (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    bank_account_id UUID NOT NULL REFERENCES finance.bank_accounts(id),
    transaction_date DATE NOT NULL,
    description     TEXT NOT NULL,
    debit           NUMERIC(20,4) NOT NULL DEFAULT 0,
    credit          NUMERIC(20,4) NOT NULL DEFAULT 0,
    balance         NUMERIC(20,4) NOT NULL,
    reference_no    VARCHAR(200),
    is_reconciled   BOOLEAN NOT NULL DEFAULT FALSE,
    matched_sale_id UUID REFERENCES pos.sales(id),
    matched_supplier_payment_id UUID REFERENCES finance.supplier_payments(id),
    uploaded_batch  VARCHAR(100),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Cash In Transit (Setoran Bank)
CREATE TABLE finance.cash_in_transit (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    branch_id       UUID NOT NULL REFERENCES core.branches(id),
    shift_id        UUID REFERENCES pos.shifts(id),
    amount          NUMERIC(20,4) NOT NULL,
    status          VARCHAR(30) NOT NULL DEFAULT 'HANDED_OVER', -- HANDED_OVER, IN_TRANSIT, DEPOSITED, RECONCILED
    handed_by       UUID REFERENCES core.users(id),
    handed_to       UUID REFERENCES core.users(id),
    handed_at       TIMESTAMPTZ,
    deposit_slip_url TEXT,
    bank_account_id UUID REFERENCES finance.bank_accounts(id),
    deposited_amount NUMERIC(20,4),
    difference      NUMERIC(20,4),
    notes           TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ===========================================================
-- SECTION 17: HR & ABSENSI
-- ===========================================================

CREATE TABLE hr.employees (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    branch_id       UUID NOT NULL REFERENCES core.branches(id),
    employee_no     VARCHAR(50) NOT NULL,
    user_id         UUID REFERENCES core.users(id),
    full_name       VARCHAR(200) NOT NULL,
    nik             VARCHAR(20),                          -- NIK KTP
    npwp            VARCHAR(20),
    gender          gender_type,
    date_of_birth   DATE,
    phone           phone_number,
    email           VARCHAR(200),
    address         TEXT,
    join_date       DATE NOT NULL,
    resignation_date DATE,
    position        VARCHAR(100) NOT NULL,
    department      VARCHAR(100),
    base_salary     NUMERIC(20,4) NOT NULL DEFAULT 0,
    bank_account_no VARCHAR(50),
    bank_name       VARCHAR(100),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, employee_no)
);

CREATE TABLE hr.shifts_schedule (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    branch_id       UUID NOT NULL REFERENCES core.branches(id),
    employee_id     UUID NOT NULL REFERENCES hr.employees(id),
    schedule_date   DATE NOT NULL,
    shift_name      VARCHAR(50) NOT NULL,
    expected_start  TIME NOT NULL,
    expected_end    TIME NOT NULL,
    UNIQUE(branch_id, employee_id, schedule_date)
);

CREATE TABLE hr.attendance (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id     UUID NOT NULL REFERENCES hr.employees(id),
    schedule_id     UUID REFERENCES hr.shifts_schedule(id),
    clock_in        TIMESTAMPTZ,
    clock_out       TIMESTAMPTZ,
    is_late         BOOLEAN NOT NULL DEFAULT FALSE,
    late_minutes    INT NOT NULL DEFAULT 0,
    shift_opened    BOOLEAN NOT NULL DEFAULT FALSE,      -- Apakah berhasil buka shift POS?
    pos_shift_id    UUID REFERENCES pos.shifts(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Komisi & Insentif
CREATE TABLE hr.commission_rules (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    product_id      UUID REFERENCES core.products(id),   -- NULL = semua produk
    category_id     UUID REFERENCES core.categories(id),
    promo_id        UUID REFERENCES pos.promotions(id),  -- Push-selling
    commission_pct  percentage NOT NULL DEFAULT 0,
    commission_fixed NUMERIC(20,4),
    min_price       NUMERIC(20,4),
    split_rules     JSONB,  -- {sales: 60, cashier: 40} - pembagian %
    valid_from      DATE NOT NULL,
    valid_to        DATE,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE hr.commission_ledger (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id     UUID NOT NULL REFERENCES hr.employees(id),
    sale_item_id    UUID REFERENCES pos.sale_items(id),
    commission_rule_id UUID REFERENCES hr.commission_rules(id),
    gross_amount    NUMERIC(20,4) NOT NULL,
    status          VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, FINAL, CLAWBACK, PAID
    clawback_at     TIMESTAMPTZ,                           -- Saat retur terjadi
    finalized_at    TIMESTAMPTZ,
    paid_at         TIMESTAMPTZ,
    payout_batch_id UUID,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE hr.sales_targets (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id     UUID NOT NULL REFERENCES hr.employees(id),
    period_year     SMALLINT NOT NULL,
    period_month    SMALLINT NOT NULL,
    target_amount   NUMERIC(20,4) NOT NULL,
    target_transactions INT,
    achieved_amount NUMERIC(20,4) NOT NULL DEFAULT 0,
    achieved_transactions INT NOT NULL DEFAULT 0,
    achievement_pct percentage,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(employee_id, period_year, period_month)
);

-- Employee Purchase Program
CREATE TABLE hr.employee_purchases (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id     UUID NOT NULL REFERENCES hr.employees(id),
    sale_id         UUID NOT NULL REFERENCES pos.sales(id),
    period_year     SMALLINT NOT NULL,
    period_month    SMALLINT NOT NULL,
    purchase_amount NUMERIC(20,4) NOT NULL,
    monthly_limit   NUMERIC(20,4) NOT NULL,
    is_payroll_deduct BOOLEAN NOT NULL DEFAULT FALSE,
    lock_in_until   DATE,                               -- Tidak boleh dijual kembali sampai
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ===========================================================
-- SECTION 18: LOGISTICS & PENGIRIMAN
-- ===========================================================

CREATE TABLE logistics.deliveries (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    branch_id       UUID NOT NULL REFERENCES core.branches(id),
    delivery_no     VARCHAR(100) NOT NULL,
    sale_id         UUID NOT NULL REFERENCES pos.sales(id),
    customer_id     UUID NOT NULL REFERENCES crm.customers(id),
    status          delivery_status NOT NULL DEFAULT 'PACKED',
    recipient_name  VARCHAR(200),
    recipient_phone phone_number,
    delivery_address TEXT NOT NULL,
    city            VARCHAR(100),
    courier_type    VARCHAR(30) NOT NULL DEFAULT 'INTERNAL', -- INTERNAL, JNE, JT, GRAB
    courier_id      UUID REFERENCES hr.employees(id),
    tracking_no     VARCHAR(100),
    shipping_cost   NUMERIC(20,4) NOT NULL DEFAULT 0,
    weight_gram     NUMERIC(10,2),
    dimension_cm    JSONB,
    packed_at       TIMESTAMPTZ,
    shipped_at      TIMESTAMPTZ,
    delivered_at    TIMESTAMPTZ,
    delivery_proof_url TEXT,
    receiver_signature_url TEXT,
    rto_reason      TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, delivery_no)
);

CREATE TABLE logistics.delivery_items (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    delivery_id     UUID NOT NULL REFERENCES logistics.deliveries(id),
    sale_item_id    UUID NOT NULL REFERENCES pos.sale_items(id),
    product_id      UUID NOT NULL REFERENCES core.products(id),
    serial_no_id    UUID REFERENCES inventory.serial_numbers(id),
    qty             NUMERIC(20,4) NOT NULL
);

-- ===========================================================
-- SECTION 19: KONSINYASI
-- ===========================================================

-- Konsinyasi Masuk (Barang titipan orang lain di toko kita)
CREATE TABLE inventory.consignments (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    branch_id       UUID NOT NULL REFERENCES core.branches(id),
    consignment_no  VARCHAR(100) NOT NULL,
    consignment_type consignment_type NOT NULL, -- IN (orang lain titip ke kita), OUT (kita titip ke orang lain)
    partner_id      UUID REFERENCES core.suppliers(id),       -- Supplier sebagai pemilik barang
    partner_name    VARCHAR(200),                              -- Nama pemilik jika bukan supplier
    start_date      DATE NOT NULL,
    end_date        DATE,
    settlement_day  INT NOT NULL DEFAULT 25,                  -- Tanggal settlement setiap bulan
    commission_pct  percentage NOT NULL DEFAULT 0,            -- Komisi toko dari barang konsinyasi
    status          VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, consignment_no)
);

CREATE TABLE inventory.consignment_items (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    consignment_id  UUID NOT NULL REFERENCES inventory.consignments(id),
    product_id      UUID NOT NULL REFERENCES core.products(id),
    qty_entrusted   NUMERIC(20,4) NOT NULL,
    qty_sold        NUMERIC(20,4) NOT NULL DEFAULT 0,
    qty_remaining   NUMERIC(20,4) NOT NULL DEFAULT 0,
    selling_price   NUMERIC(20,4) NOT NULL,
    consignor_price NUMERIC(20,4) NOT NULL,                   -- Harga ke pemilik per unit
    total_sales_value NUMERIC(20,4) NOT NULL DEFAULT 0,
    total_payable   NUMERIC(20,4) NOT NULL DEFAULT 0          -- Yang harus dibayar ke pemilik
);

-- Settlement Konsinyasi
CREATE TABLE inventory.consignment_settlements (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    consignment_id  UUID NOT NULL REFERENCES inventory.consignments(id),
    period_start    DATE NOT NULL,
    period_end      DATE NOT NULL,
    total_sold_qty  NUMERIC(20,4) NOT NULL DEFAULT 0,
    total_sold_value NUMERIC(20,4) NOT NULL DEFAULT 0,
    commission_amount NUMERIC(20,4) NOT NULL DEFAULT 0,
    net_payable     NUMERIC(20,4) NOT NULL DEFAULT 0,
    status          VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, INVOICED, PAID
    paid_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ===========================================================
-- SECTION 20: PEMINJAMAN BARANG (LOAN)
-- ===========================================================

CREATE TABLE inventory.loan_documents (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    branch_id       UUID NOT NULL REFERENCES core.branches(id),
    loan_no         VARCHAR(100) NOT NULL,
    loan_type       VARCHAR(30) NOT NULL DEFAULT 'INTERNAL', -- INTERNAL, DEMO, EXHIBITION
    product_id      UUID NOT NULL REFERENCES core.products(id),
    serial_no_id    UUID REFERENCES inventory.serial_numbers(id),
    qty             NUMERIC(20,4) NOT NULL DEFAULT 1,
    loaned_to       VARCHAR(200),                           -- Nama peminjam
    purpose         TEXT NOT NULL,
    loan_date       DATE NOT NULL DEFAULT CURRENT_DATE,
    expected_return_date DATE NOT NULL,
    actual_return_date DATE,
    status          loan_status NOT NULL DEFAULT 'ACTIVE',
    converted_to_sale_id UUID REFERENCES pos.sales(id),
    notes           TEXT,
    created_by      UUID REFERENCES core.users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, loan_no)
);

-- ===========================================================
-- SECTION 21: ASET INTERNAL
-- ===========================================================

CREATE TABLE core.assets (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    branch_id       UUID NOT NULL REFERENCES core.branches(id),
    asset_no        VARCHAR(100) NOT NULL,
    name            VARCHAR(200) NOT NULL,
    category        VARCHAR(100) NOT NULL,  -- PC, PRINTER, AC, FURNITURE
    serial_no       VARCHAR(200),
    purchase_date   DATE,
    purchase_cost   NUMERIC(20,4) NOT NULL DEFAULT 0,
    useful_life_months INT NOT NULL DEFAULT 60,
    depreciation_method VARCHAR(30) NOT NULL DEFAULT 'STRAIGHT_LINE',
    monthly_depreciation NUMERIC(20,4) NOT NULL DEFAULT 0,
    current_book_value NUMERIC(20,4) NOT NULL DEFAULT 0,
    status          asset_status NOT NULL DEFAULT 'ACTIVE',
    last_maintenance_date DATE,
    next_maintenance_date DATE,
    maintenance_interval_months INT NOT NULL DEFAULT 3,
    coa_asset_id    UUID REFERENCES finance.chart_of_accounts(id),
    coa_depreciation_id UUID REFERENCES finance.chart_of_accounts(id),
    notes           TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, asset_no)
);

CREATE TABLE core.asset_maintenance (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    asset_id        UUID NOT NULL REFERENCES core.assets(id),
    maintenance_date DATE NOT NULL,
    description     TEXT NOT NULL,
    cost            NUMERIC(20,4) NOT NULL DEFAULT 0,
    vendor          VARCHAR(200),
    performed_by    UUID REFERENCES core.users(id),
    next_due        DATE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ===========================================================
-- SECTION 22: NOTIFIKASI & KOMUNIKASI
-- ===========================================================

CREATE TABLE core.notification_templates (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    code            VARCHAR(100) NOT NULL,
    name            VARCHAR(200) NOT NULL,
    trigger_event   VARCHAR(100) NOT NULL,  -- SALE_COMPLETE, BIRTHDAY, POINT_EXPIRY, etc.
    channel         VARCHAR(20) NOT NULL DEFAULT 'WHATSAPP', -- WHATSAPP, EMAIL, PUSH
    subject         VARCHAR(500),                           -- Untuk email
    body_template   TEXT NOT NULL,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE(tenant_id, code)
);

CREATE TABLE core.notification_logs (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    template_id     UUID REFERENCES core.notification_templates(id),
    recipient_type  VARCHAR(30) NOT NULL,   -- CUSTOMER, STAFF, MANAGER
    recipient_id    UUID NOT NULL,
    channel         VARCHAR(20) NOT NULL,
    recipient_address VARCHAR(200) NOT NULL, -- Nomor WA / email
    subject         VARCHAR(500),
    body            TEXT NOT NULL,
    status          VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, SENT, FAILED, DELIVERED
    sent_at         TIMESTAMPTZ,
    delivered_at    TIMESTAMPTZ,
    error_message   TEXT,
    retry_count     INT NOT NULL DEFAULT 0,
    fallback_channel VARCHAR(20),           -- Channel cadangan jika gagal
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Queue Management (Antrean Layanan)
CREATE TABLE pos.service_queues (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    branch_id       UUID NOT NULL REFERENCES core.branches(id),
    queue_no        VARCHAR(20) NOT NULL,
    customer_id     UUID REFERENCES crm.customers(id),
    customer_name   VARCHAR(200),
    customer_phone  phone_number,
    service_type    VARCHAR(50) NOT NULL,   -- SERVICE, CONSULTATION, CASHIER
    status          queue_status NOT NULL DEFAULT 'WAITING',
    assigned_to     UUID REFERENCES core.users(id),
    called_at       TIMESTAMPTZ,
    started_at      TIMESTAMPTZ,
    completed_at    TIMESTAMPTZ,
    wait_minutes    INT,
    service_minutes INT,
    csat_score      SMALLINT CHECK (csat_score BETWEEN 1 AND 5),
    csat_submitted_at TIMESTAMPTZ,
    notes           TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ===========================================================
-- SECTION 23: AUDIT TRAIL & KEAMANAN
-- ===========================================================

CREATE TABLE audit.audit_logs (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL,
    branch_id       UUID,
    user_id         UUID,
    action          VARCHAR(100) NOT NULL,   -- CREATE, UPDATE, DELETE, APPROVE, VOID, LOGIN, etc.
    module          VARCHAR(50) NOT NULL,
    resource_type   VARCHAR(100) NOT NULL,
    resource_id     UUID,
    data_before     JSONB,
    data_after      JSONB,
    ip_address      INET,
    user_agent      TEXT,
    session_id      UUID,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
) PARTITION BY RANGE (created_at);

-- Partisi audit log per bulan
CREATE TABLE audit.audit_logs_2026_01 PARTITION OF audit.audit_logs
    FOR VALUES FROM ('2026-01-01') TO ('2026-02-01');
CREATE TABLE audit.audit_logs_2026_02 PARTITION OF audit.audit_logs
    FOR VALUES FROM ('2026-02-01') TO ('2026-03-01');
-- (partisi tambahan dibuat via script deployment otomatis setiap awal bulan)

CREATE INDEX idx_audit_logs_tenant ON audit.audit_logs(tenant_id, created_at);
CREATE INDEX idx_audit_logs_user ON audit.audit_logs(user_id, created_at);
CREATE INDEX idx_audit_logs_resource ON audit.audit_logs(resource_type, resource_id);

-- Fraud Detection Alerts
CREATE TABLE audit.fraud_alerts (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    branch_id       UUID REFERENCES core.branches(id),
    alert_type      fraud_alert_type NOT NULL,
    severity        VARCHAR(20) NOT NULL DEFAULT 'MEDIUM', -- LOW, MEDIUM, HIGH, CRITICAL
    user_id         UUID REFERENCES core.users(id),
    customer_id     UUID REFERENCES crm.customers(id),
    reference_id    UUID,
    reference_type  VARCHAR(50),
    description     TEXT NOT NULL,
    is_acknowledged BOOLEAN NOT NULL DEFAULT FALSE,
    acknowledged_by UUID REFERENCES core.users(id),
    acknowledged_at TIMESTAMPTZ,
    resolved        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Document Access Log (Privacy)
CREATE TABLE audit.document_access_logs (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL,
    user_id         UUID NOT NULL REFERENCES core.users(id),
    document_type   VARCHAR(100) NOT NULL,  -- CUSTOMER_DATABASE, FINANCIAL_REPORT, TRANSACTION_DETAIL
    document_id     UUID,
    action          VARCHAR(30) NOT NULL,   -- VIEW, DOWNLOAD, EXPORT
    ip_address      INET,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Receipt Print Log (Anti-Ghosting)
CREATE TABLE audit.receipt_prints (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sale_id         UUID NOT NULL REFERENCES pos.sales(id),
    print_no        INT NOT NULL,           -- Nomor urut cetak unik
    printed_by      UUID NOT NULL REFERENCES core.users(id),
    printed_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_reprint      BOOLEAN NOT NULL DEFAULT FALSE,
    reprint_reason  TEXT
);

-- ===========================================================
-- SECTION 24: ANALYTICS & BUSINESS INTELLIGENCE
-- ===========================================================

-- Heatmap Penjualan (Materialized, di-refresh periodik)
CREATE TABLE analytics.sales_heatmap (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    branch_id       UUID NOT NULL REFERENCES core.branches(id),
    day_of_week     SMALLINT NOT NULL CHECK (day_of_week BETWEEN 0 AND 6),
    hour_of_day     SMALLINT NOT NULL CHECK (hour_of_day BETWEEN 0 AND 23),
    avg_transaction_count NUMERIC(10,2),
    avg_revenue     NUMERIC(20,4),
    period_start    DATE NOT NULL,
    period_end      DATE NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(branch_id, day_of_week, hour_of_day, period_start)
);

-- Dead Stock Tracking
CREATE TABLE analytics.dead_stock_report (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    branch_id       UUID NOT NULL REFERENCES core.branches(id),
    product_id      UUID NOT NULL REFERENCES core.products(id),
    last_sold_date  DATE,
    days_no_movement INT NOT NULL DEFAULT 0,
    qty_on_hand     NUMERIC(20,4) NOT NULL DEFAULT 0,
    total_value     NUMERIC(20,4) NOT NULL DEFAULT 0,
    recommended_discount_pct percentage,
    status          VARCHAR(30) NOT NULL DEFAULT 'NORMAL', -- NORMAL, SLOW_MOVING, DEAD_STOCK
    auto_markdown_applied BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(branch_id, product_id)
);

-- Out-of-Stock Prediction
CREATE TABLE analytics.stock_predictions (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    branch_id       UUID NOT NULL REFERENCES core.branches(id),
    product_id      UUID NOT NULL REFERENCES core.products(id),
    avg_daily_sales NUMERIC(20,4),
    predicted_oos_date DATE,
    confidence_score percentage,
    recommended_reorder_date DATE,
    recommended_qty NUMERIC(20,4),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(branch_id, product_id)
);

-- Market Basket Analysis
CREATE TABLE analytics.product_affinity (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    product_a_id    UUID NOT NULL REFERENCES core.products(id),
    product_b_id    UUID NOT NULL REFERENCES core.products(id),
    co_purchase_count INT NOT NULL DEFAULT 0,
    lift_score      NUMERIC(10,4),
    confidence_score percentage,
    period_start    DATE NOT NULL,
    period_end      DATE NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, product_a_id, product_b_id, period_start)
);

-- ===========================================================
-- SECTION 25: SYSTEM CONFIG & OFFLINE SYNC
-- ===========================================================

CREATE TABLE core.system_settings (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    setting_key     VARCHAR(200) NOT NULL,
    setting_value   TEXT NOT NULL,
    value_type      VARCHAR(20) NOT NULL DEFAULT 'STRING', -- STRING, INTEGER, BOOLEAN, JSON
    description     TEXT,
    is_sensitive    BOOLEAN NOT NULL DEFAULT FALSE,
    updated_by      UUID REFERENCES core.users(id),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, setting_key)
);

-- Terminal Kasir
CREATE TABLE core.pos_terminals (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    branch_id       UUID NOT NULL REFERENCES core.branches(id),
    terminal_code   VARCHAR(50) NOT NULL,
    name            VARCHAR(100) NOT NULL,
    device_info     JSONB,
    last_heartbeat  TIMESTAMPTZ,
    is_online       BOOLEAN NOT NULL DEFAULT FALSE,
    last_sync_at    TIMESTAMPTZ,
    sync_status     sync_status NOT NULL DEFAULT 'SYNCED',
    offline_since   TIMESTAMPTZ,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(branch_id, terminal_code)
);

-- Offline Transaction Queue
CREATE TABLE core.offline_queue (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    terminal_id     UUID NOT NULL REFERENCES core.pos_terminals(id),
    payload         JSONB NOT NULL,
    operation_type  VARCHAR(50) NOT NULL,    -- CREATE_SALE, UPDATE_STOCK, etc.
    local_id        VARCHAR(100) NOT NULL,
    status          VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, SYNCED, CONFLICT, FAILED
    conflict_reason TEXT,
    retry_count     INT NOT NULL DEFAULT 0,
    synced_at       TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ===========================================================
-- SECTION 26: TAMBAHAN MODUL SPESIFIK
-- ===========================================================

-- Trade-In (Tukar Tambah)
CREATE TABLE pos.trade_in_assessments (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    branch_id       UUID NOT NULL REFERENCES core.branches(id),
    assessment_no   VARCHAR(100) NOT NULL,
    customer_id     UUID REFERENCES crm.customers(id),
    product_id      UUID NOT NULL REFERENCES core.products(id),
    serial_no       VARCHAR(200),
    grade           VARCHAR(20) NOT NULL,               -- GOOD, FAIR, POOR, BROKEN
    checklist       JSONB NOT NULL,                     -- {screen: true, battery: false, signal: true}
    appraised_value NUMERIC(20,4) NOT NULL,
    status          VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, ACCEPTED, REJECTED, CONVERTED
    converted_to_sale_id UUID REFERENCES pos.sales(id),
    appraised_by    UUID REFERENCES core.users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, assessment_no)
);

-- Stock Reservation
CREATE TABLE inventory.stock_reservations (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    branch_id       UUID NOT NULL REFERENCES core.branches(id),
    product_id      UUID NOT NULL REFERENCES core.products(id),
    variant_id      UUID REFERENCES core.product_variants(id),
    qty_reserved    NUMERIC(20,4) NOT NULL,
    reservation_type VARCHAR(30) NOT NULL, -- VIP, QUOTATION, PREORDER, ONLINE
    reference_id    UUID NOT NULL,
    customer_id     UUID REFERENCES crm.customers(id),
    reserved_until  TIMESTAMPTZ NOT NULL,
    is_released     BOOLEAN NOT NULL DEFAULT FALSE,
    released_at     TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Purchase Limits (Anti Borong)
CREATE TABLE pos.purchase_limits (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    promotion_id    UUID REFERENCES pos.promotions(id),
    product_id      UUID REFERENCES core.products(id),
    limit_per_customer INT NOT NULL DEFAULT 1,
    limit_per_branch INT,
    limit_period    VARCHAR(20) NOT NULL DEFAULT 'DAY', -- DAY, PROMO_PERIOD
    is_active       BOOLEAN NOT NULL DEFAULT TRUE
);

-- Shelf Label Verification
CREATE TABLE inventory.shelf_label_queue (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    branch_id       UUID NOT NULL REFERENCES core.branches(id),
    product_id      UUID NOT NULL REFERENCES core.products(id),
    variant_id      UUID REFERENCES core.product_variants(id),
    current_shelf_price NUMERIC(20,4) NOT NULL,
    system_price    NUMERIC(20,4) NOT NULL,
    is_discrepancy  BOOLEAN NOT NULL DEFAULT FALSE,
    print_status    VARCHAR(30) NOT NULL DEFAULT 'QUEUED', -- QUEUED, PRINTED, VERIFIED
    printed_at      TIMESTAMPTZ,
    verified_by     UUID REFERENCES core.users(id),
    verified_at     TIMESTAMPTZ,
    added_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Expense Tracking (Biaya Operasional)
CREATE TABLE finance.expenses (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    branch_id       UUID NOT NULL REFERENCES core.branches(id),
    expense_no      VARCHAR(100) NOT NULL,
    expense_date    DATE NOT NULL DEFAULT CURRENT_DATE,
    category        VARCHAR(100) NOT NULL,  -- RENT, SALARY, ELECTRICITY, INTERNET
    description     TEXT NOT NULL,
    amount          NUMERIC(20,4) NOT NULL,
    tax_amount      NUMERIC(20,4) NOT NULL DEFAULT 0,
    coa_id          UUID REFERENCES finance.chart_of_accounts(id),
    is_recurring    BOOLEAN NOT NULL DEFAULT FALSE,
    recurring_day   SMALLINT,               -- Tanggal berulang tiap bulan
    receipt_url     TEXT,
    status          approval_status NOT NULL DEFAULT 'PENDING',
    approved_by     UUID REFERENCES core.users(id),
    approved_at     TIMESTAMPTZ,
    paid_by         UUID REFERENCES core.users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, expense_no)
);

-- Inter-Company Transactions
CREATE TABLE finance.inter_company_transactions (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    from_company_id UUID NOT NULL REFERENCES core.companies(id),
    to_company_id   UUID NOT NULL REFERENCES core.companies(id),
    transaction_no  VARCHAR(100) NOT NULL,
    transaction_type VARCHAR(50) NOT NULL DEFAULT 'STOCK_TRANSFER',
    transfer_price  NUMERIC(20,4) NOT NULL,
    tax_amount      NUMERIC(20,4) NOT NULL DEFAULT 0,
    total_amount    NUMERIC(20,4) NOT NULL,
    from_document_id UUID NOT NULL,          -- FK ke stock transfer atau PO
    to_document_id  UUID,                    -- Draft dokumen di perusahaan penerima
    status          inter_company_status NOT NULL DEFAULT 'DRAFT',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Tenant Contract (Ruang Sewa)
CREATE TABLE finance.tenant_space_contracts (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    branch_id       UUID NOT NULL REFERENCES core.branches(id),
    tenant_name     VARCHAR(200) NOT NULL,
    space_code      VARCHAR(50) NOT NULL,
    monthly_rent    NUMERIC(20,4) NOT NULL,
    start_date      DATE NOT NULL,
    end_date        DATE,
    billing_day     SMALLINT NOT NULL DEFAULT 1,
    last_billed_at  DATE,
    status          VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    notes           TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Close Month / Stock Freeze
CREATE TABLE finance.period_closings (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id      UUID NOT NULL REFERENCES core.companies(id),
    period_year     SMALLINT NOT NULL,
    period_month    SMALLINT NOT NULL,
    status          VARCHAR(30) NOT NULL DEFAULT 'OPEN', -- OPEN, FROZEN, CLOSED
    frozen_at       TIMESTAMPTZ,
    closed_at       TIMESTAMPTZ,
    closed_by       UUID REFERENCES core.users(id),
    notes           TEXT,
    UNIQUE(company_id, period_year, period_month)
);

-- COGS Adjustment
CREATE TABLE finance.cogs_adjustments (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    adj_no          VARCHAR(100) NOT NULL,
    product_id      UUID NOT NULL REFERENCES core.products(id),
    branch_id       UUID NOT NULL REFERENCES core.branches(id),
    adj_date        DATE NOT NULL DEFAULT CURRENT_DATE,
    old_cogs        NUMERIC(20,4) NOT NULL,
    new_cogs        NUMERIC(20,4) NOT NULL,
    adjustment_reason TEXT NOT NULL,
    document_url    TEXT,
    status          approval_status NOT NULL DEFAULT 'PENDING',
    approved_by     UUID REFERENCES core.users(id),
    approved_at     TIMESTAMPTZ,
    created_by      UUID REFERENCES core.users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, adj_no)
);

-- Sales Orders (Quotation / B2B)
CREATE TABLE pos.sales_orders (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    branch_id       UUID NOT NULL REFERENCES core.branches(id),
    order_no        VARCHAR(100) NOT NULL,
    customer_id     UUID NOT NULL REFERENCES crm.customers(id),
    order_date      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_until     DATE,
    status          VARCHAR(30) NOT NULL DEFAULT 'QUOTATION', -- QUOTATION, CONFIRMED, PARTIAL, COMPLETED, CANCELLED
    payment_term    VARCHAR(50),             -- DP 30%, NET 30, etc.
    total_amount    NUMERIC(20,4) NOT NULL DEFAULT 0,
    dp_required     NUMERIC(20,4) NOT NULL DEFAULT 0,
    dp_paid         NUMERIC(20,4) NOT NULL DEFAULT 0,
    notes           TEXT,
    created_by      UUID REFERENCES core.users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, order_no)
);

CREATE TABLE pos.sales_order_items (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id        UUID NOT NULL REFERENCES pos.sales_orders(id),
    product_id      UUID NOT NULL REFERENCES core.products(id),
    variant_id      UUID REFERENCES core.product_variants(id),
    uom_id          UUID NOT NULL REFERENCES core.uom(id),
    qty_ordered     NUMERIC(20,4) NOT NULL,
    qty_delivered   NUMERIC(20,4) NOT NULL DEFAULT 0,
    unit_price      NUMERIC(20,4) NOT NULL,
    discount_pct    percentage NOT NULL DEFAULT 0,
    line_total      NUMERIC(20,4) NOT NULL,
    is_reserved     BOOLEAN NOT NULL DEFAULT FALSE,
    reservation_id  UUID REFERENCES inventory.stock_reservations(id)
);

-- Batch Reversal (Koreksi massal)
CREATE TABLE finance.batch_reversals (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    reversal_no     VARCHAR(100) NOT NULL,
    reversal_type   VARCHAR(50) NOT NULL,   -- PRICE_CORRECTION, STOCK_ROLLBACK
    description     TEXT NOT NULL,
    affected_count  INT NOT NULL DEFAULT 0,
    total_value_impact NUMERIC(20,4),
    status          approval_status NOT NULL DEFAULT 'PENDING',
    requested_by    UUID REFERENCES core.users(id),
    approved_by     UUID REFERENCES core.users(id),
    approved_at     TIMESTAMPTZ,
    executed_at     TIMESTAMPTZ,
    rollback_point  TIMESTAMPTZ,             -- Kembalikan stok ke kondisi jam ini
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, reversal_no)
);

CREATE TABLE finance.batch_reversal_items (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    reversal_id     UUID NOT NULL REFERENCES finance.batch_reversals(id),
    reference_type  VARCHAR(50) NOT NULL,
    reference_id    UUID NOT NULL,
    old_value       NUMERIC(20,4),
    new_value       NUMERIC(20,4),
    notes           TEXT
);

-- Price Protection
CREATE TABLE inventory.price_protections (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    supplier_id     UUID NOT NULL REFERENCES core.suppliers(id),
    product_id      UUID NOT NULL REFERENCES core.products(id),
    old_purchase_price NUMERIC(20,4) NOT NULL,
    new_purchase_price NUMERIC(20,4) NOT NULL,
    price_drop_date DATE NOT NULL,
    qty_eligible    NUMERIC(20,4) NOT NULL,
    credit_note_value NUMERIC(20,4) NOT NULL,
    status          approval_status NOT NULL DEFAULT 'PENDING',
    auto_price_update BOOLEAN NOT NULL DEFAULT FALSE,
    approved_by     UUID REFERENCES core.users(id),
    approved_at     TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Scrap / Waste Management
CREATE TABLE inventory.scrap_documents (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    branch_id       UUID NOT NULL REFERENCES core.branches(id),
    scrap_no        VARCHAR(100) NOT NULL,
    scrap_date      DATE NOT NULL DEFAULT CURRENT_DATE,
    scrap_type      VARCHAR(30) NOT NULL DEFAULT 'WRITE_OFF', -- WRITE_OFF, SCRAP_SALE
    total_cost_value NUMERIC(20,4) NOT NULL DEFAULT 0,
    scrap_sale_value NUMERIC(20,4),
    document_url    TEXT,
    status          approval_status NOT NULL DEFAULT 'PENDING',
    approved_by     UUID REFERENCES core.users(id),
    approved_at     TIMESTAMPTZ,
    created_by      UUID REFERENCES core.users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, scrap_no)
);

CREATE TABLE inventory.scrap_items (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    scrap_id        UUID NOT NULL REFERENCES inventory.scrap_documents(id),
    product_id      UUID NOT NULL REFERENCES core.products(id),
    serial_no_id    UUID REFERENCES inventory.serial_numbers(id),
    qty             NUMERIC(20,4) NOT NULL,
    unit_cost       NUMERIC(20,4) NOT NULL,
    scrap_reason    TEXT NOT NULL,
    investigation_doc_url TEXT
);

-- E-Faktur Data (Pajak)
CREATE TABLE finance.efaktur_records (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES core.tenants(id),
    sale_id         UUID REFERENCES pos.sales(id),
    faktur_no       VARCHAR(30) NOT NULL,
    faktur_date     DATE NOT NULL,
    buyer_name      VARCHAR(200) NOT NULL,
    buyer_npwp      VARCHAR(20),
    buyer_address   TEXT,
    dpp_amount      NUMERIC(20,4) NOT NULL,
    ppn_amount      NUMERIC(20,4) NOT NULL,
    is_exported     BOOLEAN NOT NULL DEFAULT FALSE,
    exported_at     TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, faktur_no)
);

-- Omnichannel Stock Buffer
CREATE TABLE inventory.omnichannel_stock_buffers (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    branch_id       UUID NOT NULL REFERENCES core.branches(id),
    product_id      UUID NOT NULL REFERENCES core.products(id),
    channel         VARCHAR(50) NOT NULL,   -- TOKOPEDIA, SHOPEE, WEBSITE
    buffer_qty      NUMERIC(20,4) NOT NULL DEFAULT 0,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE(branch_id, product_id, channel)
);

-- ===========================================================
-- FOREIGN KEY DEFERRED (untuk circular references)
-- ===========================================================
ALTER TABLE core.products ADD CONSTRAINT fk_product_tax
    FOREIGN KEY (tax_code_id) REFERENCES finance.tax_codes(id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE core.users ADD CONSTRAINT fk_user_employee
    FOREIGN KEY (employee_id) REFERENCES hr.employees(id) DEFERRABLE INITIALLY DEFERRED;

-- ===========================================================
-- VIEWS BERGUNA
-- ===========================================================

-- View: Stok Tersedia (Qty On Hand - Reserved - In Transit)
CREATE VIEW inventory.v_available_stock AS
SELECT
    sb.branch_id,
    sb.product_id,
    sb.variant_id,
    sb.uom_id,
    sb.qty_on_hand,
    sb.qty_reserved,
    sb.qty_in_transit,
    (sb.qty_on_hand - sb.qty_reserved - sb.qty_in_transit) AS qty_available,
    sb.avg_cogs,
    (sb.qty_on_hand * sb.avg_cogs) AS total_value,
    p.name AS product_name,
    p.sku,
    b.name AS branch_name
FROM inventory.stock_balances sb
JOIN core.products p ON p.id = sb.product_id
JOIN core.branches b ON b.id = sb.branch_id;

-- View: Piutang Jatuh Tempo
CREATE VIEW finance.v_overdue_ar AS
SELECT
    ci.*,
    c.full_name AS customer_name,
    c.phone AS customer_phone,
    (CURRENT_DATE - ci.due_date) AS overdue_days,
    (ci.outstanding + ci.penalty_amount) AS total_outstanding
FROM finance.customer_invoices ci
JOIN crm.customers c ON c.id = ci.customer_id
WHERE ci.status IN ('OPEN','PARTIAL','OVERDUE')
  AND ci.due_date < CURRENT_DATE;

-- View: Laba Kotor Real-time (per Cabang)
CREATE VIEW analytics.v_gross_profit AS
SELECT
    s.branch_id,
    s.sale_date,
    SUM(si.subtotal) AS total_revenue,
    SUM(si.qty * si.cogs) AS total_cogs,
    SUM(si.subtotal - (si.qty * si.cogs)) AS gross_profit,
    COUNT(DISTINCT s.id) AS transaction_count
FROM pos.sales s
JOIN pos.sale_items si ON si.sale_id = s.id
WHERE s.status = 'PAID' AND s.is_posted = TRUE
GROUP BY s.branch_id, s.sale_date;

-- View: Ringkasan Poin Pelanggan (Valid saja)
CREATE VIEW crm.v_customer_points_summary AS
SELECT
    cp.customer_id,
    SUM(cp.points) AS total_points,
    SUM(CASE WHEN cp.expires_at IS NOT NULL AND cp.expires_at < NOW() + INTERVAL '30 days'
        THEN cp.points ELSE 0 END) AS expiring_soon_points
FROM crm.customer_points cp
WHERE (cp.expires_at IS NULL OR cp.expires_at > NOW())
GROUP BY cp.customer_id;

-- ===========================================================
-- FUNGSI & TRIGGER KRITIS
-- ===========================================================

-- Trigger: Update updated_at otomatis
CREATE OR REPLACE FUNCTION core.update_updated_at()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$;

-- Pasang trigger ke semua tabel yang memiliki updated_at
DO $$
DECLARE
    t TEXT;
BEGIN
    FOR t IN
        SELECT table_schema || '.' || table_name
        FROM information_schema.columns
        WHERE column_name = 'updated_at'
          AND table_schema IN ('core','pos','inventory','crm','finance','hr','logistics')
    LOOP
        EXECUTE format('
            CREATE TRIGGER trg_updated_at
            BEFORE UPDATE ON %s
            FOR EACH ROW EXECUTE FUNCTION core.update_updated_at();
        ', t);
    END LOOP;
END;
$$;

-- Fungsi: Hitung Weighted Average COGS saat barang masuk
CREATE OR REPLACE FUNCTION inventory.recalculate_avg_cogs(
    p_branch_id UUID,
    p_product_id UUID,
    p_variant_id UUID,
    p_uom_id UUID,
    p_qty_in NUMERIC,
    p_cost_in NUMERIC
) RETURNS VOID LANGUAGE plpgsql AS $$
DECLARE
    v_current_qty NUMERIC;
    v_current_cogs NUMERIC;
    v_new_avg NUMERIC;
BEGIN
    SELECT qty_on_hand, avg_cogs
    INTO v_current_qty, v_current_cogs
    FROM inventory.stock_balances
    WHERE branch_id = p_branch_id
      AND product_id = p_product_id
      AND uom_id = p_uom_id
      AND COALESCE(variant_id, '00000000-0000-0000-0000-000000000000'::UUID) =
          COALESCE(p_variant_id, '00000000-0000-0000-0000-000000000000'::UUID)
    FOR UPDATE;

    IF v_current_qty IS NULL OR v_current_qty <= 0 THEN
        v_new_avg := p_cost_in;
    ELSE
        v_new_avg := ((v_current_qty * v_current_cogs) + (p_qty_in * p_cost_in))
                     / (v_current_qty + p_qty_in);
    END IF;

    UPDATE inventory.stock_balances
    SET qty_on_hand = COALESCE(qty_on_hand, 0) + p_qty_in,
        avg_cogs = v_new_avg,
        last_cost = p_cost_in,
        last_movement_at = NOW()
    WHERE branch_id = p_branch_id
      AND product_id = p_product_id
      AND uom_id = p_uom_id
      AND COALESCE(variant_id, '00000000-0000-0000-0000-000000000000'::UUID) =
          COALESCE(p_variant_id, '00000000-0000-0000-0000-000000000000'::UUID);
END;
$$;

-- Fungsi: Validasi Price Guard (Harga Jual < HPP)
CREATE OR REPLACE FUNCTION pos.validate_price_guard(
    p_product_id UUID,
    p_branch_id UUID,
    p_sell_price NUMERIC
) RETURNS JSONB LANGUAGE plpgsql AS $$
DECLARE
    v_avg_cogs NUMERIC;
    v_result JSONB;
BEGIN
    SELECT avg_cogs INTO v_avg_cogs
    FROM inventory.stock_balances
    WHERE product_id = p_product_id AND branch_id = p_branch_id
    LIMIT 1;

    IF v_avg_cogs IS NOT NULL AND p_sell_price < v_avg_cogs THEN
        v_result := jsonb_build_object(
            'is_below_cogs', TRUE,
            'sell_price', p_sell_price,
            'avg_cogs', v_avg_cogs,
            'loss_per_unit', v_avg_cogs - p_sell_price
        );
    ELSE
        v_result := jsonb_build_object('is_below_cogs', FALSE);
    END IF;
    RETURN v_result;
END;
$$;

-- Fungsi: Hitung Grand Total (Pricing Engine)
CREATE OR REPLACE FUNCTION pos.calculate_grand_total(
    p_subtotal NUMERIC,           -- Setelah diskon item
    p_global_disc_pct NUMERIC,
    p_voucher_disc NUMERIC,
    p_manual_adj NUMERIC,
    p_tax_rate NUMERIC,
    p_tax_type tax_type,
    p_rounding_method VARCHAR
) RETURNS TABLE(
    taxable_amount NUMERIC,
    tax_amount NUMERIC,
    rounding NUMERIC,
    grand_total NUMERIC
) LANGUAGE plpgsql AS $$
DECLARE
    v_after_global NUMERIC;
    v_after_voucher NUMERIC;
    v_after_adj NUMERIC;
    v_taxable NUMERIC;
    v_tax NUMERIC;
    v_raw_total NUMERIC;
    v_grand_total NUMERIC;
    v_rounding NUMERIC;
BEGIN
    -- Urutan: Diskon Global -> Voucher -> Manual Adjustment -> Pajak -> Pembulatan
    v_after_global := p_subtotal * (1 - p_global_disc_pct / 100);
    v_after_voucher := GREATEST(0, v_after_global - p_voucher_disc);
    v_after_adj := GREATEST(0, v_after_voucher - p_manual_adj);

    IF p_tax_type = 'EXCLUSIVE' THEN
        v_taxable := v_after_adj;
        v_tax := v_taxable * (p_tax_rate / 100);
        v_raw_total := v_taxable + v_tax;
    ELSIF p_tax_type = 'INCLUSIVE' THEN
        v_taxable := v_after_adj / (1 + p_tax_rate / 100);
        v_tax := v_after_adj - v_taxable;
        v_raw_total := v_after_adj;
    ELSE -- EXEMPT
        v_taxable := v_after_adj;
        v_tax := 0;
        v_raw_total := v_after_adj;
    END IF;

    -- Pembulatan
    IF p_rounding_method = 'DOWN_100' THEN
        v_grand_total := FLOOR(v_raw_total / 100) * 100;
    ELSIF p_rounding_method = 'UP_100' THEN
        v_grand_total := CEIL(v_raw_total / 100) * 100;
    ELSIF p_rounding_method = 'NEAREST_500' THEN
        v_grand_total := ROUND(v_raw_total / 500) * 500;
    ELSE
        v_grand_total := ROUND(v_raw_total);
    END IF;
    v_rounding := v_grand_total - v_raw_total;

    RETURN QUERY SELECT v_taxable, v_tax, v_rounding, v_grand_total;
END;
$$;

-- ===========================================================
-- ROW LEVEL SECURITY (MULTI-TENANT ISOLATION)
-- ===========================================================

ALTER TABLE core.companies         ENABLE ROW LEVEL SECURITY;
ALTER TABLE core.branches          ENABLE ROW LEVEL SECURITY;
ALTER TABLE core.products          ENABLE ROW LEVEL SECURITY;
ALTER TABLE pos.sales              ENABLE ROW LEVEL SECURITY;
ALTER TABLE crm.customers          ENABLE ROW LEVEL SECURITY;
ALTER TABLE inventory.stock_balances ENABLE ROW LEVEL SECURITY;

-- Policy: user hanya bisa lihat data tenant sendiri
-- (diterapkan via current_setting('app.tenant_id') yang di-set saat koneksi)
CREATE POLICY tenant_isolation ON core.companies
    USING (tenant_id = current_setting('app.tenant_id', TRUE)::UUID);
CREATE POLICY tenant_isolation ON core.products
    USING (tenant_id = current_setting('app.tenant_id', TRUE)::UUID);
CREATE POLICY tenant_isolation ON pos.sales
    USING (tenant_id = current_setting('app.tenant_id', TRUE)::UUID);
CREATE POLICY tenant_isolation ON crm.customers
    USING (tenant_id = current_setting('app.tenant_id', TRUE)::UUID);

-- ===========================================================
-- INDEX TAMBAHAN UNTUK PERFORMA
-- ===========================================================

CREATE INDEX idx_sale_items_product ON pos.sale_items(product_id);
CREATE INDEX idx_sale_items_sale ON pos.sale_items(sale_id);
CREATE INDEX idx_stock_movements_date ON inventory.stock_movements(branch_id, created_at);
CREATE INDEX idx_serial_numbers_status ON inventory.serial_numbers(tenant_id, status);
CREATE INDEX idx_batches_expiry_alert ON inventory.batches(expiry_date) WHERE qty_remaining > 0;
CREATE INDEX idx_customer_invoices_due ON finance.customer_invoices(due_date, status) WHERE status NOT IN ('PAID','WRITTEN_OFF');
CREATE INDEX idx_supplier_invoices_due ON finance.supplier_invoices(due_date, status) WHERE status NOT IN ('PAID');
CREATE INDEX idx_reservations_expire ON inventory.stock_reservations(reserved_until) WHERE is_released = FALSE;
CREATE INDEX idx_offline_queue_pending ON core.offline_queue(terminal_id, status) WHERE status = 'PENDING';
CREATE INDEX idx_promotions_active ON pos.promotions(tenant_id, start_date, end_date) WHERE is_active = TRUE;
CREATE INDEX idx_pos_shifts_cashier ON pos.shifts(branch_id, cashier_id, opened_at);

-- ===========================================================
-- SEED DATA: ROLE BAWAAN SISTEM
-- ===========================================================

-- Catatan: seed data roles default diisi saat provisioning tenant baru

-- ===========================================================
-- AKHIR SCHEMA GAPTEK v1.0.0
-- Total Tabel  : ~120 tabel
-- Total Schema : 9 logical schema
-- Total Modul  : 95 fungsional modul
-- Database     : PostgreSQL 15+
-- ===========================================================