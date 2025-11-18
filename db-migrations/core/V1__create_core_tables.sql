-- =====================================================
-- CORE DATABASE - MIGRATION V1
-- Descripción: Tablas principales para gestión multitenancy
-- =====================================================

-- Tabla: persons
-- Almacena información de todas las personas del sistema
CREATE TABLE IF NOT EXISTS persons (
    uuid VARCHAR(36) PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    second_name VARCHAR(100),
    paternal_name VARCHAR(100) NOT NULL,
    mother_name VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(150) UNIQUE,
    birthday DATE,
    password VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Constraints
    CONSTRAINT chk_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}$' OR email IS NULL),
    CONSTRAINT chk_phone_format CHECK (phone ~ '^[0-9+\-\s()]+$' OR phone IS NULL)
);

-- Índices para persons
CREATE INDEX idx_persons_email ON persons(email) WHERE email IS NOT NULL;
CREATE INDEX idx_persons_full_name ON persons(first_name, second_name, paternal_name, mother_name);

-- Comentarios
COMMENT ON TABLE persons IS 'Almacena información de todas las personas del sistema (usuarios, residentes, referencias)';
COMMENT ON COLUMN persons.uuid IS 'Identificador único público de la persona (usado en APIs)';
COMMENT ON COLUMN persons.password IS 'Hash de contraseña - NULL si la persona no tiene acceso al sistema';
COMMENT ON COLUMN persons.email IS 'Email - NULL si la persona no requiere login o notificaciones';

-- =====================================================
-- Tabla: tenants
-- Almacena configuración de cada condominio/tenant
CREATE TABLE IF NOT EXISTS tenants (
    id BIGSERIAL PRIMARY KEY,
    system_id VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    schema_name VARCHAR(63) NOT NULL UNIQUE,
    
    -- Configuración de conexión a BD
    db_host VARCHAR(255) NOT NULL DEFAULT 'localhost',
    db_port INTEGER NOT NULL DEFAULT 5432,
    db_name VARCHAR(100) NOT NULL,
    db_username VARCHAR(100) NOT NULL,
    db_password_encrypted TEXT NOT NULL,
    
    -- Metadata
    timezone VARCHAR(50) DEFAULT 'America/La_Paz',
    locale VARCHAR(10) DEFAULT 'es_BO',
    currency VARCHAR(3) DEFAULT 'BOB',
    
    -- Control de estado
    is_active BOOLEAN DEFAULT TRUE,
    is_suspended BOOLEAN DEFAULT FALSE,
    suspended_reason TEXT,
    suspended_at TIMESTAMP,
    
    -- Auditoría
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by_person_uuid VARCHAR(36),
    
    -- Constraints
    CONSTRAINT fk_tenants_created_by FOREIGN KEY (created_by_person_uuid) 
        REFERENCES persons(uuid) ON DELETE SET NULL,
    CONSTRAINT chk_schema_name_format CHECK (schema_name ~ '^[a-z][a-z0-9_]*$'),
    CONSTRAINT chk_system_id_format CHECK (system_id ~ '^[A-Z0-9_-]+$'),
    CONSTRAINT chk_port_range CHECK (db_port BETWEEN 1 AND 65535)
);

-- Índices para tenants
CREATE INDEX idx_tenants_system_id ON tenants(system_id);
CREATE INDEX idx_tenants_schema_name ON tenants(schema_name);
CREATE INDEX idx_tenants_is_active ON tenants(is_active);
CREATE INDEX idx_tenants_is_suspended ON tenants(is_suspended);

-- Comentarios
COMMENT ON TABLE tenants IS 'Configuración de cada condominio (tenant) en el sistema multitenancy';
COMMENT ON COLUMN tenants.system_id IS 'Identificador único del tenant (ej: COND_001, EDIF_CENTRAL)';
COMMENT ON COLUMN tenants.schema_name IS 'Nombre del schema de PostgreSQL para este tenant';
COMMENT ON COLUMN tenants.db_password_encrypted IS 'Contraseña encriptada para conexión a la BD del tenant';
COMMENT ON COLUMN tenants.is_suspended IS 'Indica si el tenant está temporalmente suspendido (no eliminado)';

-- =====================================================
-- Tabla: person_tenant
-- Tabla intermedia: Relación muchos a muchos entre personas y tenants
CREATE TABLE IF NOT EXISTS person_tenant (
    person_uuid VARCHAR(36) NOT NULL,
    tenant_id BIGINT NOT NULL,
    
    -- Auditoría
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    PRIMARY KEY (person_uuid, tenant_id),
    CONSTRAINT fk_person_tenant_person FOREIGN KEY (person_uuid) 
        REFERENCES persons(uuid) ON DELETE CASCADE,
    CONSTRAINT fk_person_tenant_tenant FOREIGN KEY (tenant_id) 
        REFERENCES tenants(id) ON DELETE CASCADE
);

-- Índices para person_tenant
CREATE INDEX idx_person_tenant_tenant_id ON person_tenant(tenant_id);

-- Comentarios
COMMENT ON TABLE person_tenant IS 'Tabla intermedia para la relación muchos a muchos entre personas y condominios (tenants)';

-- =====================================================
-- Función para actualizar updated_at automáticamente
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Triggers para updated_at
CREATE TRIGGER trg_persons_updated_at
    BEFORE UPDATE ON persons
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_tenants_updated_at
    BEFORE UPDATE ON tenants
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- Datos iniciales de ejemplo (opcional - comentar en producción)
-- INSERT INTO persons (uuid, first_name, paternal_name, email, password) 
-- VALUES 
--     (gen_random_uuid()::text, 'Admin', 'Sistema', 'admin@condaty.com', 'hashed_password_here');