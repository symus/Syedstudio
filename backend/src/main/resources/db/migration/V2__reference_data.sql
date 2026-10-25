-- Reference data. Plans are proposed TEST prices, not confirmed market rates.
insert into subscription_plan (code, name, price_minor, currency, billing_interval, max_published_products,
                               max_images_per_product, custom_domain, analytics, reduced_branding, position)
values ('FREE', 'Free', 0, 'PLN', 'MONTH', 10, 4, false, false, false, 0),
       ('CREATOR', 'Creator', 2900, 'PLN', 'MONTH', 60, 8, false, true, true, 1),
       ('PRO', 'Pro', 6900, 'PLN', 'MONTH', 500, 12, true, true, true, 2);

insert into storefront_template (key, name, description, default_primary_color, default_accent_color, default_font, position)
values ('atelier', 'Atelier', 'Warm, editorial layout with a large hero image and serif headings.', '#2F2A26', '#B5654A', 'SERIF', 0),
       ('studio', 'Studio', 'Clean, gallery-first layout that lets product photography lead.', '#1F2933', '#6B8F71', 'SANS', 1),
       ('market', 'Market', 'Friendly, colourful layout with rounded cards and a bold hero.', '#3B2F2F', '#C98B2E', 'ROUNDED', 2);

with c (slug, position, en, pl) as (
    values ('jewelry-accessories', 0, 'Jewelry & accessories', 'Biżuteria i dodatki'),
           ('home-decor', 1, 'Home decor', 'Dekoracje wnętrz'),
           ('ceramics-pottery', 2, 'Ceramics & pottery', 'Ceramika'),
           ('candles-fragrance', 3, 'Candles & fragrance', 'Świece i zapachy'),
           ('clothing-textiles', 4, 'Clothing & textiles', 'Odzież i tekstylia'),
           ('crochet-knitting', 5, 'Crochet & knitting', 'Szydełko i dziewiarstwo'),
           ('art-illustrations', 6, 'Art & illustrations', 'Sztuka i ilustracje'),
           ('personalized-gifts', 7, 'Personalized gifts', 'Prezenty personalizowane'),
           ('wedding-crafts', 8, 'Wedding crafts & favors', 'Rękodzieło ślubne'),
           ('baby-family-keepsakes', 9, 'Baby & family keepsakes', 'Pamiątki dla dzieci i rodziny'),
           ('woodworking', 10, 'Woodworking', 'Wyroby z drewna'),
           ('other-handmade', 11, 'Other handmade creations', 'Inne rękodzieło')
), ins as (
    insert into category (id, slug, position, active, created_at, updated_at)
        select gen_random_uuid(), slug, position, true, now(), now() from c
        returning id, slug
)
insert into category_translation (category_id, locale, name)
select ins.id, 'en', c.en from ins join c on c.slug = ins.slug
union all
select ins.id, 'pl', c.pl from ins join c on c.slug = ins.slug;
