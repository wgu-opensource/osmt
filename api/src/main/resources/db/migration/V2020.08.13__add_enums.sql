USE osmt_db;

ALTER TABLE osmt_db.Keyword
    MODIFY COLUMN keyword_type_enum enum (
        'Category',
        'Certifications',
        'Other',
        'ProfessionalStandards',
        'Sel',
        'Tools',
        'Keyword',
        'Alignment',
        'Occupation',
        'Employers'
        );
