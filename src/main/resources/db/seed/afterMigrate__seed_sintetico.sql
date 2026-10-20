-- =====================================================================
-- oncoDoseAPI - seed sintético (só profile dev; callback afterMigrate)
-- Idempotente: só insere o que falta; nunca sobrescreve dado existente.
--
-- Fontes:
--   REAL     = Gê et al. (2023): vetores do Fluorouracil (V, C, E, D) e
--              frascos/custos da Gencitabina (200 mg US$ 16, 1 g US$ 48).
--   CEP      = Figura 1 do projeto do CEP (códigos PRE2026-000123..130).
--   FICTÍCIO = valores inventados para demonstração (custos, estoques,
--              apresentações que os documentos não trazem).
-- =====================================================================

-- Medicamentos (nomes/códigos iguais aos já cadastrados no banco de dev).
INSERT INTO medicamento (nome, codigo_interno) VALUES
    ('Fluorouracil',   'MED001'),
    ('Paclitaxel',     'MED002'),
    ('Carboplatin',    'MED003'),
    ('Gemcitabina',    'MED004'),
    ('Ciclofosfamida', 'MED008'),
    ('Oxaliplatina',   'MED009'),
    ('Docetaxel',      'MED010'),
    ('Irinotecano',    'MED011'),
    ('Trastuzumabe',   'MED012'),
    ('Cabazitaxel',    'MED015')
ON CONFLICT DO NOTHING;

-- Apresentações: (medicamento, volume mg, custo US$, estoque).
INSERT INTO apresentacao_frasco (medicamento_id, volume_mg, custo, quantidade_estoque)
SELECT m.id, v.volume, v.custo, v.estoque
FROM (VALUES
    ('Fluorouracil',    250,   74, 20),   -- REAL
    ('Fluorouracil',    500,  126, 30),   -- REAL
    ('Fluorouracil',   1000,  400, 50),   -- REAL
    ('Gemcitabina',     200,   16, 20),   -- REAL (estoque FICTÍCIO)
    ('Gemcitabina',    1000,   48, 20),   -- REAL (estoque FICTÍCIO)
    ('Paclitaxel',       30,   25, 20),   -- FICTÍCIO
    ('Paclitaxel',      100,   70, 20),   -- FICTÍCIO
    ('Paclitaxel',      300,  190, 10),   -- FICTÍCIO
    ('Carboplatin',      50,   15, 20),   -- FICTÍCIO
    ('Carboplatin',     150,   40, 20),   -- FICTÍCIO
    ('Carboplatin',     450,  110, 10),   -- FICTÍCIO
    ('Ciclofosfamida',  200,    8, 20),   -- FICTÍCIO
    ('Ciclofosfamida', 1000,   30, 10),   -- FICTÍCIO
    ('Oxaliplatina',     50,   60, 20),   -- FICTÍCIO
    ('Oxaliplatina',    100,  110, 20),   -- FICTÍCIO
    ('Docetaxel',        20,   45, 20),   -- FICTÍCIO
    ('Docetaxel',        80,  160, 20),   -- FICTÍCIO
    ('Irinotecano',      40,   20, 20),   -- FICTÍCIO
    ('Irinotecano',     100,   45, 20),   -- FICTÍCIO
    ('Trastuzumabe',    150,  300, 10),   -- FICTÍCIO
    ('Trastuzumabe',    440,  820, 10),   -- FICTÍCIO
    ('Cabazitaxel',      60, 1500,  5)    -- FICTÍCIO
) AS v(nome, volume, custo, estoque)
JOIN medicamento m ON m.nome = v.nome
WHERE NOT EXISTS (
    SELECT 1 FROM apresentacao_frasco f WHERE f.medicamento_id = m.id AND f.volume_mg = v.volume
);

-- Prescrições anonimizadas (LGPD): só código, medicamento, dose e status.
INSERT INTO prescricao (codigo_prescricao, medicamento_id, dose_mg, status, data_prescricao)
SELECT v.codigo, m.id, v.dose, v.status, v.data::date
FROM (VALUES
    -- REAL: Fluorouracil 01/11/2019, D = [300, 570, 490, 1270] → ótimo US$ 704.
    ('PRE2019-000001', 'Fluorouracil', 300,  'APTO',     '2019-11-01'),
    ('PRE2019-000002', 'Fluorouracil', 570,  'APTO',     '2019-11-01'),
    ('PRE2019-000003', 'Fluorouracil', 490,  'APTO',     '2019-11-01'),
    ('PRE2019-000004', 'Fluorouracil', 1270, 'APTO',     '2019-11-01'),
    ('PRE2019-000005', 'Fluorouracil', 5000, 'NAO_APTO', '2019-11-01'),  -- FICTÍCIO: mostra que NAO_APTO fica de fora
    -- Gencitabina 08/11/2019: total REAL de 3488 mg; divisão entre pacientes FICTÍCIA → ótimo US$ 192.
    ('PRE2019-000011', 'Gemcitabina', 1200, 'APTO', '2019-11-08'),
    ('PRE2019-000012', 'Gemcitabina', 1288, 'APTO', '2019-11-08'),
    ('PRE2019-000013', 'Gemcitabina', 1000, 'APTO', '2019-11-08'),
    -- CEP (Figura 1): doses reais do documento; distribuição das datas 02–05/04/2026 FICTÍCIA.
    ('PRE2026-000123', 'Ciclofosfamida', 1000, 'APTO', '2026-04-02'),
    ('PRE2026-000124', 'Paclitaxel',      260, 'APTO', '2026-04-02'),
    ('PRE2026-000125', 'Oxaliplatina',    200, 'APTO', '2026-04-03'),
    ('PRE2026-000126', 'Docetaxel',       120, 'APTO', '2026-04-03'),
    ('PRE2026-000127', 'Cabazitaxel',      45, 'APTO', '2026-04-04'),
    ('PRE2026-000128', 'Irinotecano',     180, 'APTO', '2026-04-04'),
    ('PRE2026-000129', 'Carboplatin',     450, 'APTO', '2026-04-05'),
    ('PRE2026-000130', 'Trastuzumabe',    420, 'APTO', '2026-04-05'),
    -- FICTÍCIO: mais pacientes de Paclitaxel em 02/04 para mostrar o compartilhamento de frascos.
    ('PRE2026-000131', 'Paclitaxel',      175, 'APTO', '2026-04-02'),
    ('PRE2026-000132', 'Paclitaxel',       80, 'APTO', '2026-04-02'),
    ('PRE2026-000133', 'Paclitaxel',      135, 'APTO', '2026-04-02')
) AS v(codigo, nome, dose, status, data)
JOIN medicamento m ON m.nome = v.nome
ON CONFLICT (codigo_prescricao) DO NOTHING;

-- Ciclos (FICTÍCIO): cenário da agenda. Paclitaxel a cada 21 dias (mínimo 19): 9 ciclos têm
-- aplicação em 27/10/2026 e o CIC2026-000010 cai sozinho em 26/10 (3ª aplicação), com janela
-- de +1 dia — o planejador deve sugerir juntá-lo ao grupo de 27/10.
INSERT INTO ciclo (codigo_ciclo, medicamento_id, dose_mg, data_inicio, total_aplicacoes,
                   intervalo_dias, intervalo_minimo_dias)
SELECT v.codigo, m.id, v.dose, v.inicio::date, v.total, 21, 19
FROM (VALUES
    ('CIC2026-000001', 260, '2026-10-27', 6),   -- 27/10 = 1ª aplicação
    ('CIC2026-000002', 175, '2026-10-06', 6),   -- 27/10 = 2ª
    ('CIC2026-000003',  80, '2026-09-15', 6),   -- 27/10 = 3ª
    ('CIC2026-000004', 135, '2026-08-25', 6),   -- 27/10 = 4ª
    ('CIC2026-000005', 150, '2026-10-27', 4),
    ('CIC2026-000006', 210, '2026-10-06', 4),
    ('CIC2026-000007',  95, '2026-09-15', 6),
    ('CIC2026-000008', 120, '2026-10-27', 6),
    ('CIC2026-000009', 190, '2026-10-06', 4),
    ('CIC2026-000010', 175, '2026-09-14', 6)    -- 14/09, 05/10, 26/10 (isolado), 16/11...
) AS v(codigo, dose, inicio, total)
JOIN medicamento m ON m.nome = 'Paclitaxel'
ON CONFLICT (codigo_ciclo) DO NOTHING;

-- Aplicações projetadas: data_prevista = data_inicio + (n-1)·intervalo; agendada começa igual.
INSERT INTO aplicacao (ciclo_id, numero, data_prevista, data_agendada, dias_antes, dias_depois, status)
SELECT c.id, n, c.data_inicio + (n - 1) * c.intervalo_dias, c.data_inicio + (n - 1) * c.intervalo_dias,
       0, CASE WHEN c.codigo_ciclo = 'CIC2026-000010' THEN 1 ELSE 0 END, 'AGENDADA'
FROM ciclo c
CROSS JOIN LATERAL generate_series(1, c.total_aplicacoes) AS n
WHERE c.codigo_ciclo BETWEEN 'CIC2026-000001' AND 'CIC2026-000010'
ON CONFLICT (ciclo_id, numero) DO NOTHING;
