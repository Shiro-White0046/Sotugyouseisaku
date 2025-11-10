-- 起動時シード用：JDBC側でトランザクション管理するので BEGIN/COMMIT は不要
TRUNCATE TABLE individual_allergies, allergens
  RESTART IDENTITY CASCADE;

-- ===== FOOD（27品目＋α）=====
INSERT INTO allergens (code,name_ja,name_en,is_active,category,subcategory) VALUES
('SHRIMP','えび','Shrimp',TRUE,'FOOD',NULL),
('CRAB','かに','Crab',TRUE,'FOOD',NULL),
('WALNUT','くるみ','Walnut',TRUE,'FOOD',NULL),
('WHEAT','小麦','Wheat',TRUE,'FOOD',NULL),
('BUCKWHEAT','そば','Buckwheat',TRUE,'FOOD',NULL),
('EGG','たまご','Egg',TRUE,'FOOD',NULL),
('MILK','乳','Milk',TRUE,'FOOD',NULL),
('PEANUT','落花生(ピーナッツ)','Peanut',TRUE,'FOOD',NULL),
('ALMOND','アーモンド','Almond',TRUE,'FOOD',NULL),
('ABALONE','あわび','Abalone',TRUE,'FOOD',NULL),
('SQUID','いか','Squid',TRUE,'FOOD',NULL),
('SALMON_ROE','いくら','Salmon roe',TRUE,'FOOD',NULL),
('ORANGE','オレンジ','Orange',TRUE,'FOOD',NULL),
('CASHEW','カシューナッツ','Cashew',TRUE,'FOOD',NULL),
('KIWI','キウイフルーツ','Kiwi',TRUE,'FOOD',NULL),
('BEEF','牛肉','Beef',TRUE,'FOOD',NULL),
('SESAME','ごま','Sesame',TRUE,'FOOD',NULL),
('SALMON','さけ','Salmon',TRUE,'FOOD',NULL),
('MACKEREL','さば','Mackerel',TRUE,'FOOD',NULL),
('SOYBEAN','大豆','Soybean',TRUE,'FOOD',NULL),
('CHICKEN','鶏肉','Chicken',TRUE,'FOOD',NULL),
('BANANA','ばなな','Banana',TRUE,'FOOD',NULL),
('PORK','豚肉','Pork',TRUE,'FOOD',NULL),
('MACADAMIA','マカダミアナッツ','Macadamia nut',TRUE,'FOOD',NULL),
('PEACH','もも','Peach',TRUE,'FOOD',NULL),
('YAM','やまいも','Yam',TRUE,'FOOD',NULL),
('APPLE','りんご','Apple',TRUE,'FOOD',NULL),
('GELATIN','ゼラチン','Gelatin',TRUE,'FOOD',NULL);

-- =========================
-- CONTACT / METAL（金属）
-- =========================
INSERT INTO allergens (code,name_ja,name_en,is_active,category,subcategory) VALUES
('C_METAL_NICKEL','ニッケル','Nickel',TRUE,'CONTACT','METAL'),
('C_METAL_COBALT','コバルト','Cobalt',TRUE,'CONTACT','METAL'),
('C_METAL_CHROMIUM','クロム','Chromium',TRUE,'CONTACT','METAL'),
('C_METAL_COPPER','銅','Copper',TRUE,'CONTACT','METAL'),
('C_METAL_SILVER','銀','Silver',TRUE,'CONTACT','METAL'),
('C_METAL_GOLD','金','Gold',TRUE,'CONTACT','METAL'),
('C_METAL_ZINC','亜鉛','Zinc',TRUE,'CONTACT','METAL'),
('C_METAL_PALLADIUM','パラジウム','Palladium',TRUE,'CONTACT','METAL'),
('C_METAL_PLATINUM','白金','Platinum',TRUE,'CONTACT','METAL'),
('C_METAL_IRON','鉄','Iron',TRUE,'CONTACT','METAL'),
('C_METAL_TIN','スズ','Tin',TRUE,'CONTACT','METAL'),
('C_METAL_ALUMINUM','アルミニウム','Aluminum',TRUE,'CONTACT','METAL'),
('C_METAL_TITANIUM','チタン','Titanium',TRUE,'CONTACT','METAL');

-- =========================
-- CONTACT / CHEMICAL（化学物質・保存料・香料・樹脂・染料・ゴム薬剤・医薬品等）
-- =========================
-- 保存料・ホルムアルデヒド系
INSERT INTO allergens (code,name_ja,name_en,is_active,category,subcategory) VALUES
('C_CHEMICAL_PARABENS','パラベン','Parabens',TRUE,'CONTACT','CHEMICAL'),
('C_CHEMICAL_FORMALDEHYDE','ホルムアルデヒド','Formaldehyde',TRUE,'CONTACT','CHEMICAL'),
('C_CHEMICAL_QUATERNIUM15','塩化第4級アンモニウム(Quaternium-15)','Quaternium-15',TRUE,'CONTACT','CHEMICAL'),
('C_CHEMICAL_IMIDAZOLIDINYL_UREA','イミダゾリジニル尿素','Imidazolidinyl urea',TRUE,'CONTACT','CHEMICAL'),
('C_CHEMICAL_DIAZOLIDINYL_UREA','ジアゾリジニル尿素','Diazolidinyl urea',TRUE,'CONTACT','CHEMICAL'),

-- イソチアゾリノン類（防腐）
('C_CHEMICAL_MI','メチルイソチアゾリノン(MI)','Methylisothiazolinone',TRUE,'CONTACT','CHEMICAL'),
('C_CHEMICAL_MCI','メチルクロロイソチアゾリノン(MCI)','Methylchloroisothiazolinone',TRUE,'CONTACT','CHEMICAL'),
('C_CHEMICAL_MI_MCI','イソチアゾリノン混合物(MI/MCI)','Isothiazolinones (MI/MCI)',TRUE,'CONTACT','CHEMICAL'),

-- 溶剤・多価アルコールなど
('C_CHEMICAL_PROPYLENE_GLYCOL','プロピレングリコール','Propylene glycol',TRUE,'CONTACT','CHEMICAL'),
('C_CHEMICAL_ETHANOLAMINES','エタノールアミン類','Ethanolamines',TRUE,'CONTACT','CHEMICAL'),
('C_CHEMICAL_SESQUITERPENE_LACTONES','セスキテルペンラクトン類','Sesquiterpene lactones',TRUE,'CONTACT','CHEMICAL'),

-- 香料・精油成分
('C_CHEMICAL_FRAGRANCE_MIX','香料ミックス','Fragrance mix',TRUE,'CONTACT','CHEMICAL'),
('C_CHEMICAL_BALSAM_PERU','ペルーバルサム','Balsam of Peru',TRUE,'CONTACT','CHEMICAL'),
('C_CHEMICAL_CINNAMAL','シンナマル(桂皮アルデヒド)','Cinnamal',TRUE,'CONTACT','CHEMICAL'),
('C_CHEMICAL_EUGENOL','オイゲノール','Eugenol',TRUE,'CONTACT','CHEMICAL'),
('C_CHEMICAL_ISOEUGENOL','イソオイゲノール','Isoeugenol',TRUE,'CONTACT','CHEMICAL'),
('C_CHEMICAL_GERANIOL','ゲラニオール','Geraniol',TRUE,'CONTACT','CHEMICAL'),
('C_CHEMICAL_CITRAL','シトラール','Citral',TRUE,'CONTACT','CHEMICAL'),
('C_CHEMICAL_HYDROXYCITRONELLAL','ヒドロキシシトロネラール','Hydroxycitronellal',TRUE,'CONTACT','CHEMICAL'),
('C_CHEMICAL_OAKMOSS','オークモス(樹苔)','Oakmoss (Evernia prunastri)',TRUE,'CONTACT','CHEMICAL'),
('C_CHEMICAL_LIMONENE','リモネン','Limonene',TRUE,'CONTACT','CHEMICAL'),
('C_CHEMICAL_LINALOOL','リナロール','Linalool',TRUE,'CONTACT','CHEMICAL'),

-- 表面活性剤・処方成分
('C_CHEMICAL_COCAMIDOPROPYL_BETAINE','コカミドプロピルベタイン','Cocamidopropyl betaine',TRUE,'CONTACT','CHEMICAL'),
('C_CHEMICAL_LANOLIN','ラノリン(ウールアルコール)','Lanolin (wool alcohols)',TRUE,'CONTACT','CHEMICAL'),

-- 殺菌・消毒
('C_CHEMICAL_BENZALKONIUM_CHLORIDE','塩化ベンザルコニウム','Benzalkonium chloride',TRUE,'CONTACT','CHEMICAL'),
('C_CHEMICAL_CHLORHEXIDINE','クロルヘキシジン','Chlorhexidine',TRUE,'CONTACT','CHEMICAL'),

-- 日焼け止め・紫外線吸収剤
('C_CHEMICAL_OXYBENZONE','オキシベンゾン(BP-3)','Oxybenzone (Benzophenone-3)',TRUE,'CONTACT','CHEMICAL'),
('C_CHEMICAL_OCTOCRYLENE','オクトクリレン','Octocrylene',TRUE,'CONTACT','CHEMICAL'),

-- ゴム・ラテックス関連（加硫促進剤など）
('C_CHEMICAL_LATEX','天然ゴム・ラテックス','Natural latex',TRUE,'CONTACT','CHEMICAL'),
('C_CHEMICAL_THIURAM_MIX','チウラム混合物','Thiuram mix',TRUE,'CONTACT','CHEMICAL'),
('C_CHEMICAL_CARBAMATE_MIX','カルバメート混合物','Carbamate mix',TRUE,'CONTACT','CHEMICAL'),
('C_CHEMICAL_MERCAPTO_MIX','メルカプト混合物(MBT等)','Mercapto mix (MBT etc.)',TRUE,'CONTACT','CHEMICAL'),
('C_CHEMICAL_BLACK_RUBBER_MIX','ブラックラバー混合物','Black rubber mix',TRUE,'CONTACT','CHEMICAL'),

-- 樹脂・接着・コーティング
('C_CHEMICAL_EPOXY_RESIN','エポキシ樹脂','Epoxy resin',TRUE,'CONTACT','CHEMICAL'),
('C_CHEMICAL_PHENOL_FORMALDEHYDE','フェノール-ホルムアルデヒド樹脂','Phenol-formaldehyde resins',TRUE,'CONTACT','CHEMICAL'),
('C_CHEMICAL_COLOPHONY','ロジン(コロホニー)','Colophony (rosin)',TRUE,'CONTACT','CHEMICAL'),
('C_CHEMICAL_ACRYLATE_HEMA','アクリレート(HEMA)','2-Hydroxyethyl methacrylate (HEMA)',TRUE,'CONTACT','CHEMICAL'),
('C_CHEMICAL_ETHYL_CYANOACRYLATE','エチルシアノアクリレート','Ethyl cyanoacrylate',TRUE,'CONTACT','CHEMICAL'),

-- 染料・テキスタイル
('C_CHEMICAL_PPD','パラフェニレンジアミン(PPD)','p-Phenylenediamine',TRUE,'CONTACT','CHEMICAL'),
('C_CHEMICAL_DISPERSE_BLUE_106','分散染料ブルー106','Disperse Blue 106',TRUE,'CONTACT','CHEMICAL'),
('C_CHEMICAL_DISPERSE_BLUE_124','分散染料ブルー124','Disperse Blue 124',TRUE,'CONTACT','CHEMICAL'),

-- 外用薬・鎮痛消炎成分など
('C_CHEMICAL_KETOPROFEN','ケトプロフェン','Ketoprofen',TRUE,'CONTACT','CHEMICAL'),
('C_CHEMICAL_DICLOFENAC','ジクロフェナク','Diclofenac',TRUE,'CONTACT','CHEMICAL'),
('C_CHEMICAL_NEOMYCIN','ネオマイシン','Neomycin',TRUE,'CONTACT','CHEMICAL'),
('C_CHEMICAL_BACITRACIN','バシトラシン','Bacitracin',TRUE,'CONTACT','CHEMICAL'),
('C_CHEMICAL_LIDOCAINE','リドカイン','Lidocaine',TRUE,'CONTACT','CHEMICAL'),

-- 食品添加・その他
('C_CHEMICAL_SORBIC_ACID','ソルビン酸','Sorbic acid',TRUE,'CONTACT','CHEMICAL'),
('C_CHEMICAL_PHENOXYETHANOL','フェノキシエタノール','Phenoxyethanol',TRUE,'CONTACT','CHEMICAL');

-- =========================
-- CONTACT / PLANT（植物・天然物）
-- =========================
INSERT INTO allergens (code,name_ja,name_en,is_active,category,subcategory) VALUES
('C_PLANT_URUSHIOL','ウルシ(漆)','Urushiol (Toxicodendron)',TRUE,'CONTACT','PLANT'),
('C_PLANT_CHRYSANTHEMUM','キク(キク科全般)','Chrysanthemum',TRUE,'CONTACT','PLANT'),
('C_PLANT_CHAMOMILE','カモミール','Chamomile',TRUE,'CONTACT','PLANT'),
('C_PLANT_LAVENDER','ラベンダー','Lavender',TRUE,'CONTACT','PLANT'),
('C_PLANT_GARLIC','ニンニク(接触)','Garlic (contact)',TRUE,'CONTACT','PLANT'),
('C_PLANT_CITRUS_PEEL','柑橘果皮(リモネン由来)','Citrus peels',TRUE,'CONTACT','PLANT'),
('C_PLANT_TEA_TREE','ティーツリー','Tea tree',TRUE,'CONTACT','PLANT');

-- =========================
-- CONTACT / ANIMAL（動物由来）
-- =========================
INSERT INTO allergens (code,name_ja,name_en,is_active,category,subcategory) VALUES
('C_ANIMAL_PROPOLIS','プロポリス','Propolis',TRUE,'CONTACT','ANIMAL'),
('C_ANIMAL_CARMINE','カルミン(コチニール)','Carmine (cochineal)',TRUE,'CONTACT','ANIMAL'),
('C_ANIMAL_LANOLIN','ラノリン(ウールアルコール)','Lanolin (wool alcohols)',TRUE,'CONTACT','ANIMAL');

-- =========================
-- CONTACT / OTHER（混合・分類困難）
-- =========================
INSERT INTO allergens (code,name_ja,name_en,is_active,category,subcategory) VALUES
('C_OTHER_NAIL_GLUE','ネイル用接着剤(瞬間接着)','Nail glue (instant adhesives)',TRUE,'CONTACT','OTHER'),
('C_OTHER_PHOTOINITIATORS','光重合開始剤(レジン)','Photoinitiators (resins)',TRUE,'CONTACT','OTHER'),
('C_OTHER_DISINFECTANT_MIX','消毒剤混合物(複合要因）','Disinfectant mix (various)',TRUE,'CONTACT','OTHER');

-- ===== AVOID（食べられないもの／禁忌・制限）=====
INSERT INTO allergens (code,name_ja,name_en,is_active,category,subcategory) VALUES
('AVOID_PORK','豚肉','Pork',TRUE,'AVOID',NULL),
('AVOID_BEEF','牛肉','Beef',TRUE,'AVOID',NULL),
('AVOID_RAW_MEAT','生肉','Raw meat',TRUE,'AVOID',NULL),
('AVOID_LIVER','レバー','Liver',TRUE,'AVOID',NULL),
('AVOID_SHELLFISH_SHRIMP','エビ（宗教等の理由）','Shrimp',TRUE,'AVOID',NULL),
('AVOID_TUNA_HIGH_MERCURY','マグロ等（妊婦は制限）','Tuna/large fish',TRUE,'AVOID',NULL),
('AVOID_RAW_FISH','生魚','Raw fish',TRUE,'AVOID',NULL),
('AVOID_RAW_EGG','生卵','Raw egg',TRUE,'AVOID',NULL),
('AVOID_UNHEATED_CHEESE','非加熱チーズ','Unheated cheese',TRUE,'AVOID',NULL),
('AVOID_MILK','牛乳','Milk',TRUE,'AVOID',NULL),
('AVOID_EGG_YOLK','卵黄','Egg yolk',TRUE,'AVOID',NULL),
('AVOID_BUTTER','バター','Butter',TRUE,'AVOID',NULL),
('AVOID_CREAM','生クリーム','Fresh cream',TRUE,'AVOID',NULL),
('AVOID_YOGURT','ヨーグルト','Yogurt',TRUE,'AVOID',NULL),
('AVOID_SOFT_EGG','半熟卵','Soft-boiled egg',TRUE,'AVOID',NULL),
('AVOID_GARLIC','ニンニク','Garlic',TRUE,'AVOID',NULL),
('AVOID_NIRA','ニラ','Chinese chive',TRUE,'AVOID',NULL),
('AVOID_RAKKYO','ラッキョウ','Pickled scallion',TRUE,'AVOID',NULL),
('AVOID_NEGI','ネギ','Green onion',TRUE,'AVOID',NULL),
('AVOID_ONION','タマネギ','Onion',TRUE,'AVOID',NULL),
('AVOID_MOCHI','餅','Mochi rice cake',TRUE,'AVOID',NULL),
('AVOID_HONEY','蜂蜜','Honey',TRUE,'AVOID',NULL);
