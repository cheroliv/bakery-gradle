-- =================================================================
--  SCHEMA COMPLET ET SÉCURISÉ POUR LE FORMULAIRE DE CONTACT
-- =================================================================

-- 1. Création de la table "contacts"
-- Utilise un UUID comme clé primaire.
-- Garantit l'unicité sur 'email' (si non nul) ET sur 'telephone' (si non nul).
-- Assure qu'au moins l'un des deux est fourni.
-- =================================================================
CREATE TABLE IF NOT EXISTS public.contacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMPTZ DEFAULT now() NOT NULL,
    name TEXT,
    email TEXT,
    telephone TEXT,
    CONSTRAINT contacts_email_key UNIQUE (email),
    CONSTRAINT contacts_telephone_key UNIQUE (telephone),
    CONSTRAINT check_contact_info CHECK (email IS NOT NULL OR telephone IS NOT NULL)
);

-- 2. Création de la table "messages" pour stocker les soumissions
-- =================================================================
CREATE TABLE IF NOT EXISTS public.messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMPTZ DEFAULT now() NOT NULL,
    contact_id UUID NOT NULL REFERENCES public.contacts(id) ON DELETE CASCADE,
    subject TEXT,
    message TEXT
);

-- 3. Activation de la sécurité RLS pour les deux tables
-- =================================================================
ALTER TABLE public.contacts ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.messages ENABLE ROW LEVEL SECURITY;

-- 4. Création de la fonction RPC "handle_contact_form"
-- C'est le seul point d'entrée pour le formulaire. Elle gère la logique
-- de recherche/création du contact et l'insertion du message de manière atomique et sécurisée.
-- =================================================================
CREATE OR REPLACE FUNCTION public.handle_contact_form(
    p_name TEXT,
    p_email TEXT,
    p_subject TEXT,
    p_message TEXT
)
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
AS $
DECLARE
    contact_uuid UUID;
BEGIN
    -- =================================================================
    -- Validation des entrées
    -- =================================================================
    IF p_name IS NULL OR TRIM(p_name) = '' THEN
        RAISE EXCEPTION 'Le nom ne peut pas être vide.' USING ERRCODE = 'P0001';
    END IF;

    IF p_email IS NULL OR p_email !~ '^[a-zA-Z0-9.!#$%&''*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*

-- 5. Donner la permission d'exécuter la fonction au rôle public
-- =================================================================
GRANT EXECUTE
ON FUNCTION public.handle_contact_form(TEXT, TEXT, TEXT, TEXT)
TO anon;

-- 6. Création de la fonction RPC "get_schemas"
-- Permet de lister les schémas de la base de données de manière sécurisée.
-- =================================================================
CREATE OR REPLACE FUNCTION get_schemas()
RETURNS TABLE(schema_name TEXT) AS $$
BEGIN
    RETURN QUERY SELECT nspname::TEXT FROM pg_namespace
    WHERE nspname NOT LIKE 'pg_%'
      AND nspname NOT LIKE 'crdb_%'
      AND nspname != 'information_schema'
      AND nspname != 'supabase_migrations';
END;
$$ LANGUAGE plpgsql;

-- 7. Donner la permission d'exécuter la fonction au rôle public
-- =================================================================
GRANT EXECUTE ON FUNCTION get_schemas() TO anon; THEN
        RAISE EXCEPTION 'L''adresse e-mail fournie n''est pas valide.' USING ERRCODE = 'P0002';
    END IF;

    IF p_subject IS NULL OR TRIM(p_subject) = '' THEN
        RAISE EXCEPTION 'Le sujet ne peut pas être vide.' USING ERRCODE = 'P0001';
    END IF;

    IF p_message IS NULL OR TRIM(p_message) = '' THEN
        RAISE EXCEPTION 'Le message ne peut pas être vide.' USING ERRCODE = 'P0001';
    END IF;

    -- Contrôles de longueur pour éviter les abus
    IF LENGTH(p_name) > 255 THEN
        RAISE EXCEPTION 'Le nom est trop long (maximum 255 caractères).' USING ERRCODE = 'P0003';
    END IF;
    IF LENGTH(p_subject) > 255 THEN
        RAISE EXCEPTION 'Le sujet est trop long (maximum 255 caractères).' USING ERRCODE = 'P0003';
    END IF;
    IF LENGTH(p_message) > 5000 THEN
        RAISE EXCEPTION 'Le message est trop long (maximum 5000 caractères).' USING ERRCODE = 'P0003';
    END IF;

    -- =================================================================
    -- Logique métier
    -- =================================================================
    SELECT id INTO contact_uuid FROM public.contacts WHERE email = p_email;

    IF contact_uuid IS NULL THEN
        INSERT INTO public.contacts (name, email)
        VALUES (p_name, p_email)
        RETURNING id INTO contact_uuid;
    END IF;

    INSERT INTO public.messages (contact_id, subject, message)
    VALUES (contact_uuid, p_subject, p_message);
END;
$;

-- 5. Donner la permission d'exécuter la fonction au rôle public
-- =================================================================
GRANT EXECUTE
ON FUNCTION public.handle_contact_form(TEXT, TEXT, TEXT, TEXT)
TO anon;

-- 6. Création de la fonction RPC "get_schemas"
-- Permet de lister les schémas de la base de données de manière sécurisée.
-- =================================================================
CREATE OR REPLACE FUNCTION get_schemas()
RETURNS TABLE(schema_name TEXT) AS $$
BEGIN
    RETURN QUERY SELECT nspname::TEXT FROM pg_namespace
    WHERE nspname NOT LIKE 'pg_%'
      AND nspname NOT LIKE 'crdb_%'
      AND nspname != 'information_schema'
      AND nspname != 'supabase_migrations';
END;
$$ LANGUAGE plpgsql;

-- 7. Donner la permission d'exécuter la fonction au rôle public
-- =================================================================
GRANT EXECUTE ON FUNCTION get_schemas() TO anon;