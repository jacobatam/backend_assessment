CREATE TABLE tenant_rate_limit_config (
id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
tenant_id UUID NOT NULL UNIQUE,
plan_name VARCHAR(50) NOT NULL,
max_requests INTEGER NOT NULL,
window_seconds INTEGER NOT NULL,
is_active BOOLEAN NOT NULL DEFAULT TRUE,
created_at TIMESTAMP NOT NULL DEFAULT NOW(),
updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

INSERT INTO tenant_rate_limit_config
(tenant_id, plan_name, max_requests, window_seconds)
VALUES
('a1000000-0000-0000-0000-000000000001','FREE',10,60),
('a2000000-0000-0000-0000-000000000002','STARTER',50,60),
('a3000000-0000-0000-0000-000000000003','PRO',200,60);